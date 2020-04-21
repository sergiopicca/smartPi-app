package com.example.firebaseauthmvvm.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.ui.auth.*
import com.example.firebaseauthmvvm.ui.complete.CompleteActivity
import com.example.firebaseauthmvvm.ui.existing.ExistingActivity
import com.example.firebaseauthmvvm.ui.home.HomeActivity
import com.example.firebaseauthmvvm.ui.registerHouse.HouseActivity
import com.example.firebaseauthmvvm.ui.splash.SplashActivity
import com.example.firebaseauthmvvm.ui.usage.FirstUsageActivity

fun Context.startHomeActivity() =
    Intent(this, HomeActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
        (this as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        // finish()
    }

fun Context.startSplashActivity() =
    Intent(this, SplashActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
        (this as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

fun Context.startLoginActivity() =
    Intent(this, LoginActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(it)
        (this as Activity).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

fun Context.startSignupActivity() =
    Intent(this, SignupActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(it)
        (this as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

fun Context.startPasswordActivity() =
    Intent(this, PasswordActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(it)
        (this as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

fun Context.startFirstUsageActivityBack() =
    Intent(this, FirstUsageActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(it)
        (this as Activity).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

fun Context.startHouseActivity() =
    Intent(this, HouseActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(it)
        (this as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

fun Context.startExistingActivity() =
    Intent(this, ExistingActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(it)
        (this as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

fun Context.startCompleteActivity(name: String,
                                  address: String, lat: String, long: String, place: String,
                                  telephone: String) =
    Intent(this, CompleteActivity::class.java).also {
        it.putExtra("Name", name)
        it.putExtra("Address", address)
        it.putExtra("Lat", lat)
        it.putExtra("Long", long)
        it.putExtra("Place", place)
        it.putExtra("Telephone", telephone)
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(it)
        (this as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }