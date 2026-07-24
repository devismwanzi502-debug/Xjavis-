package com.example.automation

import android.content.Context
import android.util.Log
import com.example.ai.GeminiClient
import com.example.data.PhonePilotRepository
import com.example.device.AppLauncher
import com.example.device.SettingsController
import com.example.voice.TextToSpeechManager

class AutomationEngine(private val context: Context) {

    private val repository = PhonePilotRepository(context)
    private val appLauncher = AppLauncher(context)
    private val settingsController = SettingsController(context)
    private val geminiClient = GeminiClient(context)

    suspend fun onNotificationReceived(appLabel: String, title: String, text: String) {
        val rules = repository.getEnabledRules()
        for (rule in rules) {
            if (rule.triggerType == "NOTIFICATION") {
                if (appLabel.contains(rule.triggerValue, ignoreCase = true) ||
                    title.contains(rule.triggerValue, ignoreCase = true)) {
                    executeAction(rule.actionType, rule.actionValue, "Notification from $appLabel: $title ($text)")
                }
            }
        }
    }

    suspend fun onChargerConnected() {
        val rules = repository.getEnabledRules()
        for (rule in rules) {
            if (rule.triggerType == "CHARGER") {
                executeAction(rule.actionType, rule.actionValue, "Charger Connected")
            }
        }
    }

    suspend fun onVoicePhrase(phrase: String) {
        val rules = repository.getEnabledRules()
        for (rule in rules) {
            if (rule.triggerType == "VOICE") {
                if (phrase.contains(rule.triggerValue, ignoreCase = true)) {
                    executeAction(rule.actionType, rule.actionValue, "Voice Phrase: $phrase")
                }
            }
        }
    }

    private suspend fun executeAction(actionType: String, actionValue: String, triggerContext: String) {
        repository.log(
            title = "Automation Triggered",
            detail = "Trigger: $triggerContext | Action: $actionType ($actionValue)",
            type = "AUTOMATION"
        )
        Log.d(TAG, "Executing Automation: $actionType -> $actionValue")

        when (actionType) {
            "AUTO_REPLY_TEXT", "SPECIFIC_TEXT" -> {
                val tts = TextToSpeechManager(context)
                val autoReplyEngine = com.example.notifications.AutoReplyEngine(context)
                val appLabel = triggerContext.substringAfter("Notification from ").substringBefore(":")
                val success = autoReplyEngine.sendReplyWithFallback(null, appLabel, actionValue)
                tts.speak("Auto-reply $actionValue sent to $appLabel.")
                repository.log("Specific Text Reply", "Success: $success | Message: $actionValue", "REPLY")
            }
            "AUTO_REPLY_AI", "CHATBOT_REPLY" -> {
                val prompt = "Compose a concise, polite auto-reply (under 15 words) for context: '$triggerContext'. Guidance: '$actionValue'."
                val aiResponse = geminiClient.generateResponse(prompt)
                val replyText = if (aiResponse.contains("Gemini API key is not configured")) {
                    "Thanks for reaching out! Auto-reply generated via PhonePilot."
                } else {
                    aiResponse.trim().removeSurrounding("\"")
                }
                val autoReplyEngine = com.example.notifications.AutoReplyEngine(context)
                val appLabel = triggerContext.substringAfter("Notification from ").substringBefore(":")
                val success = autoReplyEngine.sendReplyWithFallback(null, appLabel, replyText)
                val tts = TextToSpeechManager(context)
                tts.speak("Chatbot AI Auto-reply sent: $replyText")
                repository.log("Chatbot AI Reply Sent", "Success: $success | Reply: $replyText", "REPLY")
            }
            "LAUNCH_APP" -> {
                appLauncher.launchAppByName(actionValue)
            }
            "TOGGLE_DND" -> {
                val enable = actionValue.contains("enable", ignoreCase = true) || actionValue.contains("on", ignoreCase = true)
                settingsController.setDoNotDisturb(enable)
            }
            "SPEAK" -> {
                val tts = TextToSpeechManager(context)
                tts.speak(actionValue)
            }
            "VOLUME" -> {
                val vol = actionValue.filter { it.isDigit() }.toIntOrNull() ?: 50
                settingsController.setVolume(vol)
            }
        }
    }

    companion object {
        private const val TAG = "AutomationEngine"
    }
}
