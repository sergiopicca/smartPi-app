package com.example.firebaseauthmvvm.ui.recap

import android.app.Application
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.firebaseauthmvvm.data.firebase.FirebaseSource
import com.example.firebaseauthmvvm.data.repositories.UserRepository
import com.example.firebaseauthmvvm.database.RoomUser
import com.example.firebaseauthmvvm.database.SmartPiDatabaseDao
import com.example.firebaseauthmvvm.utils.startLoginActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

class RecapViewModel(
    val database: SmartPiDatabaseDao,
    application: Application
) : AndroidViewModel(application) {
    private val repository = UserRepository(FirebaseSource())
    private val mContext = application.applicationContext

    val user by lazy {
        repository.currentUser()
    }

    // Variable to get the user from the Room DB
    val fetchedUser: MutableLiveData<RoomUser> =  MutableLiveData<RoomUser>()

    // Coroutines stuff
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        initializeUser()
    }

    // Suspend function available in the coroutines
    private suspend fun getUserFromDatabase(): RoomUser? {
        return withContext(Dispatchers.IO) {
            // Retrieve the user from the RoomDB
            var usr = database.getUser()
            usr
        }
    }

    // This function init the user, meaning that it call the main thread the function
    // getUserFromDatabase() in order to get the user from the RoomDB
    private fun initializeUser() {
        uiScope.launch {
            fetchedUser.value = getUserFromDatabase()
            Log.e("User from dialog", fetchedUser.value.toString())
        }
    }

    // Store the image in Firebase storage and then set it as user profile image
    fun storeImg(uri: Uri){
        // Every user's profile image has as path the user's id
        val ref = FirebaseStorage.getInstance().getReference("/profile_pic/${user?.uid}")

        // Delete the previous image
        ref.delete()

        // Store the new image in the Firebase Storage
        ref.putFile(uri).addOnSuccessListener {
            Log.d("RecapMenu", "The image was successfully uploaded ${it.metadata?.path}.")

            // Download the image
            ref.downloadUrl.addOnSuccessListener {
                Log.d("RecapMenu", "The image location is ${it}.")
                val refUsers = FirebaseDatabase.getInstance().getReference("/users/${fetchedUser.value?.uid}/")
                refUsers.child("profileImageUrl").setValue(it.toString())

                // Update the user profile image in the RoomDB
                updateUserImg(it.toString())
            }
        }

    }

    // Suspend function to update the user
    private suspend fun update(user:RoomUser){
        withContext(Dispatchers.IO) {
            database.updateUser(user)
        }
    }

    // Update the user in the DB
    private fun updateUserImg(img:String){
        uiScope.launch {
            val id = fetchedUser.value?.uid!!
            val email = fetchedUser.value?.email!!
            val username = fetchedUser.value?.username!!
            val owner = fetchedUser.value?.owner
            val guest = fetchedUser.value?.guest
            val profileImg = img
            update(RoomUser(id,email,username,owner,guest,profileImg))
            Log.d("Upated User: " , getUserFromDatabase().toString())
            fetchedUser.value = getUserFromDatabase()
        }
    }

    fun onClear(uid:String) {
        uiScope.launch {
            clear(uid)
            //fetchedUser = null
        }
    }

    suspend fun clear(uid:String) {
        withContext(Dispatchers.IO) {
            database.clear(uid)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun logout(view: View){
        onClear(this.user?.uid!!)
        repository.logout()
        view.context.startLoginActivity()
    }
}