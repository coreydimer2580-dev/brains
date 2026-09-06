package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas ORDER BY timestamp DESC")
    fun getAllIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE content LIKE '%' || :searchQuery || '%' ORDER BY timestamp DESC")
    fun searchIdeas(searchQuery: String): Flow<List<IdeaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity)
}
