package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_tasks")
data class CommunityTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskText: String,
    val aiContribution: String,
    val timestamp: Long = System.currentTimeMillis()
)
