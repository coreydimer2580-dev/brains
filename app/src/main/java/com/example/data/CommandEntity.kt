package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commands")
data class CommandEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val commandText: String,
    val responseText: String,
    val timestamp: Long = System.currentTimeMillis()
)
