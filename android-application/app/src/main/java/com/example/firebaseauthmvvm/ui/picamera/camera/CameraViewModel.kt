package com.example.firebaseauthmvvm.ui.picamera.camera

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.firebaseauthmvvm.database.RoomDevice
import com.example.firebaseauthmvvm.database.SmartPiDatabaseDao
import com.example.firebaseauthmvvm.models.Device
import com.example.firebaseauthmvvm.models.NetworkResponse
import com.example.firebaseauthmvvm.models.PiCamera
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject

class CameraViewModel(
    val database: SmartPiDatabaseDao,
    application: Application
) : AndroidViewModel(application)  {

    private val context: Context = getApplication<Application>().applicationContext
    private var token: String?=null
    private var baseUrl: String?=null
    private var password: String?=null
    private lateinit var deviceName :String
    private lateinit var deviceId :String
    private lateinit var houseId: String
    private lateinit var roomId: String
    // APIs response management
    var successfulSnapshot: MutableLiveData<NetworkResponse>?=null
    var successfulRetryLogin: MutableLiveData<NetworkResponse>?=null
    var successfulAngle: MutableLiveData<NetworkResponse>?=null
    var successfulMotion: MutableLiveData<NetworkResponse>?=null
    var successfulFace: MutableLiveData<NetworkResponse>?=null
    // Error message
    private val connectionErrorMessage :String = "java.net.ConnectException: Failed to connect to"
    var connectionError: MutableLiveData<NetworkResponse>?= null
    var availability: MutableLiveData<NetworkResponse>?= null
    // Device available
    var firebaseDevice: MutableLiveData<PiCamera>?= null

    init {
        successfulSnapshot = MutableLiveData()
        successfulRetryLogin = MutableLiveData()
        successfulAngle = MutableLiveData()
        successfulMotion = MutableLiveData()
        successfulFace = MutableLiveData()
        connectionError = MutableLiveData()
        availability = MutableLiveData()
        firebaseDevice = MutableLiveData()
    }

    fun getFirebaseParams(strHouseId: String, strRoomId: String, strDeviceId: String) {
        houseId = strHouseId
        deviceId = strDeviceId
        roomId = strRoomId
    }

    fun getParams(strDeviceName: String?, strBaseUrl: String?,
                  strPassword: String?, strToken: String?
                  ) {
        token = strToken
        baseUrl = strBaseUrl
        password = strPassword
        deviceName = strDeviceName.toString()
    }

    fun getSnapshot() {
        var url = baseUrl+"snapshot"
        // Instantiate the RequestQueue
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val request = object: JsonObjectRequest( Method.GET, url, null,
            Response.Listener { response ->
                Log.i("SUCCESSFUL_RESPONSE", response["message"].toString())
                // Get network JSON response
                successfulSnapshot?.value = NetworkResponse(response["status"].toString(), response["message"].toString())
            },
            Response.ErrorListener {
                // Get network response
                Log.i("NETWORK_RESPONSE", it.message.toString())
                val msg = it.message.toString()
                if(msg.contains(connectionErrorMessage)) {
                    connectionError?.value = NetworkResponse("UNAVAILABLE", "The device is probably off")
                }
                else {
                    val statusCode = it.networkResponse.statusCode
                    if (statusCode == 401) {
                        val response = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        if (response["message"] == "token expired") {
                            // get new token
                            getNewToken()
                            // Retry snapshot
                            getSnapshot()
                        } else {
                            successfulSnapshot?.value = NetworkResponse(
                                response["status"].toString(),
                                response["message"].toString()
                            )
                        }
                    } else {
                        successfulSnapshot?.value = NetworkResponse("error", "Internal error")
                    }
                }
            }
        )
        // Custom header with token
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // headers["Authorization"] = "token.toString()"
                headers["Authorization"] = token.toString()
                return headers
            }
        }

        // Volley policy policy, only one time to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0,
            1f
        )

        // Add the volley post request to the request queue
        queue.add(request)
    }

    fun setAngle(angle: Int) {
        var url = baseUrl+"angle?angle="+angle.toString()
        // Instantiate the RequestQueue
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val request = object: JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener { response ->
                Log.i("SUCCESSFUL_RESPONSE", response["message"].toString())
                // Get network JSON response
                successfulAngle?.value = NetworkResponse(response["status"].toString(), response["message"].toString())
            },
            Response.ErrorListener {
                // Get network response
                Log.i("NETWORK_RESPONSE", it.message.toString())
                val msg = it.message.toString()
                if(msg.contains(connectionErrorMessage)) {
                    connectionError?.value = NetworkResponse("UNAVAILABLE", "The device is probably off")
                }
                else {
                    val statusCode = it.networkResponse.statusCode
                    if (statusCode == 401) {
                        val response = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        if (response["message"] == "token expired") {
                            // get new token
                            getNewToken()
                            // Retry snapshot
                            setAngle(angle)
                        } else {
                            successfulAngle?.value = NetworkResponse(
                                response["status"].toString(),
                                response["message"].toString()
                            )
                        }
                    } else {
                        Log.e("STATUS_CODE", statusCode.toString())
                        successfulAngle?.value =
                            NetworkResponse("error", "PWM channel already occupied.")
                    }
                }
            }
        )
        // Custom header with token
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // headers["Authorization"] = "token.toString()"
                headers["Authorization"] = token.toString()
                return headers
            }
        }

        // Volley policy policy, only one time to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0,
            1f
        )

        // Add the volley post request to the request queue
        queue.add(request)
    }

    fun getAngle() {
        var url = baseUrl+"angle"
        // Instantiate the RequestQueue
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val request = object: JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener { response ->
                Log.i("SUCCESSFUL_RESPONSE", response["message"].toString())
                // Get network JSON response
                successfulAngle?.value = NetworkResponse(response["status"].toString(), response["message"].toString())
            },
            Response.ErrorListener {
                // Get network response
                Log.i("NETWORK_RESPONSE", it.message.toString())
                val msg = it.message.toString()
                if(msg.contains(connectionErrorMessage)) {
                    connectionError?.value = NetworkResponse("UNAVAILABLE", "The device is probably off")
                }
                else {
                    val statusCode = it.networkResponse.statusCode
                    if (statusCode == 401) {
                        val response = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        if (response["message"] == "token expired") {
                            // get new token
                            getNewToken()
                            // Retry snapshot
                            getAngle()
                        } else {
                            successfulAngle?.value = NetworkResponse(
                                response["status"].toString(),
                                response["message"].toString()
                            )
                        }
                    } else {
                        successfulAngle?.value = NetworkResponse("error", "Internal error")
                    }
                }
            }
        )
        // Custom header with token
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // headers["Authorization"] = "token.toString()"
                headers["Authorization"] = token.toString()
                return headers
            }
        }
        // Volley policy policy, only one time to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0,
            1f
        )
        // Add the volley post request to the request queue
        queue.add(request)
    }

    fun setMotion(status: Boolean) {
        var url = if (status){
            baseUrl+"motion?motion=1"
        } else {
            baseUrl+"motion?motion=0"
        }
        // Instantiate the RequestQueue
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val request = object: JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener { response ->
                Log.i("SUCCESSFUL_RESPONSE", response["message"].toString())
                // Get network JSON response
                successfulMotion?.value = NetworkResponse(response["status"].toString(), response["message"].toString())
            },
            Response.ErrorListener {
                // Get network response
                Log.i("NETWORK_RESPONSE", it.message.toString())
                val msg = it.message.toString()
                if(msg.contains(connectionErrorMessage)) {
                    connectionError?.value = NetworkResponse("UNAVAILABLE", "The device is probably off")
                }
                else {
                    val statusCode = it.networkResponse.statusCode
                    if (statusCode == 401) {
                        val response = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        if (response["message"] == "token expired") {
                            // get new token
                            getNewToken()
                            // Retry snapshot
                            setMotion(status)
                        } else {
                            successfulMotion?.value = NetworkResponse(
                                response["status"].toString(),
                                response["message"].toString()
                            )
                        }
                    } else {
                        successfulMotion?.value = NetworkResponse("error", "Internal error")
                    }
                }
            }
        )
        // Custom header with token
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // headers["Authorization"] = "token.toString()"
                headers["Authorization"] = token.toString()
                return headers
            }
        }
        // Volley policy policy, only one time to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0,
            1f
        )
        // Add the volley post request to the request queue
        queue.add(request)
    }

    fun getMotion() {
        var url = baseUrl+"motion"
        // Instantiate the RequestQueue
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val request = object: JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener { response ->
                Log.i("SUCCESSFUL_RESPONSE", response["message"].toString())
                // Get network JSON response
                successfulMotion?.value = NetworkResponse(response["status"].toString(), response["message"].toString())
            },
            Response.ErrorListener {
                // Get network response
                Log.i("NETWORK_RESPONSE", it.message.toString())
                val msg = it.message.toString()
                if(msg.contains(connectionErrorMessage)) {
                    connectionError?.value = NetworkResponse("UNAVAILABLE", "The device is probably off")
                }
                else {
                    val statusCode = it.networkResponse.statusCode
                    if (statusCode == 401) {
                        val response = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        if (response["message"] == "token expired") {
                            // get new token
                            getNewToken()
                            // Retry snapshot
                            getMotion()
                        } else {
                            successfulMotion?.value = NetworkResponse(
                                response["status"].toString(),
                                response["message"].toString()
                            )
                        }
                    } else {
                        successfulMotion?.value = NetworkResponse("error", "Internal error")
                    }
                }
            }
        )
        // Custom header with token
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // headers["Authorization"] = "token.toString()"
                headers["Authorization"] = token.toString()
                return headers
            }
        }
        // Volley policy policy, only one time to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0,
            1f
        )
        // Add the volley post request to the request queue
        queue.add(request)
    }

    fun setFace(status: Boolean) {
        var url = if (status){
            baseUrl+"face?detection=1"
        } else {
            baseUrl+"face?detection=0"
        }
        // Instantiate the RequestQueue
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val request = object: JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener { response ->
                Log.i("SUCCESSFUL_RESPONSE", response["message"].toString())
                // Get network JSON response
                successfulMotion?.value = NetworkResponse(response["status"].toString(), response["message"].toString())
            },
            Response.ErrorListener {
                // Get network response
                Log.i("NETWORK_RESPONSE", it.message.toString())
                val msg = it.message.toString()
                if(msg.contains(connectionErrorMessage)) {
                    connectionError?.value = NetworkResponse("UNAVAILABLE", "The device is probably off")
                }
                else {
                    val statusCode = it.networkResponse.statusCode
                    if (statusCode == 401) {
                        val response = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        if (response["message"] == "token expired") {
                            // get new token
                            getNewToken()
                            // Retry snapshot
                            setFace(status)
                        } else {
                            successfulMotion?.value = NetworkResponse(
                                response["status"].toString(),
                                response["message"].toString()
                            )
                        }
                    } else {
                        successfulMotion?.value = NetworkResponse("error", "Internal error")
                    }
                }
            }
        )
        // Custom header with token
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // headers["Authorization"] = "token.toString()"
                headers["Authorization"] = token.toString()
                return headers
            }
        }
        // Volley policy policy, only one time to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0,
            1f
        )
        // Add the volley post request to the request queue
        queue.add(request)
    }

    fun getFace() {
        var url = baseUrl+"face"
        // Instantiate the RequestQueue
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val request = object: JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener { response ->
                Log.i("SUCCESSFUL_RESPONSE", response["message"].toString())
                // Get network JSON response
                successfulFace?.value = NetworkResponse(response["status"].toString(), response["message"].toString())
            },
            Response.ErrorListener {
                // Get network response
                Log.i("NETWORK_RESPONSE", it.message.toString())
                val msg = it.message.toString()
                if(msg.contains(connectionErrorMessage)) {
                    connectionError?.value = NetworkResponse("UNAVAILABLE", "The device is probably off")
                }
                else {
                    val statusCode = it.networkResponse.statusCode
                    if (statusCode == 401) {
                        val response = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        if (response["message"] == "token expired") {
                            // get new token
                            getNewToken()
                            // Retry snapshot
                            getFace()
                        } else {
                            successfulFace?.value = NetworkResponse(
                                response["status"].toString(),
                                response["message"].toString()
                            )
                        }
                    } else {
                        successfulFace?.value = NetworkResponse("error", "Internal error")
                    }
                }
            }
        )
        // Custom header with token
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // headers["Authorization"] = "token.toString()"
                headers["Authorization"] = token.toString()
                return headers
            }
        }
        // Volley policy policy, only one time to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0,
            1f
        )
        // Add the volley post request to the request queue
        queue.add(request)
    }

    fun checkAvailability() {
        var url = baseUrl
        // Instantiate the RequestQueue
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val request = object: JsonObjectRequest( Method.GET, url, null,
            Response.Listener { response ->
                Log.i("SUCCESSFUL_RESPONSE", response["message"].toString())
                // Get network JSON response
                availability?.value = NetworkResponse(response["status"].toString(), response["message"].toString())
            },
            Response.ErrorListener {
                // Get network response
                Log.i("NETWORK_RESPONSE", it.message.toString())
                val msg = it.message.toString()
                if(msg.contains(connectionErrorMessage)) {
                    availability?.value = NetworkResponse("UNAVAILABLE", "The device is probably off")
                }
                else {
                    val statusCode = it.networkResponse.statusCode
                    if (statusCode == 401) {
                        val response = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        if (response["message"] == "token expired") {
                            // get new token
                            getNewToken()
                            // Retry snapshot
                            checkAvailability()
                        } else {
                            availability?.value = NetworkResponse(
                                response["status"].toString(),
                                response["message"].toString()
                            )
                        }
                    } else {
                        availability?.value = NetworkResponse("error", "Internal error")
                    }
                }
            }
        )
        // Custom header with token
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // headers["Authorization"] = "token.toString()"
                headers["Authorization"] = token.toString()
                return headers
            }
        }
        // Volley policy policy, only one time to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0,
            1f
        )
        // Add the volley post request to the request queue
        queue.add(request)
    }

    private fun getNewToken() {
        val url = baseUrl+"login"
        val params = HashMap<String?, String?>()
        params["password"] = password
        // params["password"] = "password"
        val jsonParams = JSONObject(params as Map<*, *>)
        // LOG
        // Instantiate the RequestQueue
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val request = JsonObjectRequest(Request.Method.POST, url, jsonParams,
            Response.Listener { response ->
                // Display token
                val strToken = response["auth-token"].toString()
                token = strToken
                successfulRetryLogin?.value = NetworkResponse(response["status"].toString(), response["message"].toString())
            },
            Response.ErrorListener {
                Log.i("NETWORK_RESPONSE", it.message.toString())
                val msg = it.message.toString()
                if(msg.contains(connectionErrorMessage)) {
                    connectionError?.value = NetworkResponse("UNAVAILABLE", "The device is probably off")
                }
                else {
                    val status = it.networkResponse.statusCode
                    if ( status == 401) {
                        val response = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        successfulRetryLogin?.value = NetworkResponse(response["status"].toString(), response["message"].toString())
                    } else {
                        successfulRetryLogin?.value = NetworkResponse("error", "Internal error")
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

    fun getDeviceFirebase() {
        readData(object: FirebaseCallback{
            override fun onCallback(device: Device?) {
                val th = Thread(Runnable {
                    database.insertDevice(RoomDevice(
                        device?.deviceName.toString(),
                        device?.url.toString(),
                        "",
                        ""
                    ))
                })
                th.start()
                th.join()
                firebaseDevice?.value = PiCamera(
                    "success",
                    device?.deviceName.toString(),
                    device?.url.toString(),
                    "",
                    ""
                )
            }
        })
    }

    private fun readData(firebaseCallback: FirebaseCallback) {
        // Get reference to database
        val ref = FirebaseDatabase.getInstance()
            .getReference("/houses/${houseId}/rooms/${roomId}/devices/${deviceId}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d("DEVICE_FIREBASE", "Data CANNOT BE LOADED")
            }
            override fun onDataChange(p0: DataSnapshot) {
                val deviceId = p0.child("id").getValue().toString()
                val deviceName = p0.child("deviceName").getValue().toString()
                val type = p0.child("type").getValue().toString()
                val url = p0.child("url").getValue().toString()

                val device = Device(deviceId, deviceName, type, url)
                firebaseCallback.onCallback(device)
            }

        })
    }

    // Interface for managing the callbacks
    private interface FirebaseCallback{
        fun onCallback(device:Device?)
    }



}