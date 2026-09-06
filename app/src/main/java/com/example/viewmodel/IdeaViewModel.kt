package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrainDatabase
import com.example.data.IdeaEntity
import com.example.data.IdeaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IdeaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: IdeaRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allIdeas: StateFlow<List<IdeaEntity>>

    init {
        val ideaDao = BrainDatabase.getInstance(application).ideaDao()
        repository = IdeaRepository(ideaDao)

        allIdeas = _searchQuery.flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allIdeas
            } else {
                repository.searchIdeas(query)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveIdea(content: String) {
        if (content.isNotBlank()) {
            viewModelScope.launch {
                repository.saveIdea(IdeaEntity(content = content.trim()))
            }
        }
    }
}
