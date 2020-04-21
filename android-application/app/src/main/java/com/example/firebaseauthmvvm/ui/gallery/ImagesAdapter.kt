package com.example.firebaseauthmvvm.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.models.Image
import com.squareup.picasso.Picasso

class ImagesAdapter :
    RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    var data = mutableListOf<Image>()
        set(value) {
            field = value
            notifyDataSetChanged()
            notifyItemInserted(data.size)
        }
    private var listenerDelete: ((item:Image) -> Unit)? = null
    private var listenerDownload: ((item:Image) -> Unit)? = null

    // Delete image function
    fun deleteImg(listener: (item: Image) -> Unit){
        this.listenerDelete = listener
    }

    fun removeImg(image: Image){
        data.remove(image)
    }

    // Download image function
    fun downloadImg(listener: (item: Image) -> Unit) {
        this.listenerDownload = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
            .inflate(R.layout.image_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.name.text = item.name
        Picasso.get().load(item.url).into(holder.img)
        holder.vButton.setOnClickListener { listenerDelete?.invoke(item) }
        holder.dButton.setOnClickListener { listenerDownload?.invoke(item) }
    }

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.nameGoogle)
        val img: ImageView = itemView.findViewById(R.id.imageGoogle)
        val vButton: ImageButton = itemView.findViewById(R.id.image_view_button)
        val dButton: ImageButton = itemView.findViewById(R.id.image_download_button)
    }

}