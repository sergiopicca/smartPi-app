package com.example.firebaseauthmvvm.ui.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.firebaseauthmvvm.models.Room

class DeleteRoomDialogFragment(val homeViewModel: HomeViewModel, val room: Room) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Are you really sure to delete the room: ${room.name}?")
                .setPositiveButton("Delete",
                    DialogInterface.OnClickListener { dialog, id ->
                        //Toast.makeText(context,"Deleted",Toast.LENGTH_LONG).show()
                        this.delete(homeViewModel, room)
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
    private fun delete(homeViewModel: HomeViewModel, room: Room){
        homeViewModel.deleteRoom(room)
    }
}

