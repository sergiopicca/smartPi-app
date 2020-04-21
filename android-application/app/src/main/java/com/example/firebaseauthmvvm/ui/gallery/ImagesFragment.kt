package com.example.firebaseauthmvvm.ui.gallery

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.databinding.FragmentImagesBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class ImagesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentImagesBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_images, container, false
        )

        val application = requireNotNull(this.activity).application
        // Hide the navigation menu on the bottom
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        val viewModelFactory = ImagesViewModelFactory(application)
        val imagesViewModel =
            ViewModelProviders.of(
                this, viewModelFactory
            ).get(ImagesViewModel::class.java)

        binding.imagesViewModel = imagesViewModel

        // Retrieve args from navigation
        val args: ImagesFragmentArgs by navArgs()
        val houseId = args.houseId

        // Adapter for the recycler view
        val adapter = ImagesAdapter()
        binding.imagesView.adapter = adapter
        binding.imagesView.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))

        // Retrieve images from house id
        imagesViewModel.retrieveImages(houseId)

        imagesViewModel._completeImages.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
                // binding.imagesView.scrollToPosition(adapter.itemCount - 1)
            }
        })

        // Implement the view of the image
        adapter.deleteImg {
            //Toast.makeText(context, it.name, Toast.LENGTH_SHORT).show()
            val position = adapter.data.indexOf(it)
            imagesViewModel.deleteImage(houseId, it.name)
            adapter.removeImg(it)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, adapter.itemCount)
        }

        // Implement the download of the image
        adapter.downloadImg {
            // Toast.makeText(context, "Download", Toast.LENGTH_SHORT).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                askPermissions(it.name, it.url, activity!!, imagesViewModel)
            } else {
                imagesViewModel.downloadImage(it.name, it.url, activity!!)
            }
        }

        return binding.root
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun askPermissions(name: String, url: String, activity: Activity, view: ImagesViewModel) {
        if (ContextCompat.checkSelfPermission(activity.applicationContext!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(activity.applicationContext)
                    .setTitle("Permission required")
                    .setMessage("Permission required to save photos from the Web.")
                    .setPositiveButton("Accept") { dialog, id ->
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
                    }
                    .setNegativeButton("Deny") { dialog, id -> dialog.cancel() }
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
                // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
        } else {
            // Permission has already been granted
            view.downloadImage(name, url, activity!!)
        }
    }


    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    }
}