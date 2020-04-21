package com.example.firebaseauthmvvm.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.models.Message
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

// The core task in implementing a RecyclerView is creating the adapter.
// The adapter creates a view holder and fills it with data for the RecyclerView to display.
class MessageAdapter: RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    private val VIEW_TYPE_MY_MESSAGE = 1
    private val VIEW_TYPE_OTHER_MESSAGE = 2

    var data =  listOf<Message>()
        set(value) {
            field = value
            notifyDataSetChanged()
            notifyItemInserted(data.size)
        }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        val msg = data.get(position)
        val isMine = msg.sendByMe
        if(isMine){
            return VIEW_TYPE_MY_MESSAGE
        }
        else{
            return VIEW_TYPE_OTHER_MESSAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if(viewType == VIEW_TYPE_MY_MESSAGE) {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater
                .inflate(R.layout.chat_to_row, parent, false)
            return ViewHolder(view)
        } else {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater
                .inflate(R.layout.chat_from_row, parent, false)
            return ViewHolder(view)
        }
    }

    // The onBindViewHolder()function is called by RecyclerView to display the data for one
    // list item at the specified position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.msgText.text = item.text
        holder.sender.text = item.sender
        holder.ts.text = item.ts
        Picasso.get().load(item.senderImg).into(holder.img)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val msgText: TextView = itemView.findViewById(R.id.content)
        val sender: TextView = itemView.findViewById(R.id.sender_name)
        val ts: TextView = itemView.findViewById(R.id.timestamp)
        val img:CircleImageView = itemView.findViewById(R.id.img)
    }
}
