package com.example.firebaseauthmvvm.ui.edit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.models.House
import com.example.firebaseauthmvvm.models.Image
import com.squareup.picasso.Picasso

class HouseAdapter :
    RecyclerView.Adapter<HouseAdapter.ViewHolder>() {
    var data = mutableListOf<House>()
        set(value) {
            field = value
            notifyDataSetChanged()
            notifyItemInserted(data.size)
        }
    private var listenerDelete: ((item:House) -> Unit)? = null
    private var listenerImages: ((item:House) -> Unit)? = null
    private var listenerModify: ((item:House) -> Unit)? = null

    fun removeHouse(listener: (item: House) -> Unit){
        this.listenerDelete = listener
        //data.remove(image)
    }

    // Show images of the
    fun imagesHouse(listener: (item: House) -> Unit){
        this.listenerImages = listener
    }

    // Download image function
    fun modifyHouse(listener: (item: House) -> Unit) {
        this.listenerModify = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
            .inflate(R.layout.house_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.name.text = item.name
        holder.address.text = item.address
        holder.removeButton.setOnClickListener { listenerDelete?.invoke(item) }
        holder.imagesButton.setOnClickListener { listenerImages?.invoke(item) }
        holder.modifyButton.setOnClickListener { listenerModify?.invoke(item) }
    }

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.house_name)
        val address: TextView = itemView.findViewById(R.id.house_address)
        val removeButton: ImageButton = itemView.findViewById(R.id.house_delete)
        val imagesButton: Button = itemView.findViewById(R.id.button_images_house_edit)
        val modifyButton: Button = itemView.findViewById(R.id.button_modify_house_edit)
    }

}