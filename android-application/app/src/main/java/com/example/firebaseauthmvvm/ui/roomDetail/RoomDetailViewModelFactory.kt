package com.example.firebaseauthmvvm.ui.roomDetail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RoomDetailViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoomDetailViewModel::class.java)) {
                return RoomDetailViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}
