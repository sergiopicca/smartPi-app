package com.example.firebaseauthmvvm.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "NO need of you"
    }
    val text: LiveData<String> = _text
}
