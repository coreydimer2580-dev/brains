package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CognitiveScore(
    val id: String = "",
    val category: String = "",
    val score: Int = 0,
    val timestamp: Long = 0L
)

class UserProgressViewModel : ViewModel() {
    private val _scores = MutableStateFlow<List<CognitiveScore>>(emptyList())
    val scores: StateFlow<List<CognitiveScore>> = _scores.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadScores()
    }

    fun loadScores() {
        try {
            _isLoading.value = true
            val db = Firebase.firestore
            db.collection("cognitive_scores")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        _error.value = "Error loading scores: ${e.message}"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }
                    val newScores = snapshot?.documents?.mapNotNull { it.toObject(CognitiveScore::class.java) } ?: emptyList()
                    _scores.value = newScores
                    _error.value = null
                    _isLoading.value = false
                }
        } catch (e: Exception) {
            _error.value = "Firestore is missing configuration. Please add google-services.json to the workspace to enable cloud sync."
            _isLoading.value = false
        }
    }

    fun addScore(category: String, score: Int) {
        try {
            val db = Firebase.firestore
            val newDoc = db.collection("cognitive_scores").document()
            val entry = CognitiveScore(
                id = newDoc.id,
                category = category,
                score = score,
                timestamp = System.currentTimeMillis()
            )
            newDoc.set(entry).addOnFailureListener { e ->
                _error.value = "Failed to save: ${e.message}"
            }
        } catch (e: Exception) {
            _error.value = "Firestore is missing configuration. Please add google-services.json to the workspace to enable cloud sync."
        }
    }
}
