package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "buddy"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false,
    val emotionDetected: String = "neutral",
    val intentActionExecuted: String? = null
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueTimeFormatted: String,
    val isCompleted: Boolean = false,
    val category: String = "General"
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val triggerText: String,
    val actionText: String,
    val isEnabled: Boolean = true
)

@Entity(tableName = "user_memory")
data class UserMemoryEntity(
    @PrimaryKey val memoryKey: String,
    val memoryValue: String,
    val updatedAt: Long = System.currentTimeMillis()
)
