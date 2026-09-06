package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameType: String, // "MEMORY_GRID", "STROOP_SPEED", "SYNAPSE_MATH", "NEURO_QUIZ"
    val score: Int,
    val accuracy: Float, // 0.0 to 1.0
    val reactionTimeMs: Long, // e.g. 450ms
    val levelReached: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
