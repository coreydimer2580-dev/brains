package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WellnessViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _wellnessFeedback = MutableStateFlow<String?>(null)
    val wellnessFeedback: StateFlow<String?> = _wellnessFeedback.asStateFlow()

    fun submitWellnessSurvey(mood: String, thoughts: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _wellnessFeedback.value = null
            
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                _wellnessFeedback.value = "Please configure your Gemini API Key in the Settings to enable AI wellness feedback."
                _isLoading.value = false
                return@launch
            }

            val prompt = """
                The user has completed a mental wellness check-in survey.
                They selected their mood as: $mood
                They described their current thoughts/stressors as: "$thoughts"
                
                Please provide a short, highly encouraging, and empathetic response (2-3 sentences). 
                If they are feeling down, suggest a gentle cognitive reframing technique or a simple grounding exercise.
                Important: Speak as a supportive AI companion.
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                systemInstruction = Content(
                    parts = listOf(Part(text = "You are a supportive, empathetic, and gentle AI companion. You must not provide medical diagnoses or replace professional therapy. Keep responses under 3 sentences."))
                )
            )

            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "I'm here for you, but I couldn't generate a response right now. Please take a deep breath and take care of yourself."
                
                _wellnessFeedback.value = responseText
            } catch (e: Exception) {
                _wellnessFeedback.value = "Unable to connect right now. Remember you are not alone. (Error: ${e.message})"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetSurvey() {
        _wellnessFeedback.value = null
    }
}
