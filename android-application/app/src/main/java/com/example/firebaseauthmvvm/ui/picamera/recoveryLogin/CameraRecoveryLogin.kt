package com.example.firebaseauthmvvm.ui.picamera.recoveryLogin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
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
import com.example.firebaseauthmvvm.databinding.FragmentCameraRecoveryLoginBinding

class CameraRecoveryLogin : Fragment() {

    lateinit var binding : FragmentCameraRecoveryLoginBinding
    lateinit var dataSource : SmartPiDatabaseDao
    private lateinit var viewModel: CameraRecoveryLoginViewModel

    lateinit var deviceId: String
    lateinit var baseUrl: String
    lateinit var houseId: String
    lateinit var roomId: String
    lateinit var roomName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Initialize binding and viewModel
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera_recovery_login,
            container, false)
        viewModel = ViewModelProviders.of(this).get(CameraRecoveryLoginViewModel::class.java)
        // Local database instance
        val application = requireNotNull(this.activity).application
        // Initialize datasource
        dataSource = SmartPiDatabase.getInstance(application).smartPiDatabaseDao

        binding.viewModel = viewModel

        // Set lifecycle
        binding.lifecycleOwner = this

        // Retrieve data from nav
        val args: CameraRecoveryLoginArgs by navArgs()
        deviceId = args.deviceId
        baseUrl = args.baseUrl
        houseId = args.houseId
        roomId = args.roomId
        roomName = args.roomName
        Log.i("PARAMS_RECOVERY_LOGIN", baseUrl)
        viewModel.getUrl(baseUrl)

        // Initialize listeners
        initObservable()

        return binding.root
    }

    private fun initObservable() {
        viewModel.successfulLogin?.observe(viewLifecycleOwner, Observer { camera ->
            if (camera.status == "INVALID") {
                Toast.makeText(context, "Invalid credentials", Toast.LENGTH_LONG).show()
            }
            else {
                if (camera.status == "ERROR") {
                    val action = CameraRecoveryLoginDirections.actionCameraRecoveryLoginToRoomDetailFragment(
                        roomName,roomId,houseId, true
                    )
                    findNavController().navigate(action)
                }
                else {
                    val th = Thread(Runnable {
                        val device = dataSource.getDevices()
                        Log.i("RECOVERY_CAMERA_RETRIEVED", device.toString())
                        if (device == null) {
                            dataSource.insertDevice(RoomDevice(
                                "piCamera",
                                camera.baseUrl,
                                camera.password,
                                camera.token
                            ))
                            Log.i("CAMERA_DB_LOCAL","Camera instance created")
                        } else{
                            dataSource.updateDevice(RoomDevice(
                                device?.name.toString(),
                                device?.url.toString(),
                                camera.password,
                                camera.token
                            ))
                            Log.i("CAMERA_DB_LOCAL","Camera instance updated")
                        }
                    })
                    th.start()
                    th.join()
                    val action =
                        CameraRecoveryLoginDirections.actionCameraRecoveryLoginToCameraFragment(
                            deviceId,
                            houseId,
                            roomId,
                            roomName
                        )
                    findNavController().navigate(action)
                }
            }
        })
        // Back button
        val backButton = binding.root.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener{
            val action = CameraRecoveryLoginDirections.actionCameraRecoveryLoginToRoomDetailFragment(
                roomId,roomName,houseId
            )
            findNavController().navigate(action)
        }
    }
}
