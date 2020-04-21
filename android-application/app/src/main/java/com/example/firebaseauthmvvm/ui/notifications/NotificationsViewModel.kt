package com.example.firebaseauthmvvm.ui.notifications

import android.app.Application
import android.app.NotificationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.firebaseauthmvvm.data.firebase.FirebaseSource
import com.example.firebaseauthmvvm.data.repositories.UserRepository
import com.example.firebaseauthmvvm.database.SmartPiDatabaseDao
import com.example.firebaseauthmvvm.models.ChatMessage
import com.example.firebaseauthmvvm.models.Message
import com.example.firebaseauthmvvm.models.User
import com.google.firebase.database.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class NotificationsViewModel(
    val database: SmartPiDatabaseDao,
    val app: Application
) : AndroidViewModel(app) {

    private val repository = UserRepository(FirebaseSource())
    // Get the user id
    val currentUserId by lazy {
        repository.currentUser()?.uid
    }
    val _house = MutableLiveData<String>()

    // Will contain the list of messages in the home chat
    val _completeMessages = MutableLiveData<MutableList<Message>>()
    // List of users of the current house
    val _users =  MutableLiveData<MutableMap<String,User>>()

    // Coroutines stuff
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Interface for managing the callbacks to retrieve the rooms
    private interface FirebaseCallbackMsg{
        fun onCallbackMsg(m:ChatMessage)
    }

    private interface FirebaseCallbackUsers{
        fun onCallbackUsers(u:MutableMap<String,User>)
    }

    init {
        initChat()
    }

    private fun initChat() {
        uiScope.launch {
            // Depending if the user is an owner or not we assign a different value
            if(getOwnerHouseFromDb().toString().isBlank()){
                _house.value = getGuestHouseFromDb()
                Log.d("Assigned guest house", _house.value.toString())
            }
            else{
                _house.value = getOwnerHouseFromDb()
                Log.d("Assigned owner house", _house.value.toString())
            }
            collectUser()
            readMsg()
        }
    }

    // Return the house of which the user is OWNER (if exists)
    private suspend fun getOwnerHouseFromDb(): String?{
        return withContext(Dispatchers.IO) {
            // Retrieve the house from the RoomDB
            var house = database.getUserOwnerHouses()
            Log.d("OwnerHouse", house.toString())
            house
        }
    }

    // Return the house of which the user is GUEST (if exists)
    private suspend fun getGuestHouseFromDb(): String?{
        return withContext(Dispatchers.IO) {
            // Retrieve the house from the RoomDB
            var house = database.getUserGuestHouses()
            Log.d("OwnerHouse", house.toString())
            house
        }
    }


    // Retrieve the users of the house
    private fun getUsers(f:FirebaseCallbackUsers){
        // Support list to store temporary users
        val mMapOfUsers = mutableMapOf<String,User>()

        // Always start from the first house of the list
        // Id of the first house
        val house = _house.value

        val refUsers = FirebaseDatabase.getInstance().getReference("/users")
        refUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val retrievedUsers = p0.children

                for(user in retrievedUsers!!){
                    val mUser = user.value as HashMap<*,*>
                    if(mUser.get("owner").toString() == house || mUser.get("guest").toString() == house){
                        // Collect users info
                        val userId = mUser.get("uid").toString()
                        val name = mUser.get("username").toString()
                        val img = mUser.get("profileImageUrl").toString()
                        // mMapOfUsers.putIfAbsent(userId,User(userId,name,img))
                        // Log.d("mUser",mUser.toString())
                        // Supported version for API < 26
                        mMapOfUsers[userId] = User(userId,name,img)
                    }
                }
                // Log.d("MapOfUsers",mMapOfUsers.toString())
                f.onCallbackUsers(mMapOfUsers)
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.e("NotificationViewModel", "Cannot get users $p0")
            }
        })
    }

    private fun collectUser(){
        getUsers(object:FirebaseCallbackUsers{
            override fun onCallbackUsers(u: MutableMap<String,User>) {
                _users.value = u
                Log.d("Users",_users.value.toString())
            }
        })
    }

    // Call Firebase Realtime database to get information about messages
    private fun getMsgFromFirebase(f:FirebaseCallbackMsg){
        // Id of the first house
        val mHouse = _house.value
        val mPath = "house-messages"

        val refMsg = FirebaseDatabase.getInstance().getReference("/${mPath}/${mHouse}/")
        refMsg.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                // Get the messages from the realtime db
                val newMsg = p0.getValue(ChatMessage::class.java)
                if(newMsg != null){
                    // Log.d("New Msg", newMsg.toString())
                    f.onCallbackMsg(newMsg)
                }
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("NotificationViewModel", "Cannot get msg $p0")
            }
        })
    }

    // read the Messages
    private fun readMsg(){
        val supportList = mutableListOf<Message>()
        getMsgFromFirebase(object : FirebaseCallbackMsg{
            override fun onCallbackMsg(m:ChatMessage) {
                val usr = _users.value?.get(m.fromId)!!
                if(usr.uid == currentUserId){
                    supportList.add(Message(m.id,m.text,usr.username,usr.profileImageUrl,true, m.timestamp))
                    _completeMessages.value = supportList

                }else{
                    supportList.add(Message(m.id,m.text,usr.username,usr.profileImageUrl,false, m.timestamp))
                    _completeMessages.value = supportList
                }
            }
        })
    }

    // Perform the send of the message
    fun send(text:String){
        // Id of the first house
        val mHouse = _house.value
        val mPath = "house-messages"
        if (text.length == 0) return
        else{
            // Time when the user perfrom the sending
            val currentTime = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("h:mm a, d MMM")
            val formattedTime = dateFormat.format(currentTime).toString()

            val refMsg = FirebaseDatabase.getInstance().getReference("/${mPath}/${mHouse}").push()
            val msg = ChatMessage(refMsg.key!!, text, currentUserId!!, formattedTime)

            refMsg.setValue(msg)
                .addOnSuccessListener {
                    Log.d("NotificationViewModel", "Saved our chat message: ${refMsg.key}")
                }
        }
    }
}
