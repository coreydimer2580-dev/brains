package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BrainInsightsRepository {
    suspend fun getPersonalizedInsights(workoutHistorySummary: String, brainQuotient: Int): String {
        return withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext "Please configure your Gemini API Key in the Settings / Secrets to unlock personalized live brain insights."
            }

            val prompt = """
                Based on the user's Brain Quotient (BQ) of ${brainQuotient} and their recent workout history:
                $workoutHistorySummary
                
                Provide a short, personalized, encouraging insight (2-3 sentences) about their cognitive performance and recommend which area to focus on next.
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                ),
                systemInstruction = Content(
                    parts = listOf(Part(text = "You are a friendly and expert neuro-cognitive coach."))
                )
            )

            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No insight could be generated at this time."
            } catch (e: Exception) {
                "Unable to fetch live insights: ${e.message}"
            }
        }
    }
}
