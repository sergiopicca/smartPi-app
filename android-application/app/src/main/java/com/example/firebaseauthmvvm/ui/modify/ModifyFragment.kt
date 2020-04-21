package com.example.firebaseauthmvvm.ui.modify

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.databinding.FragmentModifyBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class ModifyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentModifyBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_modify, container, false
        )

        val application = requireNotNull(this.activity).application
        // Hide the navigation menu on the bottom
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        val viewModelFactory = ModifyViewModelFactory(application)
        val modifyViewModel =
            ViewModelProviders.of(
                this, viewModelFactory
            ).get(ModifyViewModel::class.java)

        binding.modifyViewModel = modifyViewModel

        // Collecting the arguments from the action
        val args: ModifyFragmentArgs by navArgs()
        val houseId = args.houseId
        val houseName = args.houseName
        val houseTelephone = args.houseTelephone

        binding.textNameHouseModify.setText(houseName)
        binding.textTelephoneHouseModify.setText(houseTelephone)

        binding.buttonModify.setOnClickListener {
            var houseNameText: String = binding.textNameHouseModify.text.toString()
            var houseTelephoneText = binding.textTelephoneHouseModify.text.toString()
            Log.d(
                "ModifyFragment",
                "houseName: $houseNameText, houseTelephone: $houseTelephoneText"
            )
            if (!houseNameText.isNullOrEmpty()) {
                modifyViewModel.changeInfoHouse(
                    houseId,
                    houseNameText,
                    houseTelephoneText,
                    binding.root
                )
            } else {
                Toast.makeText(view?.context, "Please insert the name", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}