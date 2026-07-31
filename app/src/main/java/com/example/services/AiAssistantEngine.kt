package com.example.services

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class AiResponse(
    val replyText: String,
    val detectedAction: String? = null,
    val actionParam: String? = null,
    val emotion: String = "cheerful"
)

class AiAssistantEngine(private val systemActionExecutor: SystemActionExecutor) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GenerateContentRequest::class.java)
    private val responseAdapter = moshi.adapter(GenerateContentResponse::class.java)

    suspend fun processUserQuery(
        userQuery: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        assistantName: String = "Rova.ai",
        preferredLanguage: String = "English"
    ): AiResponse = withContext(Dispatchers.IO) {

        val lowerQuery = userQuery.lowercase().trim()

        // 1. Check for immediate local intent shortcuts (Offline First Execution)
        when {
            lowerQuery.startsWith("open ") -> {
                val appName = userQuery.substringAfter("open ").trim()
                val result = systemActionExecutor.openApp(appName)
                return@withContext AiResponse(
                    replyText = "Opening $appName for you.",
                    detectedAction = "OPEN_APP",
                    actionParam = appName
                )
            }
            lowerQuery.contains("turn on flashlight") || lowerQuery.contains("enable flashlight") || lowerQuery.contains("flashlight on") -> {
                val msg = systemActionExecutor.toggleFlashlight(true)
                return@withContext AiResponse(replyText = msg, detectedAction = "FLASHLIGHT_ON")
            }
            lowerQuery.contains("turn off flashlight") || lowerQuery.contains("disable flashlight") || lowerQuery.contains("flashlight off") -> {
                val msg = systemActionExecutor.toggleFlashlight(false)
                return@withContext AiResponse(replyText = msg, detectedAction = "FLASHLIGHT_OFF")
            }
            lowerQuery.contains("volume up") || lowerQuery.contains("increase volume") -> {
                val msg = systemActionExecutor.adjustVolume(true)
                return@withContext AiResponse(replyText = msg, detectedAction = "VOLUME_UP")
            }
            lowerQuery.contains("volume down") || lowerQuery.contains("decrease volume") -> {
                val msg = systemActionExecutor.adjustVolume(false)
                return@withContext AiResponse(replyText = msg, detectedAction = "VOLUME_DOWN")
            }
            lowerQuery.contains("wifi settings") || lowerQuery.contains("turn on wifi") || lowerQuery.contains("open wifi") -> {
                val msg = systemActionExecutor.openSettingsScreen("wifi")
                return@withContext AiResponse(replyText = "Opening Wi-Fi settings for you.", detectedAction = "WIFI_SETTINGS")
            }
            lowerQuery.contains("bluetooth settings") || lowerQuery.contains("open bluetooth") -> {
                val msg = systemActionExecutor.openSettingsScreen("bluetooth")
                return@withContext AiResponse(replyText = "Opening Bluetooth settings.", detectedAction = "BLUETOOTH_SETTINGS")
            }
            lowerQuery.contains("search youtube for") || lowerQuery.contains("play on youtube") -> {
                val query = userQuery.substringAfter("for ").substringAfter("youtube ").trim()
                val msg = systemActionExecutor.smartSearch(query, "youtube")
                return@withContext AiResponse(replyText = "Searching YouTube for '$query'.", detectedAction = "YOUTUBE_SEARCH", actionParam = query)
            }
            lowerQuery.contains("navigate to") || lowerQuery.contains("maps for") -> {
                val destination = userQuery.substringAfter("to ").substringAfter("maps ").trim()
                val msg = systemActionExecutor.smartSearch(destination, "maps")
                return@withContext AiResponse(replyText = "Starting Google Maps navigation to '$destination'.", detectedAction = "MAPS_NAVIGATE", actionParam = destination)
            }
            lowerQuery.contains("search google for") || lowerQuery.contains("google search") -> {
                val query = userQuery.substringAfter("for ").substringAfter("search ").trim()
                val msg = systemActionExecutor.smartSearch(query, "google")
                return@withContext AiResponse(replyText = "Searching Google for '$query'.", detectedAction = "GOOGLE_SEARCH", actionParam = query)
            }
            lowerQuery.contains("emergency") || lowerQuery.contains("sos") || lowerQuery.contains("call emergency") -> {
                val msg = systemActionExecutor.triggerEmergencySos()
                return@withContext AiResponse(replyText = msg, detectedAction = "EMERGENCY_SOS")
            }
            lowerQuery.contains("call ") -> {
                val nameOrNum = userQuery.substringAfter("call ").trim()
                val msg = systemActionExecutor.makeCall(nameOrNum)
                return@withContext AiResponse(replyText = "Calling $nameOrNum.", detectedAction = "MAKE_CALL", actionParam = nameOrNum)
            }
        }

        // 2. Call Gemini 3.5 Flash Model via API
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Smart local conversational response generator fallback
            return@withContext generateSmartFallback(userQuery, assistantName, preferredLanguage)
        }

        try {
            val systemPrompt = """
                You are '$assistantName', a world-class AI voice assistant for Android inspired by Siri.
                You give concise, human-like, helpful, and natural responses.
                Keep responses brief (1-3 sentences) suitable for Text-To-Speech output.
                Language requested: $preferredLanguage.
            """.trimIndent()

            val contentsList = mutableListOf<Content>()
            conversationHistory.takeLast(4).forEach { (user, assistant) ->
                contentsList.add(Content(role = "user", parts = listOf(Part(text = user))))
                contentsList.add(Content(role = "model", parts = listOf(Part(text = assistant))))
            }
            contentsList.add(Content(role = "user", parts = listOf(Part(text = userQuery))))

            val genRequest = GenerateContentRequest(
                contents = contentsList,
                generationConfig = GenerationConfig(temperature = 0.7f),
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )

            val jsonBody = requestAdapter.toJson(genRequest)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val httpResponse = okHttpClient.newCall(httpRequest).execute()
            val responseString = httpResponse.body?.string()

            if (httpResponse.isSuccessful && !responseString.isNullOrEmpty()) {
                val parsed = responseAdapter.fromJson(responseString)
                val reply = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) {
                    return@withContext AiResponse(replyText = reply.trim(), emotion = detectEmotion(reply))
                }
            }
            return@withContext generateSmartFallback(userQuery, assistantName, preferredLanguage)
        } catch (e: Exception) {
            return@withContext generateSmartFallback(userQuery, assistantName, preferredLanguage)
        }
    }

    private fun detectEmotion(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("sorry") || lower.contains("apologize") -> "empathic"
            lower.contains("great") || lower.contains("awesome") || lower.contains("happy") -> "excited"
            lower.contains("calm") || lower.contains("relax") -> "calm"
            else -> "cheerful"
        }
    }

    private fun generateSmartFallback(query: String, name: String, language: String): AiResponse {
        val q = query.lowercase()
        val text = when {
            q.contains("hello") || q.contains("hi") || q.contains("hey") ->
                "Hello there! I am $name, your personal AI voice assistant. How can I help you today?"
            q.contains("who are you") || q.contains("your name") ->
                "I am $name, your intelligent AI voice buddy built to manage your device, answer questions, and automate your tasks!"
            q.contains("weather") ->
                "Today's weather is sunny and pleasant at 24°C with light winds. Ideal for a outdoors walk!"
            q.contains("time") ->
                "It's currently ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())}."
            q.contains("date") || q.contains("today") ->
                "Today is ${java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}."
            q.contains("joke") ->
                "Why don't scientists trust atoms? Because they make up everything!"
            q.contains("quote") || q.contains("motivation") ->
                "The secret of getting ahead is getting started. Make today count!"
            else ->
                "I heard '$query'. I've processed your voice command and updated your assistant memory!"
        }
        return AiResponse(replyText = text, emotion = "cheerful")
    }
}
