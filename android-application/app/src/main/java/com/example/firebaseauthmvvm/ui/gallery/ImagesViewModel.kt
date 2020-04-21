package com.example.firebaseauthmvvm.ui.gallery


import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.models.Image
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ImagesViewModel(application: Application) : AndroidViewModel(application){
    // Will contain the list of messages in the home chat
    val _completeImages = MutableLiveData<MutableList<Image>>()
    // Application context
    private val mContext = application.applicationContext

    var CHANNEL_1_ID = "Download"

    // Function to retrieve images based on house id
    fun retrieveImages(houseId: String){
        val supportList = mutableListOf<Image>()
        val storageRef = FirebaseStorage.getInstance().reference.child("/$houseId")
        storageRef.listAll().addOnCompleteListener {
            if (it.isSuccessful) {
                val imagesIter = it.result?.items?.listIterator()
                imagesIter!!.forEach { image ->
                    image.downloadUrl.addOnCompleteListener { url ->
                        if (url.isSuccessful) {
                            supportList.add(Image(image.name, url.result.toString()))
                            Log.d("ImagesViewModel", "Name: ${image.name} Path: ${url.result}")
                            _completeImages.value = supportList
                        }
                    } // DownloadListener
                } // forEach
            } // Successful
        } // ListALl Listner
    }

    fun deleteImage(houseId: String, name: String) {
        // Every user's profile image has as path the user's id
        val ref = FirebaseStorage.getInstance().getReference("/$houseId/$name")
        // Delete the previous image
        ref.delete()
    }

    fun downloadImage(name: String, url: String, activity: Activity) {
        // https://johncodeos.com/how-to-download-image-from-the-web-in-android-using-kotlin/
        val directory = File(Environment.DIRECTORY_PICTURES)

        if (!directory.exists()){
            directory.mkdirs()
        }

        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(url.substring(url.lastIndexOf("/") + 1))
                .setDescription("")
                .setDestinationInExternalPublicDir(
                    directory.toString(),
                    url.substring(url.lastIndexOf("/") + 1)
                )
        }
        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)

        // Settings for Notification
        val manager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(CHANNEL_1_ID,
                "Image Download",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel1.description = "Channel for image download"
            manager.createNotificationChannel(channel1)
        }

        val progressMax = 100
        val notification = NotificationCompat.Builder(activity.applicationContext, "Download")
            .setSmallIcon(R.drawable.logo_smartpi)
            .setContentTitle(name)
            .setContentText("Download in progress ...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(progressMax, 0, false)

        manager.notify(1, notification.build())

        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.type = "image/*"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        // val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.substring(url.lastIndexOf("/") + 1)))
        val contentPendingIntent = PendingIntent.getActivity(
            activity.applicationContext,
            1,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )


        Thread(Runnable {
            SystemClock.sleep(2000)
            for (progress in 1..progressMax step 10){
                notification.setProgress(progressMax, progress, false)
                manager.notify(1, notification.build())
                SystemClock.sleep(1000)
            }
            notification.setContentText("Download Finished")
                .setProgress(0, 0, false)
                .setOngoing(false)
                .setContentIntent(contentPendingIntent)
            manager.notify(1, notification.build())
        }).start()
    }
}