package com.mobileandroid.appsnews.repository

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileandroid.appsnews.viewmodels.DatabaseViewModel

class DatabaseVMFactory (private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatabaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DatabaseViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}