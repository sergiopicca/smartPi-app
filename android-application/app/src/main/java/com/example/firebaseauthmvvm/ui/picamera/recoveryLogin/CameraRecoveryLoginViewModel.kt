package com.example.firebaseauthmvvm.ui.picamera.recoveryLogin

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
import com.example.firebaseauthmvvm.models.PiCamera
import org.json.JSONObject

class CameraRecoveryLoginViewModel(application: Application): AndroidViewModel(application) {

    private val context: Context = getApplication<Application>().applicationContext
    lateinit var baseUrl: String
    var password: ObservableField<String>? = null
    var btnSelected: ObservableBoolean? = null
    var successfulLogin: MutableLiveData<PiCamera>? = null
    // Error message
    private val connectionErrorMessage :String = "java.net.ConnectException: Failed to connect to"

    init {
        btnSelected = ObservableBoolean(false)
        successfulLogin = MutableLiveData()
        password = ObservableField("")
    }

    fun getUrl(url: String) {
        baseUrl = url
    }

    fun onPasswordChanged(s: CharSequence, start: Int, befor: Int, count: Int) {
        btnSelected?.set(s.toString().length > 6)
    }

    fun login() {
        val url = baseUrl+"login"
        val params = HashMap<String?, String?>()
        val strPass = password?.get().toString()
        params["password"] = strPass
        val jsonParams = JSONObject(params as Map<*, *>)
        // Instantiate the RequestQueue
        val queue = Volley.newRequestQueue(context)
        // Request a string response from the provided URL.
        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonParams,
            Response.Listener { response ->
                // Display token
                val strToken = response["auth-token"].toString()
                // token = strToken
                successfulLogin?.value = PiCamera("SUCCESS", "", baseUrl, strPass, strToken)
            },
            Response.ErrorListener {
                // Get network response
                Log.i("NETWORK_RESPONSE", it.message.toString())
                val msg = it.message.toString()
                if(msg.contains(connectionErrorMessage)) {
                    successfulLogin?.value = PiCamera("ERROR", "", "", "", "")
                }
                else {
                    val status = it.networkResponse.statusCode
                    if ( status == 401) {
                        val response = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        successfulLogin?.value = PiCamera("INVALID", "", "", "", "")
                    } else {
                        successfulLogin?.value = PiCamera("ERROR", "", "", "", "")
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

}
