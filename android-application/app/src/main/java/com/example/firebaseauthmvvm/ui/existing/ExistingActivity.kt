package com.example.firebaseauthmvvm.ui.existing

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.databinding.ActivityExistingBinding
import com.example.firebaseauthmvvm.utils.startFirstUsageActivityBack

class ExistingActivity : AppCompatActivity(){
    private lateinit var binding: ActivityExistingBinding
    private val existingViewModel: ExistingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_existing)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_existing)
        binding.viewmodel
        binding.viewmodel = existingViewModel

        val buttonDone: Button = findViewById(R.id.button_existing_house)
        buttonDone.setOnClickListener {
            val existingHouse = binding.edittextExistingHouse.text.toString()
            Log.d("ExistingActivity", "HouseID: $existingHouse")
            if (existingHouse.isNotEmpty()){
                existingViewModel.addGuest(existingHouse, binding.root)
            } else {
                Toast.makeText(baseContext, "Please insert the code",  Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startFirstUsageActivityBack()
    }
}