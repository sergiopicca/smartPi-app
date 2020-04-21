package com.example.firebaseauthmvvm.ui.picamera.camera

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.database.RoomDevice
import com.example.firebaseauthmvvm.database.SmartPiDatabase
import com.example.firebaseauthmvvm.database.SmartPiDatabaseDao
import com.example.firebaseauthmvvm.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {
    // ViewModel and databinding
    private lateinit var viewModel: CameraViewModel
    private lateinit var binding: FragmentCameraBinding
    // Local database
    private lateinit var dataSource: SmartPiDatabaseDao
    // Useful params
    lateinit var deviceId: String
    lateinit var houseId: String
    lateinit var roomId: String
    lateinit var roomName: String
    private var strToken: String? = null
    private var strUrl: String? = null
    private var strPassword: String? = null
    private var strDeviceName: String?= null
    // Angle
    private var currentAngle: Int = 0
    private var newAngle: Int = 0
    // Motion / Face
    private var motionEnabled: Boolean = false
    private var faceEnabled: Boolean = false

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?,
                               savedInstanceState: Bundle? ): View {
        // Local database instance
        val application = requireNotNull(this.activity).application
        // Initialize datasource
        dataSource = SmartPiDatabase.getInstance(application).smartPiDatabaseDao
        // Initialize databinding and viewmodel
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)

        val viewModelFactory = CameraViewModelFactory(dataSource, application)
        viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(CameraViewModel::class.java)

        binding.cameraViewModel = viewModel
        // Set lifecycle
        binding.lifecycleOwner = this
        // Retrieve data
        val args: CameraFragmentArgs by navArgs()
        deviceId = args.deviceId
        houseId = args.houseId
        roomId = args.roomId
        roomName = args.roomName
        Log.i("DEVICE_ID", deviceId)
        // Retrieve data
        viewModel.getFirebaseParams(
            houseId,
            roomId,
            deviceId
        )
        val th = Thread(Runnable {
            getData()
        })
        th.start()
        // Initialize listeners
        initializeListeners()
        // Wait for th to finish
        th.join()
        // Return binding
        return binding.root
    }

    private fun initializeListeners(){
        // Wait for device to be available
        viewModel.firebaseDevice?.observe(viewLifecycleOwner, Observer {camera ->
            if (camera.status == "success"){
                // get params
                strDeviceName = camera.deviceName
                strUrl = camera.baseUrl
                strPassword = camera.password
                strToken = camera.token
                val th = Thread(Runnable {
                    getData()
                })
                th.start()
            }
        })
        // check availability of device
        viewModel.availability?.observe(viewLifecycleOwner, Observer { netResponse ->
            if (netResponse.status == "success") {
                viewModel.getAngle()
                viewModel.getFace()
                viewModel.getMotion()
                // Initialize stream
                getStream()
            }
            else {
                if (netResponse.status == "error"){
                    Toast.makeText(context, "Please, login again", Toast.LENGTH_LONG)
                    // Go to recovery login
                    goToRecoveryLogin()
                }
                if (netResponse.status == "UNAVAILABLE"){
                    val action = CameraFragmentDirections.actionCameraFragmentToRoomDetailFragment(
                        roomName,roomId,houseId, true
                    )
                    findNavController().navigate(action)
                }
            }
        })
        // Connection error listener
        viewModel.connectionError?.observe(viewLifecycleOwner, Observer { netResponse ->
            if (netResponse.status == "UNAVAILABLE") {
                Toast.makeText(context, "Device unavailable", Toast.LENGTH_LONG)
                val action = CameraFragmentDirections.actionCameraFragmentToRoomDetailFragment(
                    "", roomId, houseId
                )
                findNavController().navigate(action)
            }
        })
        // Retry login
        viewModel.successfulRetryLogin?.observe(viewLifecycleOwner, Observer { netResponse ->
            if(netResponse?.status == "error") {
                Toast.makeText(context, "Please, login again", Toast.LENGTH_LONG).show()
                // Go to recovery login
                goToRecoveryLogin()
            } else {
                strToken = netResponse.message
                Thread(Runnable {
                    updateDevice()
                })
            }
        })
        // Snapshot Listener
        val snapshot: Button = binding.root.findViewById(R.id.snapshot_button)
        snapshot.setOnClickListener {
            viewModel.getSnapshot()
        }
        // Snapshot feedback
        viewModel.successfulSnapshot?.observe(viewLifecycleOwner, Observer { netResponse ->
            if (netResponse?.status == "success"){
                Toast.makeText(context, "Snapshot Taken!", Toast.LENGTH_LONG).show()
            } else{
                if(netResponse?.message == "token invalid" || netResponse?.message == "token missing"){
                    Toast.makeText(context, "Please, login again", Toast.LENGTH_LONG).show()
                    // Go to recovery login
                    goToRecoveryLogin()
                } else {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show()
                }
            }
        })
        // Angle management
        val angleBar: SeekBar = binding.root.findViewById(R.id.angleBar)
        val angleText: TextView = binding.root.findViewById(R.id.angleText)
        angleText.text = currentAngle.toString()
        angleBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                newAngle = progress
                angleText.text = newAngle.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Set new angle
                viewModel.setAngle(-newAngle)
            }
        })
        // Angle response
        viewModel.successfulAngle?.observe(viewLifecycleOwner, Observer {netResponse ->
            if(netResponse?.status == "success") {
                currentAngle = netResponse.message.toInt()
                // angleText.text = currentAngle.toString()
                angleBar.progress = -currentAngle
            } else {
                if(netResponse?.message == "token invalid" || netResponse?.message == "token missing"){
                    Toast.makeText(context, "Please, login again", Toast.LENGTH_LONG).show()
                    // Go to recovery login
                    goToRecoveryLogin()
                } else {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show()
                    angleText.text = currentAngle.toString()
                }
            }

        })
        // Motion and face switches definition
        val motionSwitch : Switch = binding.root.findViewById(R.id.motion_switch)
        val faceSwitch : Switch = binding.root.findViewById(R.id.face_switch)
        // Motion switch events
        motionSwitch.setOnClickListener {
            viewModel.setMotion(motionSwitch.isChecked)
        }
        // Face switch events
        faceSwitch.setOnClickListener {
            viewModel.setFace(faceSwitch.isChecked)
        }
        // Motion response
        viewModel.successfulMotion?.observe(viewLifecycleOwner, Observer { netResponse ->
            if(netResponse?.status == "success"){
                if(netResponse?.message == "Motion activated" || netResponse?.message == "Motion active") {
                    motionSwitch.isChecked = true
                    motionEnabled = true
                    faceSwitch.isChecked = false
                    faceEnabled = false
                } else {
                    motionSwitch.isChecked = false
                    motionEnabled = false
                }
            } else{
                if(netResponse?.message == "token invalid" || netResponse?.message == "token missing"){
                    Toast.makeText(context, "Please, login again", Toast.LENGTH_LONG).show()
                    // Go to recovery login
                    goToRecoveryLogin()
                } else {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show()
                    motionSwitch.isChecked = motionEnabled
                }
            }
        })
        // Face response
        viewModel.successfulFace?.observe(viewLifecycleOwner, Observer { netResponse ->
            if(netResponse?.status == "success"){
                if(netResponse?.message == "Face detection activated" || netResponse?.message == "Face detection active") {
                    motionSwitch.isChecked = false
                    motionEnabled = false
                    faceSwitch.isChecked = true
                    faceEnabled = true
                } else {
                    faceSwitch.isChecked = false
                    faceEnabled = false
                }
            } else{
                if(netResponse?.message == "token invalid" || netResponse?.message == "token missing"){
                    Toast.makeText(context, "Please, login again", Toast.LENGTH_LONG).show()
                    // Go to recovery login
                    goToRecoveryLogin()
                } else {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show()
                    faceSwitch.isChecked = faceEnabled
                }
            }
        })
        // Image button
        val imageButton = binding.root.findViewById<ImageButton>(R.id.image_button)
        imageButton.setOnClickListener{
            val action = CameraFragmentDirections.actionCameraFragmentToImagesFragment(houseId)
            findNavController().navigate(action)
        }
        // Back button
        val backButton = binding.root.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener{
            val action = CameraFragmentDirections.actionCameraFragmentToRoomDetailFragment(
                roomName,
                roomId,
                houseId
            )
            findNavController().navigate(action)
        }
    }

    private fun getStream() {
        // Get web view view
        val stream : WebView = binding.root.findViewById(R.id.stream_view)
        // Set layput of webview
        stream.settings.setSupportZoom(true)
        stream.settings.loadWithOverviewMode = true
        stream.settings.useWideViewPort = true
        // Initialize headers
        val headers = HashMap<String, String>()
        headers["Authorization"] = strToken.toString()
        // Get stream
        stream.loadUrl("${strUrl}video_feed", headers)
    }

    private fun getData() {
        // Retrieve useful info
        var piCamera: RoomDevice? = dataSource.getDevices()
        Log.i("DEVICE_RETRIEVED", piCamera.toString())
        if (piCamera == null){
            // Retrieve data from firebase
            viewModel.getDeviceFirebase()
        }
        else {
            // get params
            strDeviceName = piCamera?.name
            strUrl = piCamera?.url
            strPassword = piCamera?.password
            strToken = piCamera?.token
            // Pass data to view model
            viewModel.getParams(
                strDeviceName,
                strUrl,
                strPassword,
                strToken)
            // Request current status
            viewModel.checkAvailability()
        }
    }

    private fun updateDevice() {
        dataSource.updateDevice(RoomDevice(
            strDeviceName.toString(),
            strUrl.toString(),
            strPassword.toString(),
            strToken.toString()
        ))
    }

    private fun goToRecoveryLogin(){
        // Go to recovery login
        val action = CameraFragmentDirections
            .actionCameraFragmentToCameraRecoveryLogin(deviceId, strUrl.toString(), houseId, roomId, roomName)
        findNavController().navigate(action)
    }
}
