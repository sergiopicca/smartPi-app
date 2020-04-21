package com.example.firebaseauthmvvm.ui.edit

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.firebaseauthmvvm.database.RoomUser
import com.example.firebaseauthmvvm.database.SmartPiDatabaseDao
import com.example.firebaseauthmvvm.models.House
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*


class EditViewModel(
    val database: SmartPiDatabaseDao,
    application: Application
) : AndroidViewModel(application){
    // Variable to get the user from the Room DB
    val fetchedUser: MutableLiveData<RoomUser> =  MutableLiveData<RoomUser>()

    // Current house id
    val _house = MutableLiveData<String>()
    // Will contain the list of messages in the home chat
    val _completeHouse = MutableLiveData<MutableList<House>>()
    // Application context
    private val mContext = application.applicationContext

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
            // Log.e("HomeViewModel", usr.toString())
            usr
        }
    }

    // Return the house of which the user is OWNER (if exists)
    private suspend fun getOwnerHouseFromDb():String?{
        return withContext(Dispatchers.IO) {
            // Retrieve the house from the RoomDB
            val house = database.getUserOwnerHouses()
            house
        }
    }

    // Return the house of which the user is GUEST (if exists)
    private suspend fun getGuestHouseFromDb(): String?{
        return withContext(Dispatchers.IO) {
            // Retrieve the house from the RoomDB
            val house = database.getUserGuestHouses()
            house
        }
    }

    private fun initializeUser() {
        uiScope.launch {
            fetchedUser.value = getUserFromDatabase()
            // Depending if the user is an owner or not we assign a different value
            if(getOwnerHouseFromDb().toString().isBlank()){
                _house.value = getGuestHouseFromDb()
                retrieveHouses()
                Log.d("Assigned guest house", _house.value.toString())
            }
            else{
                _house.value = getOwnerHouseFromDb()
                retrieveHouses()
                Log.d("Assigned owner house", _house.value.toString())
            }
        }
    }

    private fun retrieveHouses(){
        val supportList = mutableListOf<House>()
        Log.d("EditViewModel", "HouseID: ${_house.value}")
        val ref = FirebaseDatabase.getInstance().getReference("/houses/${_house.value}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val hid = p0.child("houseuid").value.toString()
                val name = p0.child("name").value.toString()
                val address = p0.child("address").value.toString()
                val lat = p0.child("lat").value.toString()
                val long = p0.child("long").value.toString()
                val place = p0.child("place").value.toString()
                val telephone = p0.child("telephone").value.toString()
                val owner = p0.child("owner").value.toString()
                supportList.add(House(hid, name, address, lat, long, place, telephone, owner))
                _completeHouse.value = supportList
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}