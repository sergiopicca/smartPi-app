package com.example.firebaseauthmvvm.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.database.SmartPiDatabase
import com.example.firebaseauthmvvm.databinding.FragmentNotificationsBinding
import com.google.firebase.messaging.FirebaseMessaging

class NotificationsFragment : Fragment() {
    private var TOPIC = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentNotificationsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_notifications, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SmartPiDatabase.getInstance(application).smartPiDatabaseDao

        val viewModelFactory = NotificationsViewModelFactory(dataSource, application)
        val notificationsViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(NotificationsViewModel::class.java)

        binding.notificationsViewModel = notificationsViewModel
        // Adapter for the recycler view
        val adapter = MessageAdapter()
        binding.chatMessages.adapter = adapter

        notificationsViewModel._completeMessages.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
                binding.chatMessages.scrollToPosition(adapter.itemCount - 1)
            }
        })

        val msgText = binding.root.findViewById<EditText>(R.id.msg_text).text
        binding.root.findViewById<Button>(R.id.send).setOnClickListener {
            notificationsViewModel.send(msgText.toString())
            binding.msgText.text.clear()
        }

        binding.setLifecycleOwner(this)
        notificationsViewModel._house.observe(viewLifecycleOwner, Observer {

            createChannel(
                getString(R.string.notification_msg_channel_id),
                getString(R.string.notification_msg_channel_name)
            )

            TOPIC = notificationsViewModel._house.value.toString()

            // call subscribe topics on start
            subscribeTopic()
        })

        return binding.root
    }

    // subscribe to breakfast topic
    private fun subscribeTopic() {
        // [START subscribe_topics]
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
            .addOnCompleteListener { task ->
                var msg = getString(R.string.message_subscribed)
                if (!task.isSuccessful) {
                    msg = getString(R.string.message_subscribe_failed)
                }
                // Toast.makeText(context, "$msg: $TOPIC", Toast.LENGTH_SHORT).show()

            }
        // [END subscribe_topics]
    }

    private fun createChannel(channelId: String, channelName: String) {
        // START create a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                // change importance
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Messages"

            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
        // END create channel
    }
}