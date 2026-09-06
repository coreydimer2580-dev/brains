package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout_sessions WHERE gameType = :gameType ORDER BY timestamp DESC")
    fun getWorkoutsByType(gameType: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentWorkouts(limit: Int): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("DELETE FROM workout_sessions")
    suspend fun clearAllWorkouts()
}
