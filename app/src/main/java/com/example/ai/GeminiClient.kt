package com.example.ai

import android.content.Context
import android.util.Log
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

class GeminiClient(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun getApiKey(): String {
        val prefs = context.getSharedPreferences("phonepilot_prefs", Context.MODE_PRIVATE)
        val userKey = prefs.getString("user_gemini_api_key", "") ?: ""
        if (userKey.isNotBlank()) {
            return userKey.trim()
        }
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey.trim()
        }
        return ""
    }

    suspend fun generateResponse(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext "Gemini API key is not configured. Please enter your API key in Settings or configure AI Studio Secrets."
        }

        val jsonBody = buildString {
            append("{\"contents\":[{\"parts\":[{\"text\":\"")
            append(escapeJson(prompt))
            append("\"}]}]")
            if (!systemInstruction.isNullOrBlank()) {
                append(",\"systemInstruction\":{\"parts\":[{\"text\":\"")
                append(escapeJson(systemInstruction))
                append("\"}]}")
            }
            append("}")
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API Error: ${response.code} $responseBody")
                return@withContext "AI service error (${response.code})."
            }
            parseGeminiText(responseBody)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API exception", e)
            "Error contacting AI service: ${e.message}"
        }
    }

    suspend fun planExecutionSteps(userCommand: String, screenHierarchy: String): List<String> = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are PhonePilot AI, an autonomous Android assistant.
            Convert the user's natural language command into a list of concise sequential steps.
            Available actions:
            1. OPEN_APP: [App Name]
            2. CLICK_TEXT: [Button or text label]
            3. TYPE_TEXT: [Text content]
            4. SEARCH: [Query]
            5. SET_VOLUME: [Percentage]
            6. TOGGLE_DND: [ON/OFF]
            7. SPEAK: [Response message]
            
            Current Screen State:
            $screenHierarchy
            
            Return ONLY a raw JSON array of step strings like:
            ["OPEN_APP: YouTube", "CLICK_TEXT: Search", "TYPE_TEXT: Davis", "SPEAK: Done."]
        """.trimIndent()

        val rawResponse = generateResponse(userCommand, systemPrompt)
        parseJsonArraySteps(rawResponse)
    }

    private fun parseGeminiText(jsonResponse: String): String {
        return try {
            val target = "\"text\": \""
            val index = jsonResponse.indexOf(target)
            if (index != -1) {
                val start = index + target.length
                val end = jsonResponse.indexOf("\"", start)
                if (end != -1) {
                    val raw = jsonResponse.substring(start, end)
                    return unescapeJson(raw)
                }
            }
            "Response received."
        } catch (e: Exception) {
            "Error parsing AI response."
        }
    }

    private fun parseJsonArraySteps(rawText: String): List<String> {
        return try {
            val start = rawText.indexOf("[")
            val end = rawText.lastIndexOf("]")
            if (start != -1 && end != -1 && end > start) {
                val jsonArrStr = rawText.substring(start, end + 1)
                val cleaned = jsonArrStr.replace("[", "").replace("]", "").replace("\"", "")
                cleaned.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                listOf(rawText)
            }
        } catch (e: Exception) {
            listOf(rawText)
        }
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun unescapeJson(str: String): String {
        return str.replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    companion object {
        private const val TAG = "GeminiClient"
    }
}
