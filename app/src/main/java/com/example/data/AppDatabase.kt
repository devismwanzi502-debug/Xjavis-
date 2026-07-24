package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automation_rules ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<AutomationRule>>

    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1")
    suspend fun getEnabledRulesList(): List<AutomationRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AutomationRule): Long

    @Update
    suspend fun updateRule(rule: AutomationRule)

    @Delete
    suspend fun deleteRule(rule: AutomationRule)
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM ai_memory ORDER BY timestamp DESC")
    fun getAllMemory(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM ai_memory WHERE `key` = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): MemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Query("DELETE FROM ai_memory WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM ai_memory")
    suspend fun clearMemory()
}

@Dao
interface LogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog): Long

    @Query("DELETE FROM activity_logs")
    suspend fun clearLogs()
}

@Database(
    entities = [AutomationRule::class, MemoryEntity::class, ActivityLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun automationDao(): AutomationDao
    abstract fun memoryDao(): MemoryDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "phonepilot_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
