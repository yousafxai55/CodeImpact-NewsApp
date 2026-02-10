package com.mobileandroid.appsnews.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.mobileandroid.appsnews.bookmarks.Bookmark
import com.mobileandroid.appsnews.repository.DatabaseRepository
import com.mobileandroid.appsnews.savedsearches.SavedSearches
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatabaseViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = DatabaseRepository(application)

    // --- Bookmarks ---
     fun getAllBookmarks(): LiveData<List<Bookmark>> = repo.getAllBookmarks()

    fun addBookmark(bookmark: Bookmark) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.insertBookmark(bookmark)
        }
    }

    fun isNewsBookmarked(url: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val exists = repo.isNewsBookmarked(url)
            withContext(Dispatchers.Main) { callback(exists) }
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteBookmark(bookmark)
        }
    }

    fun deleteBookmarkByUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteBookmarkByUrl(url)
        }
    }

    fun deleteAllBookmarks() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteAllBookmarks()
        }
    }

    // --- Saved Searches ---
    fun getAllRecentSearches(): LiveData<List<SavedSearches>> = repo.getAllRecentSearches()

    fun addRecentSearch(savedSearches: SavedSearches) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.insertSearch(savedSearches)
        }
    }

    fun deleteRecentSearch(savedSearches: SavedSearches) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteRecentSearch(savedSearches)
        }
    }

    fun deleteRecentSearchByName(topic: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteRecentSearchByName(topic)
        }
    }

    fun deleteAllRecentSearches() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteAllRecentSearches()
        }
    }
}
