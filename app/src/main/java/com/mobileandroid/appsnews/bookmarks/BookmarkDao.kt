package com.mobileandroid.appsnews.bookmarks

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Dao



@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     suspend fun insert(bookmark: Bookmark)

    @Query("SELECT * FROM my_bookmarks ORDER BY ID DESC")
     fun getAll(): LiveData<List<Bookmark>>

    @Query("SELECT EXISTS (SELECT 1 FROM my_bookmarks WHERE URL=:url)")
    suspend fun isBookmarked(url: String): Boolean

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)   //  renamed

    @Query("DELETE FROM my_bookmarks WHERE URL = :url")
    suspend fun deleteByUrl(url: String)             //  renamed

    @Query("DELETE FROM my_bookmarks")
     suspend fun deleteAll()
}

