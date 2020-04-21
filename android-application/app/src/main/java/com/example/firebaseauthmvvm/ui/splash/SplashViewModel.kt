package com.example.firebaseauthmvvm.ui.splash

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.data.firebase.FirebaseSource
import com.example.firebaseauthmvvm.data.repositories.UserRepository
import com.example.firebaseauthmvvm.database.RoomUser
import com.example.firebaseauthmvvm.database.SmartPiDatabaseDao
import com.example.firebaseauthmvvm.ui.usage.FirstUsageActivity
import com.example.firebaseauthmvvm.ui.auth.LoginActivity
import com.example.firebaseauthmvvm.utils.startHomeActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*

class SplashViewModel(
    val database: SmartPiDatabaseDao,
    application: Application
) : AndroidViewModel(application) {
    private val repository = UserRepository(FirebaseSource())

    val user by lazy {
        repository.currentUser()
    }

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    // Implementation of the read from the Firebase Realtime DB, this function takes
    // as parameter the callback interface, since it behaves asynchronously and we MUST
    // handle the result, by overriding the callback call.
    private fun readData(firebaseCallback: FirebaseCallback ){
        if(user?.uid != null){
            val ref = FirebaseDatabase.getInstance().getReference("/users/${user?.uid}")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    // Get the user from the db
                    val email = p0.child("email").getValue().toString()
                    val username = p0.child("username").getValue().toString()
                    val owner = p0.child("owner").getValue().toString()
                    val guest = p0.child("guest").getValue().toString()
                    var img = ""
                    if(p0.child("profileImageUrl").exists()){
                        // If the profile img is not blank we add it
                        if(p0.child("profileImageUrl").value.toString().isNotBlank()){
                            img = p0.child("profileImageUrl").getValue().toString()
                        }else{
                            // Otherwise we assign the starting profile img
                            img = "https://image.flaticon.com/icons/png/512/149/149071.png"
                        }
                    }

                    val uid = p0.child("uid").getValue().toString()

                    val usr = RoomUser(uid,email,username,owner,guest,img)
                    firebaseCallback.onCallback(usr)
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d("Splash", "Data CANNOT BE LOADED")
                }
            })
        }
        else{
            firebaseCallback.onCallback(null)
        }
    }

    // According to the documentation, the common pattern is the following:
    // we launch a function that runs in the main UI Thread, since the result
    // affects the UI/UX, then we call a suspend function to do the work, in
    // order to not block the UI (bad user experience).

    // The keyword suspend is Kotlin's way of marking a function, or function type,
    // as being available to coroutines.

    private fun storeUser(usr:RoomUser){
        uiScope.launch {
            insert(usr)
        }
    }

    // This fu function is launched in the main thread and it stores the user
    // if it is not in the Room DB
    private suspend fun insert(usr:RoomUser){
        withContext(Dispatchers.IO) {
            if (database.getUser() != null){
                //database.getUser()
                Log.e("SplashViewModel", "The user already exist ${database.getUser()}")
            }
            else{
                Log.d("SplashViewModel", "We actually store the user.")
                database.insertUser(usr)
            }
        }
    }

    // Interface for managing the callbacks
    private interface FirebaseCallback{
        fun onCallback(usr:RoomUser?)
    }

    // This function is called once the activity is destroyed to cancel all
    // coroutines
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    // This function handles the callback by overriding the OnCallback method
    // and stores the user in the RoomDB
    fun verifyUser(activity: SplashActivity){
        readData(object : FirebaseCallback {
            override fun onCallback(usr: RoomUser?) {
                Log.d("SplashViewModel", "The user is " + usr.toString())
                if (usr == null) {
                    // If there is no user, we go back to the login activity
                    // activity.startLoginActivity()
                    val intent = Intent(activity, LoginActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }

                else if(usr.owner.isNullOrBlank() && usr.guest.isNullOrBlank()){
                    // No parameters needed
                    val intent = Intent(activity, FirstUsageActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }

                else{
                    // Store the user, this function will be launch the read in the main thread
                    storeUser(usr)
                    Log.d("SplashViewModel", "User credentials: $usr")

                    // Otherwise we start the home activty
                    activity.startHomeActivity()
                }
            }
        })
    }
}