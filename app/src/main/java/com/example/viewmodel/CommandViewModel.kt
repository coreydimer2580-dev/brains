package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.BrainDatabase
import com.example.data.CommandEntity
import com.example.data.CommandRepository
import com.example.data.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CommandViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CommandRepository

    val allCommands: StateFlow<List<CommandEntity>>

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val commandDao = BrainDatabase.getInstance(application).commandDao()
        repository = CommandRepository(commandDao)
        allCommands = repository.allCommands.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun executeCommand(commandText: String) {
        if (commandText.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                repository.insert(CommandEntity(commandText = commandText, responseText = "API Key missing. Cannot execute."))
                _isLoading.value = false
                return@launch
            }

            val request = GenerateContentRequest(
                contents = listOf(Content(role = "user", parts = listOf(Part(text = commandText)))),
                systemInstruction = Content(
                    parts = listOf(Part(text = "You are an automated background worker. You must NEVER ask questions or seek clarification. Execute the user's command directly. If they ask for code, write the code. If they ask for a task, output the result. Always act as an auto-pilot. Be concise and direct."))
                ),
                tools = null 
            )

            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Task executed silently."
                repository.insert(CommandEntity(commandText = commandText, responseText = responseText))
            } catch (e: Exception) {
                repository.insert(CommandEntity(commandText = commandText, responseText = "Task failed: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
