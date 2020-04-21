package com.example.firebaseauthmvvm.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.database.SmartPiDatabase
import com.example.firebaseauthmvvm.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.Picasso
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator

class HomeFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentHomeBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_home, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SmartPiDatabase.getInstance(application).smartPiDatabaseDao
        val viewModelFactory = HomeViewModelFactory(dataSource, application)
        val homeViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(HomeViewModel::class.java)

        binding.homeViewModel = homeViewModel

        // Prevent to hide the bottom nav menu
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE

        // Update the meteo
        homeViewModel._house.observe(viewLifecycleOwner, Observer {
            homeViewModel.getWeather()
        })

        // Set the icon for depending on the weather conditions
        homeViewModel._iconWeatherConditions.observe(viewLifecycleOwner, Observer {
            Picasso.get().load(it).into(binding.iconWeather)
        })

        // Handle the dialog in order to log out and view user information
        val userRecapButton: Button = binding.root.findViewById(R.id.showUserRecap)
        userRecapButton.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.goToRecap)
        }

        // Defining the horizontal layout of the recycler view
        binding.roomsList.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        // Adapter for the recycler view
        val adapter = RoomAdapter(RoomListener { r ->
            // Handle the click of the room
            val action = HomeFragmentDirections
                .actionNavigationHomeToRoomDetailFragment(r.name, r.roomId, homeViewModel._house.value!!)
            findNavController().navigate(action)
        })

        // Handle the delete of the Room
        adapter.deleteRoom {
            val dialogFragment = DeleteRoomDialogFragment(homeViewModel, it)
            dialogFragment.show(fragmentManager!!,"AlertDialog")
        }

        binding.roomsList.adapter = adapter

        val recyclerIndicator: ScrollingPagerIndicator = binding.indicator
        recyclerIndicator.attachToRecyclerView(binding.roomsList)


        // Handle the snap in the recycler view
        val snapHelper:SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.roomsList)

        // Instantiate the dialog for adding a new room
        val addRoomDialogFragment = AddRoomDialogFragment(homeViewModel)
        binding.addRoom.setOnClickListener { view ->
            addRoomDialogFragment.show(fragmentManager!!, "AddRoom")
        }

        // Add rooms to the recycler view
        homeViewModel._rooms.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        binding.setLifecycleOwner(this)

        return binding.root
    }
}