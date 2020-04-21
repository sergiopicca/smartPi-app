package com.example.firebaseauthmvvm.ui.usage

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firebaseauthmvvm.data.firebase.FirebaseSource
import com.example.firebaseauthmvvm.data.repositories.UserRepository
import com.example.firebaseauthmvvm.utils.startLoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirstUsageViewModel : ViewModel() {
    private val repository = UserRepository(FirebaseSource())
    val _username = MutableLiveData<String>()

    private val user by lazy {
        repository.currentUser()
    }

    init {
        helloMsg()
    }

    // Function to perform the logout
    fun logout(view: View){
        repository.logout()
        view.context.startLoginActivity()
    }

    // Retrieve the username to display
    private fun helloMsg(){
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.e("Error first usage", p0.toString())
            }

            override fun onDataChange(p0: DataSnapshot) {
                _username.value = p0.child("username").value.toString()
            }

        })
    }
}