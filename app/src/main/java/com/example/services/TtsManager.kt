package com.example.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    var speechRate: Float = 1.0f
        set(value) {
            field = value
            tts?.setSpeechRate(value)
        }

    var speechPitch: Float = 1.0f
        set(value) {
            field = value
            tts?.setPitch(value)
        }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
            isInitialized = true
        } else {
            isInitialized = false
        }
    }

    fun speak(text: String, voiceStyle: String = "Spruce Male", languageCode: String = "en") {
        if (!isInitialized) return
        
        val locale = when (languageCode.lowercase()) {
            "te", "telugu" -> Locale("te", "IN")
            "hi", "hindi" -> Locale("hi", "IN")
            "ta", "tamil" -> Locale("ta", "IN")
            else -> Locale.US
        }
        
        tts?.language = locale

        // Search system TTS engine for matching male/female voice if available
        try {
            val isMaleRequested = voiceStyle.lowercase().contains("male") || voiceStyle.lowercase().contains("baritone") || voiceStyle.lowercase() == "spruce"
            val availableVoices = tts?.voices
            if (!availableVoices.isNullOrEmpty()) {
                val targetVoice = availableVoices.find { voice ->
                    val voiceName = voice.name.lowercase()
                    if (isMaleRequested) {
                        voiceName.contains("male") || voiceName.contains("en-us-x-sfg") || voiceName.contains("en-us-x-iom") || voiceName.contains("m-local")
                    } else {
                        voiceName.contains("female") || voiceName.contains("en-us-x-sfd") || voiceName.contains("f-local")
                    }
                }
                if (targetVoice != null) {
                    tts?.voice = targetVoice
                }
            }
        } catch (e: Exception) {
            // Fallback to pitch modulation
        }

        when (voiceStyle.lowercase()) {
            "spruce male", "spruce deep", "male", "deep baritone", "spruce" -> {
                // Deep, clear, resonant male tone
                tts?.setPitch(0.72f)
                tts?.setSpeechRate(speechRate * 0.98f)
            }
            "female", "soft melody" -> {
                tts?.setPitch(1.18f)
                tts?.setSpeechRate(speechRate * 1.02f)
            }
            "studio warm" -> {
                tts?.setPitch(0.85f)
                tts?.setSpeechRate(speechRate * 0.95f)
            }
            else -> {
                tts?.setPitch(0.75f)
                tts?.setSpeechRate(speechRate)
            }
        }
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ROVA_UTTERANCE")
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
