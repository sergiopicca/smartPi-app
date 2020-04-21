package com.example.firebaseauthmvvm.ui.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.data.firebase.FirebaseSource
import com.example.firebaseauthmvvm.data.repositories.UserRepository
import com.example.firebaseauthmvvm.ui.home.HomeActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationsFirebaseMsgService : FirebaseMessagingService() {

    private val repository = UserRepository(FirebaseSource())

    // Current logged user
    private val currentUser by lazy {
        repository.currentUser()
    }

    private val notificationId = 0

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")
        // check messages for data
        // Check if message contains a data payload.
        remoteMessage.data.let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
        }
        // check messages for notification and call sendNotification
        // Check if message contains a notification payload.
        remoteMessage.notification.let {
            Log.d(TAG, "Message Notification user id: ${it?.tag}")
            Log.d(TAG, "Message Notification Title: ${it?.title}")
            Log.d(TAG, "Message Notification Body: ${it?.body}")
            sendNotification(it?.tag!!, it.title!!, it.body!!)
        }
    }
    // [END receive_message]

    // log registration token
    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Persist token to third-party servers.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageTag: String, messageTitle: String, messageBody: String) {
        // check if current user is the creator of the notification
        if (!messageTag.isNullOrBlank() && (currentUser?.uid.isNullOrBlank() || currentUser?.uid == messageTag)) {
            return
        }
        // Instantiate notification manager
        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java) as NotificationManager
        // Create pending intent
        val contentPendingIntent : PendingIntent?
        if(messageTag.contains("topic_key_")){ // intrusion notification
            contentPendingIntent = applicationContext?.let {
                NavDeepLinkBuilder(it)
                    .setComponentName(HomeActivity::class.java)
                    .setGraph(R.navigation.mobile_navigation)
                    .setDestination(R.id.navigation_home)
                    .createPendingIntent()
            }
        }
        else { // chat notification
            contentPendingIntent = applicationContext?.let {
                NavDeepLinkBuilder(it)
                    .setComponentName(HomeActivity::class.java)
                    .setGraph(R.navigation.mobile_navigation)
                    .setDestination(R.id.navigation_notifications)
                    .createPendingIntent()
            }
        }
        // Logo
        val logo = BitmapFactory.decodeResource(
            applicationContext.resources,
            R.drawable.logo_smartpi // Should be in png
        )
        val bigPicStyle = NotificationCompat.BigPictureStyle()
            .bigPicture(logo)
            .bigLargeIcon(null)

        // Get an instance of NotificationCompat.Builder
        val builder = NotificationCompat.Builder(
            applicationContext,
            // verify the notification channel name
            applicationContext.getString(R.string.notification_msg_channel_id)
        )
            .setSmallIcon(R.drawable.logo_smartpi)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // This ID represents the current notification instance and is needed for updating or
        // cancelling this notification.
        notificationManager.notify(notificationId, builder.build())

    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}