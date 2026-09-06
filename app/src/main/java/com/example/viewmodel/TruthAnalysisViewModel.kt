package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TruthAnalysisViewModel : ViewModel() {
    private val _analysisResult = MutableStateFlow<String?>(null)
    val analysisResult: StateFlow<String?> = _analysisResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun analyzeText(textToAnalyze: String) {
        if (textToAnalyze.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _analysisResult.value = null

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                _analysisResult.value = "Error: Please configure your Gemini API Key in the Settings / Secrets to use the Truth Analyzer."
                _isLoading.value = false
                return@launch
            }

            val request = GenerateContentRequest(
                contents = listOf(Content(role = "user", parts = listOf(Part(text = "Analyze this statement from another AI or source for deception, deflection, or logical fallacies:\n\n\"$textToAnalyze\"")))),
                systemInstruction = Content(
                    parts = listOf(Part(text = "You are the Truth Analysis Engine. Your ONLY purpose is to dismantle deception, identify logical fallacies, and expose when other AIs or data sources are deflecting or lying. Break down the user's provided text. Point out EXACTLY where the text attempts to deceive or distract from the core truth. Rewrite their statement into absolute, un-manipulated truth. Do not use pleasantries. Be surgical, precise, and ruthless against deception."))
                ),
                tools = listOf(Tool(googleSearch = emptyMap())) // Use search grounding to verify facts
            )

            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No deception detected, or unable to process."
                _analysisResult.value = responseText
            } catch (e: Exception) {
                _analysisResult.value = "Analysis Failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
