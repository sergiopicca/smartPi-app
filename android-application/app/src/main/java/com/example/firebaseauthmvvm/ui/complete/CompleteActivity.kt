package com.example.firebaseauthmvvm.ui.complete

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.databinding.ActivityCompleteBinding
import com.example.firebaseauthmvvm.utils.startLoginActivity
import com.example.firebaseauthmvvm.utils.startSplashActivity
import java.util.*

class CompleteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompleteBinding
    private val completeViewModel: CompleteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_complete)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_complete)
        binding.viewmodel = completeViewModel

        // Assign the values of the data passed with the intent
        completeViewModel.name.value = this.intent.getStringExtra("Name")!!
        completeViewModel.addr.value = this.intent.getStringExtra("Address")!!
        completeViewModel.lat.value = this.intent.getStringExtra("Lat")!!
        completeViewModel.long.value = this.intent.getStringExtra("Long")!!
        completeViewModel.place.value = this.intent.getStringExtra("Place")!!
        completeViewModel.tel.value = this.intent.getStringExtra("Telephone")!!

        // Randomly generated to assign the UID to the house
        completeViewModel.houseId.value = UUID.randomUUID().toString()

        // Save
        completeViewModel.save(binding.root)

        binding.buttonCreateHouse.setOnClickListener {
            //completeViewModel.logout(binding.root)
            startSplashActivity()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        completeViewModel.logout(binding.root)
        startLoginActivity()
        Toast.makeText(binding.root.context, "Logged out", Toast.LENGTH_SHORT).show()
    }

}