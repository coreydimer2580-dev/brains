package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorkoutEntity::class,
        IdeaEntity::class,
        CommandEntity::class,
        CommunityTaskEntity::class,
        PurchaseEntity::class,
        PayoutDestinationEntity::class,
        PayoutDisbursementEntity::class,
        RacingResultEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class BrainDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun ideaDao(): IdeaDao
    abstract fun commandDao(): CommandDao
    abstract fun communityTaskDao(): CommunityTaskDao
    abstract fun payoutDao(): PayoutDao
    abstract fun racingResultDao(): RacingResultDao

    companion object {
        @Volatile
        private var INSTANCE: BrainDatabase? = null

        fun getInstance(context: Context): BrainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrainDatabase::class.java,
                    "brain_learning_db"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
