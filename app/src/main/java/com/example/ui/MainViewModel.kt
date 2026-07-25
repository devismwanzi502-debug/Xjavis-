package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.accessibility.PhonePilotAccessibilityService
import com.example.ai.AgentPlanner
import com.example.ai.GeminiClient
import com.example.ai.MemoryEngine
import com.example.automation.AutomationEngine
import com.example.automation.TriggerManager
import com.example.data.ActivityLog
import com.example.data.AutomationRule
import com.example.data.MemoryEntity
import com.example.data.PhonePilotRepository
import com.example.notifications.AutoReplyEngine
import com.example.notifications.InterceptedNotification
import com.example.notifications.PhonePilotNotificationListener
import com.example.voice.SpeechRecognizerManager
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = PhonePilotRepository(application)
    val agentPlanner = AgentPlanner(application)
    val geminiClient = GeminiClient(application)
    val memoryEngine = MemoryEngine(application)
    val triggerManager = TriggerManager(application)
    val autoReplyEngine = AutoReplyEngine(application)

    val speechManager = SpeechRecognizerManager(application)
    val ttsManager = TextToSpeechManager(application)

    val isAccessibilityEnabled: StateFlow<Boolean> = PhonePilotAccessibilityService.isConnected
    val isNotificationListenerEnabled: StateFlow<Boolean> = PhonePilotNotificationListener.isConnected

    val isExecuting = agentPlanner.isExecuting
    val currentPlan = agentPlanner.currentPlan
    val lastMessage = agentPlanner.lastMessage

    val rules: StateFlow<List<AutomationRule>> = repository.allRules.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val memories: StateFlow<List<MemoryEntity>> = memoryEngine.memoryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val logs: StateFlow<List<ActivityLog>> = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentNotifications: StateFlow<List<InterceptedNotification>> = PhonePilotNotificationListener.recentNotifications

    val isListening = speechManager.isListening
    val isSpeaking = ttsManager.isSpeaking
    val recognizedText = speechManager.recognizedText

    init {
        viewModelScope.launch {
            recognizedText.collect { text ->
                if (text.isNotBlank()) {
                    val lower = text.lowercase()
                    if (lower.startsWith("hello jarvis") || lower.startsWith("hey jarvis") || lower.startsWith("jarvis") || lower.startsWith("hello phone pilot")) {
                        // Automatically run wake word command
                        sendUserCommand(text)
                    }
                }
            }
        }
    }

    fun sendUserCommand(command: String) {
        viewModelScope.launch {
            agentPlanner.executeUserCommand(command)
        }
    }

    fun toggleVoiceListening() {
        if (speechManager.isListening.value) {
            speechManager.stopListening()
            val query = speechManager.recognizedText.value
            if (query.isNotBlank()) {
                sendUserCommand(query)
            }
        } else {
            speechManager.startListening()
        }
    }

    fun speakText(text: String) {
        ttsManager.speak(text)
    }

    fun addAutomationRule(name: String, triggerType: String, triggerValue: String, actionType: String, actionValue: String) {
        viewModelScope.launch {
            repository.insertRule(
                AutomationRule(
                    name = name,
                    triggerType = triggerType,
                    triggerValue = triggerValue,
                    actionType = actionType,
                    actionValue = actionValue
                )
            )
        }
    }

    fun toggleAutomationRule(rule: AutomationRule) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun deleteAutomationRule(rule: AutomationRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    fun fireTriggerTest(triggerType: String, triggerValue: String) {
        viewModelScope.launch {
            triggerManager.fireTrigger(triggerType, triggerValue)
        }
    }

    fun addMemory(key: String, value: String) {
        viewModelScope.launch {
            memoryEngine.savePreference(key, value)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryEngine.deleteMemory(id)
        }
    }

    fun clearAllMemory() {
        viewModelScope.launch {
            memoryEngine.clearAll()
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun sendAutoReply(notification: InterceptedNotification, replyText: String) {
        viewModelScope.launch {
            val sbn = notification.sbn
            val success = autoReplyEngine.sendReplyWithFallback(sbn, notification.appLabel, replyText)
            if (success) {
                repository.log("Auto-Reply Sent", "To ${notification.appLabel}: $replyText", "NOTIFICATION")
            } else {
                repository.log("Auto-Reply Attempted", "Sent to ${notification.appLabel}: $replyText", "NOTIFICATION")
            }
        }
    }

    fun sendChatbotAutoReply(notification: InterceptedNotification) {
        viewModelScope.launch {
            val prompt = "Compose a friendly, brief auto-reply (under 15 words) for a message from ${notification.title} on ${notification.appLabel}. Message content: '${notification.text}'."
            val aiResponse = geminiClient.generateResponse(prompt)
            val finalReply = if (aiResponse.contains("Gemini API key is not configured")) {
                "Thanks for reaching out! Received your message on ${notification.appLabel}."
            } else {
                aiResponse.trim().removeSurrounding("\"")
            }
            sendAutoReply(notification, finalReply)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}
