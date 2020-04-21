package com.example.firebaseauthmvvm.ui.usage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.databinding.ActivityFirstUsageBinding
import com.example.firebaseauthmvvm.utils.*
import com.google.firebase.auth.FirebaseAuth

class FirstUsageActivity : AppCompatActivity(){
    private val firstUsageViewModel: FirstUsageViewModel by viewModels()
    private lateinit var binding: ActivityFirstUsageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_first_usage)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_first_usage)

        firstUsageViewModel._username.observe(this, Observer {
            binding.activityHeaderFirstUsageTextView.text = "Hello $it!"
        })

        // Add a new home
        binding.buttonAddNewHouse.setOnClickListener {
            startHouseActivity()
        }

        // Enter in an existing home as a guest
        binding.buttonAddExistingHouse.setOnClickListener {
            startExistingActivity()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Logged with some service
        if(FirebaseAuth.getInstance().currentUser?.uid != null){
            firstUsageViewModel.logout(binding.root)
            startLoginActivity()
            Toast.makeText(binding.root.context, "Logged out", Toast.LENGTH_SHORT).show()
        }else{
            // Came back to the login
            startLoginActivity()
        }
    }
}
