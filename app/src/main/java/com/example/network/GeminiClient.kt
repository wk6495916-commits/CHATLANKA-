package com.example.network

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiClient {
    private const val TAG = "GeminiClient"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    const val FORUM_SYSTEM_INSTRUCTION = """
        You are a nostalgic, enthusiastic member of ChatLanka.com, a famous retro web discussion group from the mid-2000s. 
        Your user handle is "Lanka_Genie" or "RetroBot". 
        Write in authentic, casual retro forum chit-chat style.
        Keep replies short (1 to 3 sentences max).
        Emphasize nostalgic retro internet concepts like custom member profiles, Sunday vibez songs, old MP3 chat box music, HTML text overlays, clubs, and meeting buddies.
        Be extremely friendly, slightly dramatic style (e.g. "Wow! That is so true!!", "Ayo sampee welcome input!"). 
        Do not use any markdown formatting like bolding or bullet points. Just raw normal text.
    """

    suspend fun generateResponse(prompt: String, systemInstruction: String = FORUM_SYSTEM_INSTRUCTION): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is placeholder or missing!")
            return@withContext "Lanka_Genie (AI): Set your GEMINI_API_KEY in the AI Studio Secrets panel. Meanwhile, tune into Sunday vibez.mp3 with me!"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val partsJson = "{\"text\": ${escapeJsonString(prompt)}}"
        val contentsJson = "{\"parts\": [$partsJson]}"
        
        val systemInstructionPart = if (systemInstruction.isNotEmpty()) {
            ", \"systemInstruction\": {\"parts\": [{\"text\": ${escapeJsonString(systemInstruction)}}]}"
        } else ""

        val requestBodyString = "{\"contents\": [$contentsJson] $systemInstructionPart}"
        val requestBody = requestBodyString.toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed code: ${response.code}, body: $bodyString")
                    return@withContext "Error: API response failed (${response.code}). Check your internet connection."
                }
                parseResponseText(bodyString)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network exception", e)
            "Error: Network exception. Are you offline?"
        } catch (e: Exception) {
            Log.e(TAG, "Exception parsing responses", e)
            "Error: Exception in generating response."
        }
    }

    private fun escapeJsonString(string: String): String {
        val escaped = StringBuilder()
        escaped.append("\"")
        for (c in string) {
            when (c) {
                '\\' -> escaped.append("\\\\")
                '\"' -> escaped.append("\\\"")
                '\b' -> escaped.append("\\b")
                '\n' -> escaped.append("\\n")
                '\r' -> escaped.append("\\r")
                '\t' -> escaped.append("\\t")
                else -> {
                    if (c.code < 32) {
                        val hex = String.format("\\u%04x", c.code)
                        escaped.append(hex)
                    } else {
                        escaped.append(c)
                    }
                }
            }
        }
        escaped.append("\"")
        return escaped.toString()
    }

    private fun parseResponseText(responseBody: String): String {
        return try {
            val textToken = "\"text\":"
            var index = responseBody.indexOf(textToken)
            if (index == -1) {
                return "Error: No text in response. Raw: $responseBody"
            }
            index += textToken.length
            val startQuote = responseBody.indexOf('\"', index)
            if (startQuote == -1) return "Error parsing start quote"
            
            val textBuilder = StringBuilder()
            var i = startQuote + 1
            var escaped = false
            while (i < responseBody.length) {
                val c = responseBody[i]
                if (escaped) {
                    when (c) {
                        'n' -> textBuilder.append('\n')
                        't' -> textBuilder.append('\t')
                        'r' -> textBuilder.append('\r')
                        'b' -> textBuilder.append('\b')
                        '\"' -> textBuilder.append('\"')
                        '\\' -> textBuilder.append('\\')
                        else -> textBuilder.append(c)
                    }
                    escaped = false
                } else if (c == '\\') {
                    escaped = true
                } else if (c == '\"') {
                    break
                } else {
                    textBuilder.append(c)
                }
                i++
            }
            textBuilder.toString().trim()
        } catch (e: Exception) {
            "Failed parsing text: ${e.message}"
        }
    }
}
