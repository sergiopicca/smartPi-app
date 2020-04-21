package com.example.firebaseauthmvvm.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.database.SmartPiDatabase
import com.example.firebaseauthmvvm.databinding.FragmentEditBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class EditFragment : Fragment() {

    private lateinit var editViewModel: EditViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentEditBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_edit, container, false
        )

        val application = requireNotNull(this.activity).application
        // Show the navigation menu on the bottom
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE

        val dataSource = SmartPiDatabase.getInstance(application).smartPiDatabaseDao

        val viewModelFactory = EditViewModelFactory(dataSource, application)
        val editViewModel =
            ViewModelProviders.of(
                this, viewModelFactory
            ).get(EditViewModel::class.java)

        binding.editViewModel = editViewModel

        // Adapter for the recycler view
        val adapter = HouseAdapter()
        binding.houseView.adapter = adapter

        editViewModel._completeHouse.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
                // binding.imagesView.scrollToPosition(adapter.itemCount - 1)
            }
        })

        adapter.removeHouse {
            Toast.makeText(context, "Delete: ${it.houseuid}", Toast.LENGTH_SHORT).show()
        }

        // Implement the view of the images by the house
        adapter.imagesHouse {
            // Toast.makeText(context, "Images: ${it.name}", Toast.LENGTH_SHORT).show()
            val action = EditFragmentDirections
                .actionNavigationEditToImagesFragment(it.houseuid)
            findNavController().navigate(action)
//            findNavController().navigate(R.id.action_navigation_edit_to_imagesFragment)
        }

        // Implement the modify of the house
        adapter.modifyHouse {
            // Toast.makeText(context, "Modify: ${it.name}", Toast.LENGTH_SHORT).show()
            val action = EditFragmentDirections
                .actionNavigationEditToModifyFragment(it.houseuid, it.name, it.telephone)
            findNavController().navigate(action)
        }

        return binding.root
    }
}