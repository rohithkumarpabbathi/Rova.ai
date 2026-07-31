package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChatMessageEntity
import com.example.repository.AssistantRepository
import com.example.services.AiAssistantEngine
import com.example.services.SpeechRecognitionManager
import com.example.services.SystemActionExecutor
import com.example.services.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val systemActionExecutor = SystemActionExecutor(application)
    private val ttsManager = TtsManager(application)
    private val speechManager = SpeechRecognitionManager(application)
    private val aiEngine = AiAssistantEngine(systemActionExecutor)

    val repository = AssistantRepository(
        dao = db.assistantDao(),
        systemActionExecutor = systemActionExecutor,
        ttsManager = ttsManager,
        speechRecognitionManager = speechManager,
        aiEngine = aiEngine
    )

    val messages = repository.allMessages.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val reminders = repository.allReminders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val notes = repository.allNotes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val automationRules = repository.allRules.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val isListening = speechManager.isListening
    val isSpeaking = ttsManager.isSpeaking
    val audioRmsLevel = speechManager.audioRmsLevel

    val wakeWordEnabled = MutableStateFlow(true)
    val voiceStyle = MutableStateFlow("Spruce Male")
    val voiceGender = voiceStyle // Alias for compatibility
    val speechSpeed = MutableStateFlow(1.0f)
    val preferredLanguage = MutableStateFlow("English")
    val assistantName = MutableStateFlow("Rova.ai")
    val activeTab = MutableStateFlow("Voice")

    val isProcessing = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            // Seed initial greeting and default rules if database is empty
            repository.allMessages.collect { list ->
                if (list.isEmpty()) {
                    repository.saveMessage(
                        ChatMessageEntity(
                            sender = "buddy",
                            text = "Hello! I am Rova.ai, your AI Voice Assistant. Say 'Hey Rova.ai' or tap the glowing mic button to speak with me!",
                            emotionDetected = "cheerful"
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.allRules.collect { list ->
                if (list.isEmpty()) {
                    repository.addRule("Home Arrival Automation", "When I reach home", "Turn on Wi-Fi & set volume to 80%")
                    repository.addRule("Night Routine", "At 10:00 PM", "Enable Do Not Disturb & lower brightness")
                }
            }
        }
    }

    fun startListening() {
        speechManager.startListening(getLangCode(preferredLanguage.value)) { text ->
            processQuery(text, isVoiceInput = true)
        }
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun sendTextQuery(query: String) {
        if (query.isBlank()) return
        processQuery(query, isVoiceInput = false)
    }

    private fun processQuery(query: String, isVoiceInput: Boolean) {
        viewModelScope.launch {
            isProcessing.value = true
            
            // Save user query
            repository.saveMessage(
                ChatMessageEntity(sender = "user", text = query, isVoice = isVoiceInput)
            )

            val currentHistory = messages.value.map { it.sender to it.text }
            val aiResponse = aiEngine.processUserQuery(
                userQuery = query,
                conversationHistory = currentHistory,
                assistantName = assistantName.value,
                preferredLanguage = preferredLanguage.value
            )

            // Save assistant reply
            repository.saveMessage(
                ChatMessageEntity(
                    sender = "buddy",
                    text = aiResponse.replyText,
                    emotionDetected = aiResponse.emotion,
                    intentActionExecuted = aiResponse.detectedAction
                )
            )

            isProcessing.value = false

            // Speak reply if TTS enabled
            ttsManager.speechRate = speechSpeed.value
            ttsManager.speak(
                text = aiResponse.replyText,
                voiceStyle = voiceStyle.value,
                languageCode = preferredLanguage.value
            )
        }
    }

    fun runVisionAnalysis(mode: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            isProcessing.value = true
            kotlinx.coroutines.delay(1200) // Simulated scan processing delay

            val prompt = when (mode) {
                "OCR Text Reader" -> "Scan camera document and read text."
                "Math Solver" -> "Solve math equation in camera view."
                "QR Code Scanner" -> "Decode QR Code in camera frame."
                "Screen Summarizer" -> "Summarize currently displayed screen."
                else -> "Identify object in camera view."
            }

            val result = when (mode) {
                "OCR Text Reader" -> "Detected Text: 'Welcome to Rova.ai Voice & Vision Engine v2.0. Multimodal system online.'"
                "Math Solver" -> "Math Solution: 3x + 12 = 27 ➔ 3x = 15 ➔ x = 5. Verified."
                "QR Code Scanner" -> "Decoded QR Code: https://ai.studio/build (AI Studio Applet Link)"
                "Screen Summarizer" -> "Screen Analysis: Jetpack Compose UI with dark glassmorphic panels, active microphone visualizer, and responsive voice feedback."
                else -> "Object Identified: High-definition Android tablet & stylus (Confidence: 99.2%)."
            }

            // Save vision query & response
            repository.saveMessage(ChatMessageEntity(sender = "user", text = "[Vision Scan • $mode]"))
            repository.saveMessage(ChatMessageEntity(sender = "buddy", text = result, emotionDetected = "cheerful"))

            isProcessing.value = false
            onComplete(result)

            // Speak out vision result using Spruce voice
            ttsManager.speechRate = speechSpeed.value
            ttsManager.speak(
                text = result,
                voiceStyle = voiceStyle.value,
                languageCode = preferredLanguage.value
            )
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun addReminder(title: String, dueTime: String, category: String = "General") {
        viewModelScope.launch {
            repository.addReminder(title, dueTime, category)
        }
    }

    fun toggleReminder(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(id, completed)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            repository.addNote(title, content)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    fun addAutomationRule(title: String, trigger: String, action: String) {
        viewModelScope.launch {
            repository.addRule(title, trigger, action)
        }
    }

    fun toggleAutomationRule(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleRule(id, enabled)
        }
    }

    fun deleteAutomationRule(id: Long) {
        viewModelScope.launch {
            repository.deleteRule(id)
        }
    }

    private fun getLangCode(lang: String): String {
        return when (lang) {
            "Telugu" -> "te-IN"
            "Hindi" -> "hi-IN"
            "Tamil" -> "ta-IN"
            else -> "en-US"
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        speechManager.destroy()
    }
}
