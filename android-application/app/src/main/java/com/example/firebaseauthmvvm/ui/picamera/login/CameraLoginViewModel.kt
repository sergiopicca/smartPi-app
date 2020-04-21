package com.example.firebaseauthmvvm.ui.picamera.login

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.firebaseauthmvvm.database.SmartPiDatabaseDao
import com.example.firebaseauthmvvm.models.Device
import com.example.firebaseauthmvvm.models.PiCamera
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject

class CameraLoginViewModel(
    val database: SmartPiDatabaseDao,
    application: Application
) : AndroidViewModel(application)  {

    private val context: Context = getApplication<Application>().applicationContext

    var btnSelected: ObservableBoolean? = null
    var urlObeserv: ObservableField<String>? = null
    var nameObserv: ObservableField<String>? = null
    var password: ObservableField<String>? = null
    var piCameraLogin: MutableLiveData<PiCamera>? = null
    var fakeDevice: MutableLiveData<PiCamera>? = null

    lateinit var dataSource: SmartPiDatabaseDao
    lateinit var houseId: String
    lateinit var roomId :String

    // device type
    var deviceType: String = "Camera"

    init {
        btnSelected = ObservableBoolean(false)
        urlObeserv = ObservableField("")
        nameObserv = ObservableField("")
        password = ObservableField("")
        piCameraLogin = MutableLiveData()
        fakeDevice = MutableLiveData()
    }

    fun onUrlChanged(s: CharSequence, start: Int, befor: Int, count: Int) {
        btnSelected?.set(s.toString().isNotEmpty() && password?.get().toString().length >= 6 && nameObserv?.get().toString().isNotEmpty())
    }

    fun onNameChanged(s: CharSequence, start: Int, befor: Int, count: Int) {
        btnSelected?.set(s.toString().isNotEmpty() && password?.get().toString().length >= 6 && urlObeserv?.get().toString().isNotEmpty())
    }

    fun onPasswordChanged(s: CharSequence, start: Int, befor: Int, count: Int) {
        btnSelected?.set(s.toString().length >= 6 && urlObeserv?.get().toString().isNotEmpty() && nameObserv?.get().toString().isNotEmpty())
    }

    fun changeDeviceType(newValue: String) {
        deviceType = newValue
    }

    fun onButtonClicked() {
        if (deviceType == "Camera"){
            createDevice()
            Log.i("CAMERA_CREATE:", "Created device ${deviceType}")
        } else{
            createFakeDevice()
            Log.i("DEVICE_CREATE:", "Created device ${deviceType}")
        }
    }

    private fun createDevice() {
        // Create JSON parameters
        val baseUrl = "http://"+urlObeserv?.get().toString()+":3877/"
        val strPassword = password?.get().toString()
        val deviceName = nameObserv?.get().toString()
        // Create login url
        val url = baseUrl+"signup"
        // LOG
        Log.println(Log.INFO, "BASE_URL_FIELD_VALUE", baseUrl)
        Log.println(Log.INFO, "PASS_FIELD_VALUE", strPassword)
        Log.i("LOGIN_URL", url)
        // Create parameters
        val params = HashMap<String, String>()
        params["password"] = strPassword
        params["house_id"] = houseId
        val jsonParams = JSONObject(params as Map<*, *>)
        // LOG
        Log.println(Log.INFO, "JSON_OBJECT", jsonParams.toString())
        // Instantiate the RequestQueue
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val request = JsonObjectRequest( Request.Method.POST, url, jsonParams,
            Response.Listener { response ->
                // Display token
                val strToken = response["auth-token"].toString()
                Log.println(Log.INFO, "TOKEN", strToken)
                piCameraLogin?.value = PiCamera("SUCCESS", deviceName, baseUrl, strPassword, strToken)

            },
            Response.ErrorListener {
                Log.i("NETWORK_RESPONSE", it.message.toString())
                if(it.message != "java.net.UnknownHostException: Unable to resolve host \"af\": No address associated with hostname"){
                    piCameraLogin?.value = PiCamera("INVALID_CREDENTIAL", "", "", "", "")
                } else {
                    val statusCode = it.networkResponse.statusCode
                    if(statusCode == 401) {
                        val jsonResponse = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        val message = jsonResponse["message"]
                        if (message == "Invalid Credential"){
                            piCameraLogin?.value = PiCamera("INVALID_CREDENTIAL", "", "", "", "")
                        }else{
                            piCameraLogin?.value = PiCamera("MISSING_CREDENTIAL", "", "", "", "")
                        }
                        Log.e("UNSUCCESSFUL_RESPONSE", jsonResponse.toString())

                    }
                }


            }
        )
        // Volley policy policy, only one time to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0,
            1f
        )
        // Add the volley post request to the request queue
        queue.add(request)

    }

    fun getHouseId(strHouseId: String, strRoomId: String) {
        houseId = strHouseId
        roomId = strRoomId
    }

    fun addCamera(device: PiCamera): Device {
        // Get reference to database
        val ref = FirebaseDatabase.getInstance()
            .getReference("/houses/${houseId}/rooms/${roomId}/devices/").push()
        // Create new instance
        val newDevice = Device(ref.key!!, device.deviceName,"camera", device.baseUrl)
        // Add Device in Firebase storage
        ref.setValue(newDevice)
            .addOnSuccessListener {
                Log.i("FIREBASE_ADD_DEVICE", "Device added on firebase")
            }
        return newDevice
    }

    private fun createFakeDevice() {
        val baseUrl = "http://"+urlObeserv?.get().toString()+":3877/"
        val deviceName = nameObserv?.get().toString()
        // Get reference to database
        val ref = FirebaseDatabase.getInstance()
            .getReference("/houses/${houseId}/rooms/${roomId}/devices/").push()
        // Create new instance
        val newDevice = Device(ref.key!!, deviceName,deviceType, baseUrl)
        // Add Device in Firebase storage
        ref.setValue(newDevice)
            .addOnSuccessListener {
                Log.i("FIREBASE_ADD_DEVICE", "Device added on firebase")
            }
        fakeDevice?.value = PiCamera("SUCCESS", deviceName, baseUrl, "", "")
    }
}