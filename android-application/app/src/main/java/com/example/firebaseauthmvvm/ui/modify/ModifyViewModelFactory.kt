package com.example.firebaseauthmvvm.ui.modify

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ModifyViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModifyViewModel::class.java)) {
            return ModifyViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}