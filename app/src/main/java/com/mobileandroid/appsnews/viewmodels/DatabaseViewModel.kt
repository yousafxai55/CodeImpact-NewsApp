//package com.mobileandroid.appsnews.viewmodels
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.viewModelScope
//import com.mobileandroid.appsnews.bookmarks.Bookmark
//import com.mobileandroid.appsnews.repository.DatabaseRepository
//import com.mobileandroid.appsnews.savedsearches.SavedSearches
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class DatabaseViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val repository: DatabaseRepository = DatabaseRepository(application)
//
//    private val bookmarksLiveData: LiveData<List<Bookmark>> = repository.getAllBookmarks()
//    //  Saved Searches LiveData
//    private val savedSearchesLiveData: LiveData<List<SavedSearches>> = repository.getAllRecentSearches()
//
//
//    // Get All Bookmarks
//    fun getAllBookmarks(): LiveData<List<Bookmark>> {
//        return bookmarksLiveData
//    }
//
//    fun addBookmark(bookmark: Bookmark) {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.insertBookmark(bookmark)
//        }
//    }
//
//    fun isNewsBookmarked(url: String, callback: (Boolean) -> Unit) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val isBookmarked = repository.isNewsBookmarked(url)
//            withContext(Dispatchers.Main) {
//                callback(isBookmarked)
//            }
//        }
//    }
//
//    fun deleteBookmark(bookmark: Bookmark) {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.deleteBookmark(bookmark)
//        }
//    }
//
//    fun deleteBookmarkByUrl(url: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.deleteBookmarkByUrl(url)
//        }
//    }
//
//    fun deleteAllBookmarks() {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.deleteAllBookmarks()
//        }
//    }
//
//
//
//
//    //  Get All Saved Searches
//    fun getAllRecentSearches(): LiveData<List<SavedSearches>> {
//        return savedSearchesLiveData
//    }
//
//    fun addRecentSearch(savedSearches: SavedSearches) {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.insertSearch(savedSearches)
//        }
//    }
//
//    fun deleteRecentSearch(savedSearches: SavedSearches) {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.deleteRecentSearch(savedSearches)
//        }
//    }
//
//    fun deleteRecentSearchByName(topic: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.deleteRecentSearchByName(topic)
//        }
//    }
//
//    fun deleteAllRecentSearches() {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.deleteAllRecentSearches()
//        }
//    }
//}


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
