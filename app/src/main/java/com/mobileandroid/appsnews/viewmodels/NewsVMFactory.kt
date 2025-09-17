package com.mobileandroid.appsnews.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileandroid.appsnews.repository.NewsRepository

class NewsVMFactory(var repository: NewsRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        Log.d("NewsVMFactory", "Attempting to create ViewModel for class: ${modelClass.simpleName}")

        return NewsItemViewModel(repository) as T
    }
}