package com.example.firebaseauthmvvm.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseauthmvvm.databinding.RoomItemViewBinding
import com.example.firebaseauthmvvm.models.Room

// The core task in implementing a RecyclerView is creating the adapter.
// The adapter creates a view holder and fills it with data for the RecyclerView to display.
class RoomAdapter(val clickListener: RoomListener): ListAdapter<Room, RoomAdapter.ViewHolder>(RoomNightDiffCallback()) {
    private var listener: ((item:Room) -> Unit)? = null

    fun deleteRoom(listener: (item: Room) -> Unit) {
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

    class ViewHolder private constructor(val binding: RoomItemViewBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(
            item: Room,
            clickListener: RoomListener,
            listener: ((item: Room) -> Unit)?
        ) {
            binding.room = item

            // Assign the text in the card
            binding.roomName.text = item.name.capitalize()
            binding.devNumber.text = "Explore your room."
            binding.delete.setOnClickListener{listener?.invoke(item)}

            // Assign the click listener
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RoomItemViewBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class RoomNightDiffCallback : DiffUtil.ItemCallback<Room>() {
    // Room are the same if the have the same id
    override fun areItemsTheSame(oldItem: Room, newItem: Room): Boolean {
        return oldItem.roomId == newItem.roomId
    }

    // Checking if rooms contain the same data
    override fun areContentsTheSame(oldItem: Room, newItem: Room): Boolean {
        return oldItem == newItem
    }

}

// Listener for handling the click
class RoomListener(val clickListener: (r: Room) -> Unit) {
    fun onClick(room: Room) = clickListener(room)
}
