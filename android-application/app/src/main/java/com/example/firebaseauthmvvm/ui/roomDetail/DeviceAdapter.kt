package com.example.firebaseauthmvvm.ui.roomDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.databinding.DeviceItemViewBinding
import com.example.firebaseauthmvvm.models.Device

class DeviceAdapter(val clickListener: DeviceListener): ListAdapter<Device, DeviceAdapter.ViewHolder>(DeviceDiffCallback()) {
    private var listener: ((item:Device) -> Unit)? = null

    // Function to pop up the dialog once we have to delete one device.
    fun deleteDevice(listener: (item: Device) -> Unit) {
        this.listener = listener
    }

    // We do not need nomore getItem count since the list implement it for us
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    // The onBindViewHolder()function is called by RecyclerView to display the data for one
    // list item at the specified position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener, listener)
    }

    class ViewHolder private constructor(val binding: DeviceItemViewBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(
            item: Device,
            clickListener: DeviceListener,
            listener: ((item: Device) -> Unit)?
        ) {
            binding.device = item

            // Assign the text in the card
            binding.deviceName.text = item.deviceName.capitalize()
            binding.deviceImage.setImageResource(when (item.type.toLowerCase()){
                "camera" -> R.drawable.ic_cam
                "lamp" -> R.drawable.ic_lamp
                "alexa" -> R.drawable.ic_alexa
                else -> R.drawable.ic_general_device
            })
            binding.deleteDevice.setOnClickListener{listener?.invoke(item)}

            // Assign the click listener
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DeviceItemViewBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class DeviceDiffCallback : DiffUtil.ItemCallback<Device>() {
    // Room are the same if the have the same id
    override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem.id == newItem.id
    }

    // Checking if rooms contain the same data
    override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem == newItem
    }

}

// Listener for handling the click
class DeviceListener(val clickListener: (dev: Device) -> Unit) {
    fun onClick(room: Device) = clickListener(room)
}

