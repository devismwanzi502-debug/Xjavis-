package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class PhonePilotRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val automationDao = db.automationDao()
    private val memoryDao = db.memoryDao()
    private val logDao = db.logDao()

    val allRules: Flow<List<AutomationRule>> = automationDao.getAllRules()
    val allMemory: Flow<List<MemoryEntity>> = memoryDao.getAllMemory()
    val allLogs: Flow<List<ActivityLog>> = logDao.getAllLogs()

    suspend fun getEnabledRules(): List<AutomationRule> = automationDao.getEnabledRulesList()
    suspend fun insertRule(rule: AutomationRule): Long = automationDao.insertRule(rule)
    suspend fun updateRule(rule: AutomationRule) = automationDao.updateRule(rule)
    suspend fun deleteRule(rule: AutomationRule) = automationDao.deleteRule(rule)

    suspend fun insertMemory(key: String, value: String, category: String = "Preference") {
        memoryDao.insertMemory(MemoryEntity(key = key, value = value, category = category))
    }
    suspend fun deleteMemory(id: Long) = memoryDao.deleteMemoryById(id)
    suspend fun clearMemory() = memoryDao.clearMemory()

    suspend fun log(title: String, detail: String, type: String = "INFO") {
        logDao.insertLog(ActivityLog(title = title, detail = detail, type = type))
    }
    suspend fun clearLogs() = logDao.clearLogs()
}
