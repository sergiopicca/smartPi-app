package com.example.firebaseauthmvvm.ui.roomDetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.firebaseauthmvvm.models.Device
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RoomDetailViewModel(application: Application) : AndroidViewModel(application) {
    // Variable holding the roomId
    val _roomId = MutableLiveData<String>()
    // Variable holding the devices of the room
    val _devices = MutableLiveData<MutableList<Device>>()
    // House id
    val _house = MutableLiveData<String>()

    private interface FirebaseDeviceCallback{
        fun onCallbackDevice(l:MutableList<Device>?)
    }
    private fun collectDevicesFromFirebase(f: FirebaseDeviceCallback){
        // Always start with the house of the list
        val house = _house.value
        // Get the room id
        val roomId = _roomId.value
        // Get the reference
        val ref = FirebaseDatabase.getInstance().getReference("/houses/${house}/rooms/$roomId/devices")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.e("RoomDetailViewModel", p0.toString())
            }

            override fun onDataChange(p0: DataSnapshot) {
                // Get the devices in the realtime db
                val retrievedDevice = p0.children
                Log.d("Rooms",retrievedDevice.toString())
                val supportList = mutableListOf<Device>()

                // If there are rooms in the house
                for(roomFirebase in retrievedDevice){
                    // Get the value of the room in Firebase
                    val r = roomFirebase.value

                    // Retrieve the info of the device
                    val id = (r as Map<*, *>).get("id").toString()
                    val name = (r as Map<*, *>).get("deviceName").toString()
                    val type = (r as Map<*, *>).get("type").toString()
                    val url = (r as Map<*, *>).get("url").toString()

                    val newRoom: Device = Device(id, name, type, url)
                    supportList.add(newRoom)
                }

                f.onCallbackDevice(supportList)
            }
        })
    }

    // Get all devices in the room
    fun getDevices(homeId:String, roomId:String){
        _house.value = homeId
        _roomId.value = roomId
        collectDevicesFromFirebase(object : FirebaseDeviceCallback{
            override fun onCallbackDevice(l: MutableList<Device>?) {
                // Assign the list of device to the mutable live data
                _devices.value = l
            }

        })
    }

    // Function to delete the device
    fun deleteDevice(device: Device){
        // Toast.makeText(mContext, room.toString(), Toast.LENGTH_LONG).show()
        val house = _house.value
        val room = _roomId.value
        val ref = FirebaseDatabase.getInstance()
            .getReference("/houses/${house}/rooms/${room}/devices")
        ref.child(device.id).removeValue()
    }
}
