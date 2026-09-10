package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "racing_results")
data class RacingResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val meetingName: String,
    val raceNumber: Int,
    val raceType: String, // "Thoroughbred", "Greyhound", "Harness"
    val winningNumber: Int,
    val winnerName: String,
    val totalRunners: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class WinnerNumberStats(
    val winningNumber: Int,
    val winCount: Int
)

@Dao
interface RacingResultDao {
    @Query("SELECT * FROM racing_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<RacingResultEntity>>

    @Query("SELECT * FROM racing_results ORDER BY timestamp DESC")
    suspend fun getAllResultsStatic(): List<RacingResultEntity>

    @Query("SELECT * FROM racing_results WHERE raceType = :raceType ORDER BY timestamp DESC")
    fun getResultsByType(raceType: String): Flow<List<RacingResultEntity>>

    @Query("SELECT * FROM racing_results WHERE raceType = :raceType ORDER BY timestamp DESC")
    suspend fun getResultsByTypeStatic(raceType: String): List<RacingResultEntity>

    @Query("SELECT winningNumber, COUNT(*) as winCount FROM racing_results WHERE raceType = :raceType GROUP BY winningNumber ORDER BY winningNumber ASC")
    suspend fun getNumberWinStatsByType(raceType: String): List<WinnerNumberStats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: RacingResultEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<RacingResultEntity>)

    @Query("SELECT COUNT(*) FROM racing_results")
    suspend fun getTotalRacesCount(): Int

    @Query("SELECT COUNT(*) FROM racing_results WHERE raceType = :raceType")
    suspend fun getTotalRacesCount(raceType: String): Int

    @Query("SELECT COUNT(*) FROM racing_results WHERE raceType = :raceType")
    suspend fun getTotalRacesCountByType(raceType: String): Int

    @Query("DELETE FROM racing_results")
    suspend fun clearAllResults()
}
