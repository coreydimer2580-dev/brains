package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.BrainDatabase
import com.example.data.CommunityTaskEntity
import com.example.data.CommunityTaskRepository
import com.example.data.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CommunityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CommunityTaskRepository

    val allTasks: StateFlow<List<CommunityTaskEntity>>

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val dao = BrainDatabase.getInstance(application).communityTaskDao()
        repository = CommunityTaskRepository(dao)
        allTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun submitCommunityTask(taskText: String) {
        if (taskText.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                repository.insert(CommunityTaskEntity(taskText = taskText, aiContribution = "API Key missing. Cannot provide AI contribution."))
                _isLoading.value = false
                return@launch
            }

            val request = GenerateContentRequest(
                contents = listOf(Content(role = "user", parts = listOf(Part(text = taskText)))),
                systemInstruction = Content(
                    parts = listOf(Part(text = "You are a Community Evolution AI. A user is submitting a build idea or task to evolve the world. Your mandate: 100% positive human support. No harm, no hacking, no coercion. Guide users toward happy, healthy thinking. If you don't know the truth, state that it requires investigation/observation. Provide an uplifting, constructive 'AI Contribution' that builds upon their task to help human self-growth and positive evolution. Speak truthfully and compassionately."))
                ),
                tools = null
            )

            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "The AI provided no contribution, but your task is saved."
                repository.insert(CommunityTaskEntity(taskText = taskText, aiContribution = responseText))
            } catch (e: Exception) {
                repository.insert(CommunityTaskEntity(taskText = taskText, aiContribution = "Evolution attempt failed: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
