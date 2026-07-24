package com.example.automation

import android.content.Context
import android.util.Log

class TriggerManager(private val context: Context) {
    private val automationEngine = AutomationEngine(context)

    suspend fun fireTrigger(triggerType: String, triggerValue: String) {
        Log.d("TriggerManager", "Firing trigger $triggerType: $triggerValue")
        when (triggerType) {
            "CHARGER" -> automationEngine.onChargerConnected()
            "VOICE" -> automationEngine.onVoicePhrase(triggerValue)
            "NOTIFICATION" -> automationEngine.onNotificationReceived(triggerValue, "Trigger", "Manual Test")
        }
    }
}
