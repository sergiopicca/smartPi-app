package com.example.firebaseauthmvvm.ui.existing

import android.util.Log
import android.util.MutableBoolean
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.firebaseauthmvvm.data.firebase.FirebaseSource
import com.example.firebaseauthmvvm.data.repositories.UserRepository
import com.example.firebaseauthmvvm.utils.startSplashActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExistingViewModel : ViewModel() {
    private val repository = UserRepository(FirebaseSource())

    // Current logged user
    private val user by lazy {
        repository.currentUser()
    }

    fun addGuest(hid: String, view: View) {
        val uid = user?.uid
        val refH = FirebaseDatabase.getInstance().getReference("/houses")
        refH.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild("/$hid")) {
                    val refU = FirebaseDatabase.getInstance().getReference("/users/$uid/guest")
                    refU.setValue(hid)
                        .addOnSuccessListener {
                            Log.d("ExistingActivity", "User: $uid is added in $hid")
                            Toast.makeText(view.context, "You are added in this house", Toast.LENGTH_SHORT).show()
                            view.context.startSplashActivity()
                        }
                } else {
                    Log.d("ExistingActivity", "House id wrong: $hid")
                    Toast.makeText(view.context, "This code seems to not exist", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}
