package com.example.firebaseauthmvvm.ui.recap

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.database.SmartPiDatabase
import com.example.firebaseauthmvvm.databinding.FragmentRecapBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.Picasso

class RecapFragment : Fragment() {
    private lateinit var recapViewModel: RecapViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentRecapBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_recap, container, false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = SmartPiDatabase.getInstance(application).smartPiDatabaseDao
        val viewModelFactory = RecapViewModelFactory(dataSource, application)
        recapViewModel =
            ViewModelProviders.of(
                this, viewModelFactory
            ).get(RecapViewModel::class.java)

        binding.recapViewModel = recapViewModel
        binding.setLifecycleOwner(this)

        // Remove the botton navigation for this view
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        binding.logoutButton.setOnClickListener { view:View ->
            recapViewModel.logout(view)
        }

        // Observe the changes of the fetchedUser
        recapViewModel.fetchedUser.observe(viewLifecycleOwner, Observer {
            if(it.owner.isNullOrBlank()){
                binding.statusRecap.text = "Guest"
            }
            else{
                binding.statusRecap.text = "Owner"
            }
            // Load the img
            if(recapViewModel.fetchedUser.value?.profileImageUrl != ""){
                Picasso.get().load(recapViewModel.fetchedUser.value?.profileImageUrl).into(binding.recapImg)
            }
        })

        binding.exitDialog.setOnClickListener { view:View ->
            view.findNavController().navigate(R.id.action_dialogFragment_to_navigation_home)
            // Once I come back I set the bottom navigation visible again
        }

        // Handling the change of the profile picture
        binding.modifyPic.setOnClickListener { view:View ->
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 200)
        }

        return binding.root
    }

    // Handle the result of the select
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Result", requestCode.toString() + " " + resultCode.toString() + " " +data.toString())
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            Log.d("Image", data.data.toString())
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver,uri)
            if(uri != null){
                // Use the view model to handle the data
                recapViewModel.storeImg(uri)
            }
        }
    }
}