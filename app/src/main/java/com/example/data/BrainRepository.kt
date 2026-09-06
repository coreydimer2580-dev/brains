package com.example.data

import kotlinx.coroutines.flow.Flow

class BrainRepository(private val workoutDao: WorkoutDao) {
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    fun getWorkoutsByType(type: String): Flow<List<WorkoutEntity>> =
        workoutDao.getWorkoutsByType(type)

    fun getRecentWorkouts(limit: Int = 10): Flow<List<WorkoutEntity>> =
        workoutDao.getRecentWorkouts(limit)

    suspend fun recordWorkout(workout: WorkoutEntity): Long =
        workoutDao.insertWorkout(workout)

    suspend fun clearHistory() =
        workoutDao.clearAllWorkouts()
}
