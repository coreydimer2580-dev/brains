package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WorkoutEntity::class, IdeaEntity::class, CommandEntity::class, CommunityTaskEntity::class], version = 4, exportSchema = false)
abstract class BrainDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun ideaDao(): IdeaDao
    abstract fun commandDao(): CommandDao
    abstract fun communityTaskDao(): CommunityTaskDao

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
