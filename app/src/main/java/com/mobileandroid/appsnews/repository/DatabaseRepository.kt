//package com.mobileandroid.appsnews.repository
//
//import android.app.Application
//import androidx.lifecycle.LiveData
//import com.mobileandroid.appsnews.bookmarks.Bookmark
//import com.mobileandroid.appsnews.bookmarks.BookmarkDao
//import com.mobileandroid.appsnews.bookmarks.BookmarksDatabase
//import com.mobileandroid.appsnews.savedsearches.SavedSearches
//import com.mobileandroid.appsnews.savedsearches.SavedSearchesDao
//import com.mobileandroid.appsnews.savedsearches.SavedSearchesDatabase
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//class DatabaseRepository(application: Application) {
//
//    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
//
//
//    private val bookmarkDao: BookmarkDao
//    private val bookmarksLiveData: LiveData<List<Bookmark>>
//
//    //  Saved Searches Database Setup
//    private val savedSearchesDao: SavedSearchesDao
//    private val savedSearchesLiveData: LiveData<List<SavedSearches>>
//
//
//
//
//    init {
//        val bookmarksDatabase: BookmarksDatabase = BookmarksDatabase.getInstance(application)
//        bookmarkDao = bookmarksDatabase.getBookmarksDao()
//        bookmarksLiveData = bookmarkDao.getAll()
//
//        val savedSearchesDatabase: SavedSearchesDatabase = SavedSearchesDatabase.getInstance(application)
//        savedSearchesDao = savedSearchesDatabase.savedSearchesDao()
//        savedSearchesLiveData = savedSearchesDao.getAll()
//    }
//
//    //  Insert Bookmark
////    fun insertBookmark(bookmark: Bookmark) {
////        executorService.execute { bookmarkDao.insert(bookmark) }
////    }
//
//    fun insertBookmark(bookmark: Bookmark) {
//        CoroutineScope(Dispatchers.IO).
//        launch {
//            bookmarkDao.insert(bookmark)
//        }
//    }
//
//    //  Get All Bookmarks
//    fun getAllBookmarks(): LiveData<List<Bookmark>> {
//        return bookmarksLiveData
//    }
//
//    //  Check if News is Bookmarked
//    suspend fun isNewsBookmarked(url: String): Boolean {
//        return bookmarkDao.isBookmarked(url)
//    }
//
//    // Delete Bookmark by Object
////    fun deleteBookmark(bookmark: Bookmark) {
////        executorService.execute { bookmarkDao.delete(bookmark) }
////    }
//
//    fun deleteBookmark(bookmark: Bookmark) {
//        CoroutineScope(Dispatchers.IO).launch {
//            bookmarkDao.deleteBookmark(bookmark)
//        }
//    }
//
//
//    //  Delete Bookmark by URL
////    fun deleteBookmarkByUrl(url: String) {
////        executorService.execute { bookmarkDao.delete(url) }
////    }
//
//    fun deleteBookmarkByUrl(url: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            bookmarkDao.deleteByUrl(url)
//        }
//    }
//
//    //  Delete All Bookmarks
////    fun deleteAllBookmarks() {
////        executorService.execute { bookmarkDao.deleteAll() }
////    }
//
//    fun deleteAllBookmarks() {
//        CoroutineScope(Dispatchers.IO).launch {
//            bookmarkDao.deleteAll()
//        }
//    }
//
//
//    //  Insert Saved Search
//    fun insertSearch(savedSearches: SavedSearches) {
//        executorService.execute { savedSearchesDao.insert(savedSearches) }
//    }
//
//    //  Get All Saved Searches
//    fun getAllRecentSearches(): LiveData<List<SavedSearches>> {
//        return savedSearchesLiveData
//    }
//
//    //  Delete a specific search by object
//    fun deleteRecentSearch(savedSearches: SavedSearches) {
//        executorService.execute { savedSearchesDao.delete(savedSearches) }
//    }
//
//    //  Delete a specific search by topic
//    fun deleteRecentSearchByName(topic: String) {
//        executorService.execute { savedSearchesDao.deleteByName(topic) }
//    }
//
//    //  Delete All Saved Searches
//    fun deleteAllRecentSearches() {
//        executorService.execute { savedSearchesDao.deleteAll() }
//    }
//}



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
