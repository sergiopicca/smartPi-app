package com.example.firebaseauthmvvm.ui.modify

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.findNavController
import com.example.firebaseauthmvvm.R
import com.google.firebase.database.FirebaseDatabase

class ModifyViewModel(application: Application) : AndroidViewModel(application) {
    fun changeInfoHouse(hid: String, hname: String, htelephone: String, view: View)  {
        Log.d("ModifyViewModel", "Parameters: $hid, $hname, $htelephone")
        val refH = FirebaseDatabase.getInstance().getReference("/houses/$hid")
        refH.child("/name").setValue(hname)
        refH.child("/telephone").setValue(htelephone)
        Toast.makeText(view.context, "Great!",  Toast.LENGTH_SHORT).show()
        view.findNavController().navigate(R.id.action_modifyFragment_to_navigation_edit)
    }
}