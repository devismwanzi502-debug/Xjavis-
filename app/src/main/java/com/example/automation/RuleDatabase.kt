package com.example.automation

import android.content.Context
import com.example.data.AutomationRule
import com.example.data.PhonePilotRepository

class RuleDatabase(context: Context) {
    private val repository = PhonePilotRepository(context)

    suspend fun getEnabledRules(): List<AutomationRule> {
        return repository.getEnabledRules()
    }

    suspend fun addRule(name: String, triggerType: String, triggerValue: String, actionType: String, actionValue: String): Long {
        return repository.insertRule(
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
