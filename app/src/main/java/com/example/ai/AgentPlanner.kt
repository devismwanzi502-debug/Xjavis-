package com.example.ai

import android.content.Context
import android.util.Log
import com.example.accessibility.GestureExecutor
import com.example.accessibility.PhonePilotAccessibilityService
import com.example.accessibility.UiNavigator
import com.example.data.PhonePilotRepository
import com.example.device.AppLauncher
import com.example.device.SettingsController
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StepExecutionState(
    val stepIndex: Int,
    val description: String,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false
)

class AgentPlanner(private val context: Context) {

    private val repository = PhonePilotRepository(context)
    private val appLauncher = AppLauncher(context)
    private val settingsController = SettingsController(context)
    private val uiNavigator = UiNavigator(context)
    private val gestureExecutor = GestureExecutor(context)
    private val geminiClient = GeminiClient(context)
    private val commandParser = CommandParser()
    private val ttsManager = TextToSpeechManager(context)

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _currentPlan = MutableStateFlow<List<StepExecutionState>>(emptyList())
    val currentPlan: StateFlow<List<StepExecutionState>> = _currentPlan.asStateFlow()

    private val _lastMessage = MutableStateFlow<String?>(null)
    val lastMessage: StateFlow<String?> = _lastMessage.asStateFlow()

    suspend fun executeUserCommand(command: String) {
        if (_isExecuting.value) return
        _isExecuting.value = true
        _lastMessage.value = "Planning: \"$command\""
        repository.log("Agent Command Received", command, "ACTION")

        val chain = commandParser.parseChain(command)

        if (chain.size > 1) {
            executeCommandChain(chain)
        } else if (chain.isNotEmpty()) {
            val parsed = chain.first()
            if (parsed.actionType == CommandAction.UNKNOWN_COMPLEX) {
                executeComplexGeminiPlan(command)
            } else {
                val plan = listOf(getStepDescription(parsed))
                setPlanSteps(plan)
                updateStepState(0, false)
                val success = executeSingleAction(parsed)
                updateStepState(0, success, !success)
                speakAndLog(getCompletionMessage(parsed, success))
            }
        } else {
            executeComplexGeminiPlan(command)
        }

        _isExecuting.value = false
    }

    private suspend fun executeCommandChain(chain: List<ParsedCommand>) {
        val planDescriptions = chain.map { getStepDescription(it) }
        setPlanSteps(planDescriptions)

        var successCount = 0
        for ((index, cmd) in chain.withIndex()) {
            updateStepState(index, false)
            val success = executeSingleAction(cmd)
            updateStepState(index, success, !success)
            if (success) successCount++
            delay(1000)
        }

        speakAndLog("Executed $successCount of ${chain.size} actions in chain.")
    }

    private suspend fun executeSingleAction(cmd: ParsedCommand): Boolean {
        return when (cmd.actionType) {
            CommandAction.OPEN_APP -> {
                if (cmd.primaryTarget.equals("settings", ignoreCase = true)) {
                    settingsController.openSettingsScreen(android.provider.Settings.ACTION_SETTINGS)
                    true
                } else {
                    appLauncher.launchAppByName(cmd.primaryTarget)
                }
            }
            CommandAction.SWITCH_DARK_MODE -> {
                val enable = cmd.primaryTarget != "OFF"
                settingsController.setDarkMode(enable)
                delay(1200)
                val clickedDisplay = uiNavigator.clickText("Display") || uiNavigator.clickText("Display & brightness")
                if (clickedDisplay) delay(800)
                uiNavigator.clickText("Dark theme") || uiNavigator.clickText("Dark mode") || uiNavigator.clickText("Dark")
                true
            }
            CommandAction.SEARCH_APP -> {
                if (cmd.primaryTarget.isNotEmpty()) {
                    appLauncher.launchAppByName(cmd.primaryTarget)
                    delay(1800)
                }
                val clickedSearch = uiNavigator.clickSearchButton()
                if (clickedSearch) delay(800)
                val typed = uiNavigator.typeText(cmd.secondaryTarget)
                if (typed) {
                    delay(800)
                    uiNavigator.clickSendButton()
                }
                true
            }
            CommandAction.SET_VOLUME -> {
                settingsController.setVolume(cmd.numericValue)
            }
            CommandAction.TOGGLE_DND -> {
                settingsController.setDoNotDisturb(cmd.primaryTarget == "ON")
            }
            CommandAction.CLICK_TEXT -> {
                uiNavigator.clickText(cmd.primaryTarget)
            }
            CommandAction.TYPE_TEXT -> {
                uiNavigator.typeText(cmd.primaryTarget)
            }
            CommandAction.GO_HOME -> {
                gestureExecutor.performGlobalHome()
            }
            CommandAction.GO_BACK -> {
                gestureExecutor.performGlobalBack()
            }
            CommandAction.OPEN_WEBSITE -> {
                appLauncher.openWebsite(cmd.primaryTarget)
            }
            CommandAction.GAMING_MODE -> {
                settingsController.setDoNotDisturb(true)
                appLauncher.launchAppByName("Call of Duty")
                settingsController.setVolume(80)
                true
            }
            CommandAction.LONG_SCROLL_LOOP -> {
                val app = cmd.primaryTarget.ifEmpty { "TikTok" }
                val minutes = if (cmd.numericValue <= 0) 5 else cmd.numericValue
                appLauncher.launchAppByName(app)
                delay(2500)
                // Execute continuous scroll loop
                val totalScrolls = minOf(minutes * 6, 60) // Up to 60 scroll actions per request block
                for (i in 1..totalScrolls) {
                    _lastMessage.value = "Scrolling $app (Cycle $i/$totalScrolls for $minutes mins)..."
                    gestureExecutor.swipeUp()
                    delay(3500) // Watch 3.5s per video
                }
                true
            }
            CommandAction.GAME_LOBBY_NAVIGATE -> {
                val gameName = cmd.primaryTarget.ifEmpty { "Call of Duty" }
                settingsController.setDoNotDisturb(true)
                settingsController.setVolume(85)
                appLauncher.launchAppByName(gameName)
                delay(3000)
                _lastMessage.value = "Entering $gameName Ranked Lobby..."
                val clickedRank = uiNavigator.clickText("Ranked") ||
                        uiNavigator.clickText("Multiplayer") ||
                        uiNavigator.clickText("Lobby") ||
                        uiNavigator.clickText("Ranked Match") ||
                        uiNavigator.clickText("Start")
                if (!clickedRank) {
                    delay(2000)
                    uiNavigator.clickText("Play") || uiNavigator.clickText("Battle Royale")
                }
                true
            }
            CommandAction.UNKNOWN_COMPLEX -> {
                executeStepString(cmd.rawCommand)
                true
            }
        }
    }

    private suspend fun executeComplexGeminiPlan(command: String) {
        val dump = PhonePilotAccessibilityService.getInstance()?.dumpScreenHierarchy() ?: "No active window"
        val steps = geminiClient.planExecutionSteps(command, dump)
        setPlanSteps(steps)

        var stepIdx = 0
        for (rawStep in steps) {
            updateStepState(stepIdx, false)
            executeStepString(rawStep)
            updateStepState(stepIdx, true)
            stepIdx++
            delay(800)
        }
        speakAndLog("Task completed.")
    }

    private fun getStepDescription(cmd: ParsedCommand): String {
        return when (cmd.actionType) {
            CommandAction.OPEN_APP -> "Launch ${cmd.primaryTarget}"
            CommandAction.SWITCH_DARK_MODE -> "Switch to Dark Mode (${cmd.primaryTarget})"
            CommandAction.SEARCH_APP -> "Search \"${cmd.secondaryTarget}\" in ${cmd.primaryTarget.ifEmpty { "app" }}"
            CommandAction.SET_VOLUME -> "Set Volume to ${cmd.numericValue}%"
            CommandAction.TOGGLE_DND -> "Turn Do Not Disturb ${cmd.primaryTarget}"
            CommandAction.CLICK_TEXT -> "Click \"${cmd.primaryTarget}\""
            CommandAction.TYPE_TEXT -> "Type \"${cmd.primaryTarget}\""
            CommandAction.GO_HOME -> "Navigate Home"
            CommandAction.GO_BACK -> "Navigate Back"
            CommandAction.OPEN_WEBSITE -> "Open Website ${cmd.primaryTarget}"
            CommandAction.GAMING_MODE -> "Activate Gaming Mode"
            CommandAction.LONG_SCROLL_LOOP -> "Auto-scroll ${cmd.primaryTarget.ifEmpty { "TikTok" }} for ${if (cmd.numericValue <= 0) 5 else cmd.numericValue} mins"
            CommandAction.GAME_LOBBY_NAVIGATE -> "Launch ${cmd.primaryTarget} & Navigate to ${cmd.secondaryTarget}"
            CommandAction.UNKNOWN_COMPLEX -> cmd.rawCommand
        }
    }

    private fun getCompletionMessage(cmd: ParsedCommand, success: Boolean): String {
        if (!success) return "Failed to execute ${getStepDescription(cmd)}."
        return when (cmd.actionType) {
            CommandAction.OPEN_APP -> "Opened ${cmd.primaryTarget}."
            CommandAction.SWITCH_DARK_MODE -> "Switched to Dark Mode."
            CommandAction.SEARCH_APP -> "Searching for ${cmd.secondaryTarget}."
            CommandAction.SET_VOLUME -> "Volume set to ${cmd.numericValue}%."
            CommandAction.TOGGLE_DND -> "Do Not Disturb turned ${cmd.primaryTarget}."
            CommandAction.CLICK_TEXT -> "Clicked ${cmd.primaryTarget}."
            CommandAction.TYPE_TEXT -> "Typed ${cmd.primaryTarget}."
            CommandAction.GO_HOME -> "Navigated Home."
            CommandAction.GO_BACK -> "Navigated Back."
            CommandAction.OPEN_WEBSITE -> "Opened ${cmd.primaryTarget}."
            CommandAction.GAMING_MODE -> "Gaming Mode activated."
            CommandAction.LONG_SCROLL_LOOP -> "Completed continuous scrolling in ${cmd.primaryTarget.ifEmpty { "TikTok" }}."
            CommandAction.GAME_LOBBY_NAVIGATE -> "Entered ${cmd.primaryTarget} ${cmd.secondaryTarget}."
            CommandAction.UNKNOWN_COMPLEX -> "Action performed."
        }
    }

    private suspend fun executeStepString(step: String) {
        val upper = step.uppercase()
        when {
            upper.startsWith("OPEN_APP:") -> {
                val target = step.substringAfter(":").trim()
                appLauncher.launchAppByName(target)
            }
            upper.startsWith("CLICK_TEXT:") -> {
                val target = step.substringAfter(":").trim()
                uiNavigator.clickText(target)
            }
            upper.startsWith("TYPE_TEXT:") -> {
                val target = step.substringAfter(":").trim()
                uiNavigator.typeText(target)
            }
            upper.startsWith("SET_VOLUME:") -> {
                val vol = step.filter { it.isDigit() }.toIntOrNull() ?: 50
                settingsController.setVolume(vol)
            }
            upper.startsWith("TOGGLE_DND:") -> {
                val enable = upper.contains("ON")
                settingsController.setDoNotDisturb(enable)
            }
            upper.startsWith("SPEAK:") -> {
                val text = step.substringAfter(":").trim()
                ttsManager.speak(text)
            }
        }
    }

    private fun setPlanSteps(descriptions: List<String>) {
        _currentPlan.value = descriptions.mapIndexed { idx, desc ->
            StepExecutionState(stepIndex = idx, description = desc)
        }
    }

    private fun updateStepState(index: Int, isCompleted: Boolean, isFailed: Boolean = false) {
        val list = _currentPlan.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(isCompleted = isCompleted, isFailed = isFailed)
            _currentPlan.value = list
        }
    }

    private suspend fun speakAndLog(message: String) {
        _lastMessage.value = message
        ttsManager.speak(message)
        repository.log("PhonePilot Response", message, "INFO")
    }

    companion object {
        private const val TAG = "AgentPlanner"
    }
}
