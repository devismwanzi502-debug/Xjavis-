package com.example.ai

import android.content.Context
import com.example.data.MemoryEntity
import com.example.data.PhonePilotRepository
import kotlinx.coroutines.flow.Flow

class MemoryEngine(private val context: Context) {
    private val repository = PhonePilotRepository(context)

    val memoryFlow: Flow<List<MemoryEntity>> = repository.allMemory

    suspend fun savePreference(key: String, value: String) {
        repository.insertMemory(key, value, "Preference")
    }

    suspend fun saveRoutine(routineName: String, description: String) {
        repository.insertMemory(routineName, description, "Routine")
    }

    suspend fun deleteMemory(id: Long) {
        repository.deleteMemory(id)
    }

    suspend fun clearAll() {
        repository.clearMemory()
    }
}
