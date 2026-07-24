package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_rules")
data class AutomationRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val triggerType: String, // NOTIFICATION, CHARGER, VOICE, TIME
    val triggerValue: String, // e.g. "Instagram", "Gaming Mode", "Connected"
    val actionType: String, // LAUNCH_APP, TOGGLE_DND, SPEAK, CUSTOM_PLAN
    val actionValue: String, // e.g. "com.spotify.music", "Do Not Disturb Enabled", "Call of Duty Mobile"
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
