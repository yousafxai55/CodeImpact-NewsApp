package com.mobileandroid.appsnews.savedsearches

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedSearches::class], version = 1, exportSchema = false)
abstract class SavedSearchesDatabase : RoomDatabase() {

    // ✅ Fully-qualified return type (no ambiguity)
    abstract fun savedSearchesDao(): com.mobileandroid.appsnews.savedsearches.SavedSearchesDao

    companion object {
        @Volatile
        private var INSTANCE: SavedSearchesDatabase? = null

        fun getInstance(context: Context): SavedSearchesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SavedSearchesDatabase::class.java,
                    "saved_searches_db"
                )
                    // .fallbackToDestructiveMigration() // optional (dev)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
