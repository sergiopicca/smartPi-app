package com.example.firebaseauthmvvm.ui.roomDetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.databinding.FragmentRoomDetailBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class RoomDetailFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentRoomDetailBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_room_detail, container, false)

        val application = requireNotNull(this.activity).application
        // Hide the navigation menu on the bottom
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        val viewModelFactory = RoomDetailViewModelFactory(application)
        val roomDetailViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(RoomDetailViewModel::class.java)

        binding.roomDetailViewModel = roomDetailViewModel

        // Collecting the arguments from the action
        val args: RoomDetailFragmentArgs by navArgs()
        val roomName = args.roomName.capitalize()

        // Get the id of the house and the id of the room to collect the devices
        val roomId = args.roomId
        val homeId = args.houseId
        var deviceLabel = "These are the devices in this room"

        // If a device is not avaiable we output a toast
        if(args.deviceUnavailable) {
            Toast.makeText(context, "Device unavailable!", Toast.LENGTH_LONG).show()
        }

        // Handle the click on the device
        val adapter = DeviceAdapter(DeviceListener { dev ->
            if(dev.type == "camera") {
                // GO TO CAMERA
                val action = RoomDetailFragmentDirections
                    .actionRoomDetailFragmentToCameraFragment(dev.id, homeId, roomId, roomName)
                findNavController().navigate(action)
            } else {
                Toast.makeText(context, "Device unavailable!", Toast.LENGTH_SHORT).show()
            }
        })

        // Dialog pops up for the delete of the device
        adapter.deleteDevice {
            val dialogFragment = DeleteDeviceDialogFragment(roomDetailViewModel, it)
            dialogFragment.show(fragmentManager!!,"AlertDialog")
        }

        binding.deviceList.adapter = adapter

        // Set the GridLayout
        val manager = GridLayoutManager(activity, 2)
        binding.deviceList.layoutManager = manager

        // Change the view
        // Add the room name
        binding.name.text = roomName
        binding.labelDevices.text = deviceLabel
        Log.d("arguments", args.toString())
        // Get the devices of the room
        roomDetailViewModel.getDevices(homeId,roomId)

        // Pass the devices to the adapter
        roomDetailViewModel._devices.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        // Set the click listner for coming back to the home
        binding.backHomeButton.setOnClickListener { view: View->
            findNavController().navigate(R.id.action_roomDetailFragment_to_navigation_home2)
        }

        // Add Device Listener
        binding.addDeviceButton.setOnClickListener {view: View? ->
            val action = RoomDetailFragmentDirections
                .actionRoomDetailFragmentToCameraLoginFragment(
                    roomId,
                    roomName
                )
            findNavController().navigate(action)
        }

        return binding.root
    }
}
