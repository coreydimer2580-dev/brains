package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {
    @Query("SELECT * FROM commands ORDER BY timestamp DESC")
    fun getAllCommands(): Flow<List<CommandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: CommandEntity)

    @Query("DELETE FROM commands WHERE id = :id")
    suspend fun deleteCommandById(id: Int)
}
