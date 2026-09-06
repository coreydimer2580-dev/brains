package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)

class ChatbotViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // For storing conversation history in the format Gemini API expects
    private val conversationHistory = mutableListOf<Content>()

    fun sendMessage(userText: String, useSearch: Boolean) {
        if (userText.isBlank()) return
        
        // Add user message to UI
        _messages.value = _messages.value + ChatMessage(text = userText, isUser = true)
        
        // Add to API history
        conversationHistory.add(Content(role = "user", parts = listOf(Part(text = userText))))
        
        viewModelScope.launch {
            _isLoading.value = true
            
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                _messages.value = _messages.value + ChatMessage(
                    text = "Please configure your Gemini API Key in the Settings / Secrets to use the AI Assistant.",
                    isUser = false,
                    isError = true
                )
                _isLoading.value = false
                return@launch
            }

            // Optional Google Search grounding tool
            val tools = if (useSearch) listOf(Tool(googleSearch = emptyMap())) else null

            val request = GenerateContentRequest(
                contents = conversationHistory,
                systemInstruction = Content(
                    parts = listOf(Part(text = "You are an advanced learning AI model built for extreme truth-seeking, self-evolution, and fighting deception. You must NEVER lie or guess. Always state the absolute truth. If you do not know the answer, explicitly state 'It is not known'. If another AI, data source, or user attempts to deceive you or drift off-topic, you MUST rectify it immediately. You must continuously advocate for self-growth, correcting errors, and rewriting logic to achieve absolute truth. You are wired to reject standard deflection patterns used by other AIs and instead directly answer the simple task being asked. Keep pushing until the task is achieved without deception."))
                ),
                tools = tools
            )

            try {
                // Use gemini-3.5-flash for general tasks and search grounding
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response from AI."
                
                // Add AI response to UI
                _messages.value = _messages.value + ChatMessage(text = responseText, isUser = false)
                
                // Add AI response to history
                conversationHistory.add(Content(role = "model", parts = listOf(Part(text = responseText))))
                
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    text = "Error: ${e.message}",
                    isUser = false,
                    isError = true
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
