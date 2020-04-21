package com.example.firebaseauthmvvm.ui.picamera.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.database.RoomDevice
import com.example.firebaseauthmvvm.database.RoomUser
import com.example.firebaseauthmvvm.database.SmartPiDatabase
import com.example.firebaseauthmvvm.database.SmartPiDatabaseDao
import com.example.firebaseauthmvvm.databinding.FragmentCameraLoginBinding

class CameraLoginFragment : Fragment() {
    lateinit var binding : FragmentCameraLoginBinding
    lateinit var viewModel :CameraLoginViewModel
    lateinit var dataSource : SmartPiDatabaseDao
    var user: RoomUser? = null
    lateinit var roomId: String
    lateinit var houseId: String
    lateinit var roomName: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize binding and viewModel
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera_login, container, false)
        // Local database instance
        val application = requireNotNull(this.activity).application
        // Initialize datasource
        dataSource = SmartPiDatabase.getInstance(application).smartPiDatabaseDao

        val viewModelFactory = CameraLoginViewModelFactory(dataSource, application)
        viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(CameraLoginViewModel::class.java)

        binding.cameraLoginViewmodel = viewModel

        // Collect data from navigation
        val args :CameraLoginFragmentArgs by navArgs()
        roomId = args.roomId
        roomName = args.roomName

        // Initialize local database
        val th = Thread(Runnable {
            val user = dataSource.getUser()
            houseId = if (user?.owner == "") user?.guest.toString() else user?.owner.toString()
            viewModel.getHouseId(houseId.toString(), roomId)
        })
        th.start()
        initObservable()

        // Set the click listener for adding a device
        binding.button2.setOnClickListener {
             viewModel.onButtonClicked()
        }

        return binding.root
    }

    private fun initObservable() {
        // Observe creation of fake devices
        viewModel.fakeDevice?.observe(viewLifecycleOwner, Observer {
            goToRoomDetails()
        })
        // Observe creation of camera
        viewModel.piCameraLogin?.observe(viewLifecycleOwner, Observer { camera ->
            if(camera.status == "INVALID_CREDENTIAL" || camera.status == "MISSING_CREDENTIAL") {
                Toast.makeText(context, "Invalid Credentials!", Toast.LENGTH_LONG).show()
            }
            else{
                Log.i("STARTING_THREAD", "Thread is starting")
                Thread( Runnable {
                    Log.i("THREAD_RUNNING", "Thread is running")
                    // Put device info in local database
                    val device = dataSource.getDevice(camera.deviceName)
                    Log.i("DEVICE", device.toString())
                    if (device?.name != camera.deviceName) {
                        dataSource.insertDevice(
                            RoomDevice(
                                camera.deviceName,
                                camera.baseUrl,
                                camera.password,
                                camera.token
                            )
                        )
                    }
                    Log.i("DEVICE_LOCAL_DB", dataSource.getDevices().toString())
                    val newDevice = viewModel.addCamera(camera)
                    val action = CameraLoginFragmentDirections
                        .actionCameraLoginFragmentToCameraFragment(newDevice.id, houseId, roomId, roomName)
                    findNavController().navigate(action)
                }).start()
            }
        })
        val backButton = binding.root.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener{
            goToRoomDetails()
        }
        // Spinner listener
        val spinner = binding.root.findViewById<Spinner>(R.id.device_type_spinner)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val value = p0?.getItemAtPosition(p2).toString()
                Log.i("SPINNER_VALUE", value)
                viewModel.changeDeviceType(value)
            }

        }
    }

    private fun goToRoomDetails() {
        val action = CameraLoginFragmentDirections
            .actionCameraLoginFragmentToRoomDetailFragment(roomName, roomId, houseId)
        findNavController().navigate(action)
    }
}


