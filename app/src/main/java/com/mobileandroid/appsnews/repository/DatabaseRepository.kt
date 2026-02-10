package com.mobileandroid.appsnews.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.mobileandroid.appsnews.bookmarks.Bookmark
import com.mobileandroid.appsnews.bookmarks.BookmarksDatabase
import com.mobileandroid.appsnews.savedsearches.SavedSearches
import com.mobileandroid.appsnews.savedsearches.SavedSearchesDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseRepository(private val application: Application) {

    // --- Bookmarks (lazy) ---
    private val bookmarksDb by lazy { BookmarksDatabase.getInstance(application) }
    private val bookmarkDao by lazy { bookmarksDb.getBookmarksDao() }

     fun getAllBookmarks(): LiveData<List<Bookmark>> = bookmarkDao.getAll()

    suspend fun insertBookmark(bookmark: Bookmark) = withContext(Dispatchers.IO) {
        bookmarkDao.insert(bookmark)
    }

    suspend fun isNewsBookmarked(url: String): Boolean = withContext(Dispatchers.IO) {
        bookmarkDao.isBookmarked(url)
    }

    suspend fun deleteBookmark(bookmark: Bookmark) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    suspend fun deleteBookmarkByUrl(url: String) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteByUrl(url)
    }

    suspend fun deleteAllBookmarks() = withContext(Dispatchers.IO) {
        bookmarkDao.deleteAll()
    }

    // --- Saved Searches (lazy; access only if used anywhere) ---
    private val savedDb by lazy { SavedSearchesDatabase.getInstance(application) }
    private val savedDao by lazy { savedDb.savedSearchesDao() }

     fun getAllRecentSearches(): LiveData<List<SavedSearches>> = savedDao.getAll()

    suspend fun insertSearch(s: SavedSearches) = withContext(Dispatchers.IO) {
        savedDao.insert(s)
    }

    suspend fun deleteRecentSearch(s: SavedSearches) = withContext(Dispatchers.IO) {
        savedDao.delete(s)
    }

    suspend fun deleteRecentSearchByName(topic: String) = withContext(Dispatchers.IO) {
        savedDao.deleteByName(topic)
    }

    suspend fun deleteAllRecentSearches() = withContext(Dispatchers.IO) {
        savedDao.deleteAll()
    }
}
