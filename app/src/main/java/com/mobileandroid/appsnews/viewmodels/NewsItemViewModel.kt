package com.mobileandroid.appsnews.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileandroid.appsnews.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NewsItemViewModel(private val repository: NewsRepository) : ViewModel() {

    val newsLiveData: LiveData<ArrayList<Map<String, String>>> = repository.responseLiveData

    init {
        // Start fetching feeds safely
        viewModelScope.launch {
            repository.getResponsesConcurrently()
        }
    }

    fun loadNextBatch(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.getResponsesConcurrently()
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}



//class NewsItemViewModel(private val repository: NewsRepository) : ViewModel() {
//
//    val newsLiveData: LiveData<ArrayList<Map<String, String>>> = repository.responseLiveData
//
//    init {
//        // Start fetching feeds safely
//        viewModelScope.launch {
//            repository.getResponsesConcurrently()
//        }
//    }
//
//    fun loadNextBatch(onComplete: () -> Unit = {}) {
//        viewModelScope.launch {
//            repository.getResponsesConcurrently()
//            withContext(Dispatchers.Main) {
//                onComplete()
//            }
//        }
//    }
//
//}
