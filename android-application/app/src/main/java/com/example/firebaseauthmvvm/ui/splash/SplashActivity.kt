package com.example.firebaseauthmvvm.ui.splash

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.database.SmartPiDatabase

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val application = requireNotNull(this).application

        val dataSource = SmartPiDatabase.getInstance(application).smartPiDatabaseDao

        val splashModelFactory = SplashViewModelFactory(dataSource, application)

        val splashViewModel =
            ViewModelProviders.of(
                this, splashModelFactory).get(SplashViewModel::class.java)

        splashViewModel.verifyUser(this)
    }
}
