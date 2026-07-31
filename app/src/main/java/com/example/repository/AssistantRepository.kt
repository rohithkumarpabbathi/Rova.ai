package com.example.repository

import com.example.data.*
import com.example.services.AiAssistantEngine
import com.example.services.SpeechRecognitionManager
import com.example.services.SystemActionExecutor
import com.example.services.TtsManager
import kotlinx.coroutines.flow.Flow

class AssistantRepository(
    private val dao: AssistantDao,
    val systemActionExecutor: SystemActionExecutor,
    val ttsManager: TtsManager,
    val speechRecognitionManager: SpeechRecognitionManager,
    val aiEngine: AiAssistantEngine
) {

    val allMessages: Flow<List<ChatMessageEntity>> = dao.getAllMessages()
    val allReminders: Flow<List<ReminderEntity>> = dao.getAllReminders()
    val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()
    val allRules: Flow<List<AutomationRuleEntity>> = dao.getAllAutomationRules()
    val allUserMemory: Flow<List<UserMemoryEntity>> = dao.getAllUserMemory()

    suspend fun saveMessage(message: ChatMessageEntity) {
        dao.insertMessage(message)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    suspend fun addReminder(title: String, dueTime: String, category: String = "General") {
        dao.insertReminder(ReminderEntity(title = title, dueTimeFormatted = dueTime, category = category))
    }

    suspend fun toggleReminder(id: Long, completed: Boolean) {
        dao.updateReminderStatus(id, completed)
    }

    suspend fun deleteReminder(id: Long) {
        dao.deleteReminder(id)
    }

    suspend fun addNote(title: String, content: String) {
        dao.insertNote(NoteEntity(title = title, content = content))
    }

    suspend fun deleteNote(id: Long) {
        dao.deleteNote(id)
    }

    suspend fun addRule(title: String, trigger: String, action: String) {
        dao.insertRule(AutomationRuleEntity(title = title, triggerText = trigger, actionText = action))
    }

    suspend fun toggleRule(id: Long, enabled: Boolean) {
        dao.toggleRule(id, enabled)
    }

    suspend fun deleteRule(id: Long) {
        dao.deleteRule(id)
    }

    suspend fun saveUserMemory(key: String, value: String) {
        dao.saveMemory(UserMemoryEntity(memoryKey = key, memoryValue = value))
    }
}
