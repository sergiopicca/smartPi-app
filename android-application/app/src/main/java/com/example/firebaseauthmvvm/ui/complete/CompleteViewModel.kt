package com.example.firebaseauthmvvm.ui.complete

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firebaseauthmvvm.data.firebase.FirebaseSource
import com.example.firebaseauthmvvm.data.repositories.UserRepository
import com.example.firebaseauthmvvm.models.House
import com.example.firebaseauthmvvm.utils.startLoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CompleteViewModel : ViewModel() {
    private val repository = UserRepository(FirebaseSource())

    // Data holding the values for inserting the new home
    val houseId = MutableLiveData<String>()
    val name = MutableLiveData<String>()
    val addr = MutableLiveData<String>()
    val place = MutableLiveData<String>()
    val lat = MutableLiveData<String>()
    val long = MutableLiveData<String>()
    val tel = MutableLiveData<String>()

    // Current logged user
    private val user by lazy {
        repository.currentUser()
    }


    fun save(view: View) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/owner")
        ref.setValue(houseId.value)
            .addOnSuccessListener {
                Log.d("CompleteActivity", "Finally we saved the user and his house to Firebase Database")
            }

        performHouseRegister(uid, view)

    }

    private fun performHouseRegister(uid: String, view: View){
        // If something goes wrong
        if (name.value.isNullOrEmpty() || addr.value.isNullOrEmpty() || lat.value.isNullOrEmpty() ||
            long.value.isNullOrEmpty() || place.value.isNullOrEmpty()) {
            Toast.makeText(view.context, "Something goes wrong.. :(", Toast.LENGTH_SHORT).show()
            this.logout(view)
        }
        // Otherwise we save the house in the dB
        saveHouseToFirebaseDatabase(uid)
    }

    // Perform the storage of the house in the dB
    private fun saveHouseToFirebaseDatabase(uid: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/houses/${houseId.value}")

        // Instantiate the House
        val house = House(houseId.value!!, name.value!!, addr.value!!,
            lat.value!!, long.value!!, place.value!!, tel.value!!, uid)

        // Register the house in the Realtime database
        ref.setValue(house)
            .addOnSuccessListener {
                Log.d("CompleteActivity", "Finally we saved the house to Firebase Database")
            }
    }

    // Function to perform the logout
    fun logout(view: View){
        repository.logout()
        view.context.startLoginActivity()
    }
}