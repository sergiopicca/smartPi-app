package com.example.firebaseauthmvvm.ui.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.firebaseauthmvvm.R
import kotlinx.android.synthetic.main.fragment_home.*

class AddRoomDialogFragment(val homeViewModel: HomeViewModel) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_room, null)
            builder.setView(view)
                .setPositiveButton("Add",
                    DialogInterface.OnClickListener { dialog, id ->
                        //Toast.makeText(context,"Deleted",Toast.LENGTH_LONG).show()
                        this.addRoom(homeViewModel, view.findViewById<EditText>(R.id.newRoomName).text.toString())
                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    // Function to delete the current room, the viewModel will do the job
    private fun addRoom(homeViewModel: HomeViewModel, insertedName:String){
        homeViewModel.addRoom(insertedName)
    }
}