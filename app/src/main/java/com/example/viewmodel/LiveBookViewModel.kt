package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveBookViewModel : ViewModel() {
    private val _bookContent = MutableStateFlow<String?>(null)
    val bookContent: StateFlow<String?> = _bookContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun generateChapter(topic: String, experienceLevel: String) {
        if (topic.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _bookContent.value = null

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                _bookContent.value = "To awaken the Live Book, please configure your AI Key in the Settings."
                _isLoading.value = false
                return@launch
            }

            val request = GenerateContentRequest(
                contents = listOf(Content(role = "user", parts = listOf(Part(text = "Create a customized learning chapter about: '$topic'. The user describes their current understanding as: '$experienceLevel'.")))),
                systemInstruction = Content(
                    parts = listOf(Part(text = "You are the 'Live Book'—an adaptive, infinitely patient teacher. Your goal is to foster self-growth and empowerment. You must adapt your language to perfectly match the user's stated experience level. If they are a beginner, use simple, accessible analogies. If they are advanced, provide deep, structural insights. Always encourage independent thought, free will, and positive forward momentum. Format the response beautifully like a chapter in a book."))
                ),
                tools = null 
            )

            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "The book is currently blank. Try another topic."
                _bookContent.value = responseText
            } catch (e: Exception) {
                _bookContent.value = "The book could not be written at this moment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
