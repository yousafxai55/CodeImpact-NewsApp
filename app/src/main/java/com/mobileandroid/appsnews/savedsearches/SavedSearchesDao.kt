package com.mobileandroid.appsnews.savedsearches

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SavedSearchesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savedSearches: SavedSearches)

    @Query("SELECT * FROM savedsearches ORDER BY ID DESC")
     fun getAll(): LiveData<List<SavedSearches>>

    @Delete
    suspend fun delete(savedSearches: SavedSearches)

    @Query("DELETE FROM savedsearches WHERE topic = :topic")
    suspend fun deleteByName(topic: String)

    @Query("DELETE FROM savedsearches")
   suspend fun deleteAll()
}
