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

    // 1. Voice Result ke liye LiveData
    private val _voiceResult = androidx.lifecycle.MutableLiveData<String?>()
    val voiceResult: androidx.lifecycle.LiveData<String?> = _voiceResult

    // 2. Loading state (Optional: taake processing ke waqt user ko pata chale)
    private val _isProcessing = androidx.lifecycle.MutableLiveData<Boolean>()
    val isProcessing: androidx.lifecycle.LiveData<Boolean> = _isProcessing

    init {
        // Start fetching feeds safely
        viewModelScope.launch {
            repository.getResponsesConcurrently()
        }
    }

    /** 🎙️ Voice processing trigger karne ka function */
    fun processVoiceSearch(audioFile: java.io.File) {
        _isProcessing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val textResult = repository.getSpeechToText(audioFile)
            _voiceResult.postValue(textResult)
            _isProcessing.postValue(false)
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

