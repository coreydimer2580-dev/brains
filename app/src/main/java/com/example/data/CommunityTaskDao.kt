package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityTaskDao {
    @Query("SELECT * FROM community_tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<CommunityTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CommunityTaskEntity)

    @Query("DELETE FROM community_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}
