package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val detail: String,
    val type: String = "INFO", // INFO, ACTION, ACCESSIBILITY, VOICE, WARNING, ERROR
    val timestamp: Long = System.currentTimeMillis()
)
