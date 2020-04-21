package com.example.firebaseauthmvvm.ui.roomDetail

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.firebaseauthmvvm.models.Device

class DeleteDeviceDialogFragment
    (val roomDetailViewModel: RoomDetailViewModel,
     val dev: Device)
    : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Are you really sure to delete the device: ${dev.deviceName}?")
                .setPositiveButton("Delete",
                    DialogInterface.OnClickListener { dialog, id ->
                        //Toast.makeText(context,"Deleted",Toast.LENGTH_LONG).show()
                        this.delete(roomDetailViewModel, dev)
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
    private fun delete(roomDetailViewModel: RoomDetailViewModel, dev: Device){
        roomDetailViewModel.deleteDevice(dev)
    }
}