package com.example.firebaseauthmvvm.ui.home

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.firebaseauthmvvm.data.firebase.FirebaseSource
import com.example.firebaseauthmvvm.data.repositories.UserRepository
import com.example.firebaseauthmvvm.database.RoomUser
import com.example.firebaseauthmvvm.database.SmartPiDatabaseDao
import com.example.firebaseauthmvvm.models.Room
import com.google.firebase.database.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round


class HomeViewModel(
    val database: SmartPiDatabaseDao,
    application: Application
) : AndroidViewModel(application) {
    private val repository = UserRepository(FirebaseSource())
    private val mContext = application.applicationContext

    // Variable holding values about the weather
    // Temperature
    val _tempWeather = MutableLiveData<String>()
    // Description (sunny, cloudy, ...)
    val _descrWeather = MutableLiveData<String>()
    // City
    val _cityWeather = MutableLiveData<String>()
    // Current date
    val _dateWeather = MutableLiveData<String>()
    // Weather icon conditions
    val _iconWeatherConditions = MutableLiveData<String>()

    val user by lazy {
        repository.currentUser()
    }

    // Variable to get the user from the Room DB
    val fetchedUser: MutableLiveData<RoomUser> =  MutableLiveData<RoomUser>()
    // Mutable list to collect the rooms
    val _rooms = MutableLiveData<MutableList<Room>>()

    // Current house id
    val _house = MutableLiveData<String>()

    // Coroutines stuff
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Interface for managing the callbacks to retrieve the rooms
    private interface FirebaseCallbackRooms{
        fun onCallback(l:List<Room>?)
    }

    // Interface for getting the house location
    private interface FirebaseCallbackHouse{
        fun onCallbackHouse(lat:String,lon:String)
    }

    private val _navigateToRoomDetail = MutableLiveData<String>()
    val navigateToRoomDetails
        get() = _navigateToRoomDetail

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

    // This function init the user, meaning that it call the main thread the function
    // getUserFromDatabase() in order to get the user from the RoomDB
    private fun initializeUser() {
        uiScope.launch {
            fetchedUser.value = getUserFromDatabase()
            // Depending if the user is an owner or not we assign a different value
            if(getOwnerHouseFromDb().toString().isBlank()){
                _house.value = getGuestHouseFromDb()
                // Log.d("Assigned guest house", _house.value.toString())
            }
            else{
                _house.value = getOwnerHouseFromDb()
                // Log.d("Assigned owner house", _house.value.toString())
            }
            getRooms()
        }
    }

    // Call Firebase Realtime database to get information about rooms
    private fun getRoomsFromFirebase(f:FirebaseCallbackRooms){
        // Always start with the first house of the list
        val house = _house.value
        // Log.d("House", _house.value.toString())

        val ref = FirebaseDatabase.getInstance().getReference("/houses/${house}/rooms")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                // Get the user from the db
                val retrievedRooms = p0.children
                Log.d("Rooms",retrievedRooms.toString())
                val supportList = mutableListOf<Room>()

                // If there are rooms in the house
                for(roomFirebase in retrievedRooms){
                    // Get the value of the room in Firebase
                    val r = roomFirebase.value

                    // Retrieve the info of the room
                    val roomId = (r as Map<*, *>).get("roomId").toString()
                    val name = (r as Map<*, *>).get("name").toString()

                    val newRoom: Room = Room(roomId, name)
                    supportList.add(newRoom)
                }
                f.onCallback(supportList)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("Splash", "Data CANNOT BE LOADED")
            }
        })
    }

    // Implement callback for rooms
    private fun getRooms(){
        getRoomsFromFirebase(object :FirebaseCallbackRooms{
            override fun onCallback(l: List<Room>?) {
                // Log.e("List of rooms", l.toString())
                _rooms.value = l as MutableList<Room>?
                // Log.e("Mutable rooms", _rooms.value.toString())
            }
        })
    }

    private fun getHouseLocation(f: FirebaseCallbackHouse){
        val house = _house.value
        // Log.d("House", _house.value.toString())

        val ref = FirebaseDatabase.getInstance().getReference("/houses/${house}")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                // Get the latitudo and the longitudo of the current house
                f.onCallbackHouse(p0.child("lat").value.toString(),p0.child("long").value.toString())
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.e("HomeViewModel-line 174", p0.toString())
            }
        })
    }

    // Get the weather and the useful information to display
    fun getWeather(){
        getHouseLocation(object: FirebaseCallbackHouse{
            override fun onCallbackHouse(lat: String, lon: String) {
                callForWeather(lat, lon)
            }
        })
    }

    // Function to get the weather
    private fun callForWeather(lat:String, lon:String){
        val api = "WEATHER_API_KEY"
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&$api"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                // We access to the JSON returned by the url, it has a particular structure
                val mainObject: JSONObject = response.getJSONObject("main")
                // Weather is an array of different elements and it contains the temperature
                // and the description (sunny, cloudy, ...)
                val array: JSONArray = response.getJSONArray("weather")
                val jobject: JSONObject = array.getJSONObject(0)
                // Icon of the weather
                val icon = jobject.get("icon")
                val iconURL = "https://openweathermap.org/img/wn/$icon@2x.png"
                // Temperature
                val temp: String = mainObject.getDouble("temp").toString()
                // Weather description
                val description: String = jobject.getString("description")
                // The city name is an element of the JSON object
                val city: String = response.getString("name")

                // Convert the temperature, the defaul is in Kelvin
                var centigrate: Double = (temp.toDouble() - 273.13)
                centigrate = round(centigrate)
                val intTemp: Int = centigrate.toInt()

                // Change the views
                _tempWeather.value = "$intTemp°"
                _descrWeather.value = description
                _cityWeather.value = city
                _iconWeatherConditions.value = iconURL

                // Get the current day
                val calendar: Calendar = Calendar.getInstance()
                val sdf: SimpleDateFormat = SimpleDateFormat("EEE, MMM dd")
                val formattedDate: String = sdf.format(calendar.time)

                _dateWeather.value = formattedDate
            },
            Response.ErrorListener { error ->
                Log.e("VOLLEY",error.toString())
            }
        )

        val queue: RequestQueue = Volley.newRequestQueue(mContext)
        queue.add(jsonObjectRequest)
    }

    // Handle the delete of a Room
    fun deleteRoom(room: Room){
        // Toast.makeText(mContext, room.toString(), Toast.LENGTH_LONG).show()
        val house = _house.value
        val ref = FirebaseDatabase.getInstance().getReference("/houses/${house}/rooms")
        Log.d("DeleteRoom", room.roomId)
        ref.child(room.roomId).removeValue()
    }

    // Add the room to the house
    fun addRoom(insertedName:String){
        if(insertedName.isNullOrBlank()){
            Toast.makeText(mContext,"The name is empty",Toast.LENGTH_LONG).show()
        }
        else{
            val house = _house.value
            val ref = FirebaseDatabase.getInstance().getReference("/houses/${house}/rooms").push()
            val msg = Room(ref.key!!, insertedName)

            ref.setValue(msg)
                .addOnSuccessListener {
                    Toast.makeText(mContext,"Room added with success!",Toast.LENGTH_LONG).show()
                    // Log.d("HomeViewModel", "New room added: ${ref.key}")
                }
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

}