package com.example.firebaseauthmvvm.ui.registerHouse

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.utils.startCompleteActivity
import com.example.firebaseauthmvvm.utils.startFirstUsageActivityBack
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.activity_house.*

// Source
// https://www.raywenderlich.com/230-introduction-to-google-maps-api-for-android-with-kotlin

class HouseActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        // 3  REQUEST_CHECK_SETTINGS is used as the request code passed to onActivityResult.
        private const val AUTOCOMPLETE_REQUEST_CODE = 4
    }

    // Declare a LocationCallback property.
    private lateinit var locationCallback: LocationCallback
    // Declare a LocationRequest property and a location updated state property.
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    // I added this variable in order to remove the Marker when another places is searched
    private var lastMarker: Marker? = null
    private var lastPlace: Place? = null

    // Stuff necessary for the map
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house)

        // Localization
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Here you update lastLocation with the new location and update the map with
        // the new location coordinates.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }

        // Initialize Places.
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        // Create a new Places client instance.
        val placesClient: PlacesClient = Places.createClient(this)

        val autocompleteFragment: AutocompleteSupportFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setHint("Search house's address")

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME,
            Place.Field.ADDRESS, Place.Field.LAT_LNG)).setCountry("IT")

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object: PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                Log.i("HouseActivity", "Place: " + p0.name + ", " + p0.id
                        + ", " + p0.latLng?.latitude + ", " + p0.address)

                // We remove the current Marker
                lastMarker?.remove()
                // Then we place the new Marker and save the Place
                val selectedLocation = p0.latLng
                selectedLocation?.let { placeMarkerOnMap(it) }
                text_address_map.setText(p0.address)
                lastPlace = p0
            }
            override fun onError(p0: Status) {
                Log.i("HouseActivity", "An error occurred: $p0")
            }
        })

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val buttonCreateHouse: Button = findViewById(R.id.button_create_house)
        buttonCreateHouse.setOnClickListener {
            Log.d("HouseActivity", "${text_name_house.text}" +
                    " + ${lastPlace?.name} + ${lastPlace?.latLng?.latitude} + ${lastPlace?.latLng?.longitude}" +
                    " + ${lastPlace?.address} + ${text_telephone.text}")
            if (text_name_house.text.toString().isNullOrEmpty() || lastPlace == null) {
                Toast.makeText(this, "Some fields are missing. Please check them.", Toast.LENGTH_SHORT).show()
            } else {
                startCompleteActivity(text_name_house.text.toString(),
                    lastPlace?.name.toString(), lastPlace?.latLng?.latitude.toString(),
                    lastPlace?.latLng?.longitude.toString(), lastPlace?.address.toString(),
                    text_telephone.text.toString())
            }
        }

    }

     // Manipulates the map once available.
     // This callback is triggered when the map is ready to be used.
     // This is where we can add markers or lines, add listeners or move the camera. In this case,
     // we just add a marker near Sydney, Australia.
     // If Google Play services is not installed on the device, the user will be prompted to install
     // it inside the SupportMapFragment. This method will only be triggered once the user has
     // installed Google Play services and returned to the app.
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        setUpMap()
    }

    override fun onMarkerClick(p0: Marker?) = false

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // isMyLocationEnabled = true enables the my-location layer which draws a light blue dot on
        // the user’s location. It also adds a button to the map that, when tapped, centers
        // the map on the user’s location.
        map.isMyLocationEnabled = true

        // fusedLocationClient.getLastLocation() gives you the most recent location
        // currently available.
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // If you were able to retrieve the the most recent location, then move the camera
            // to the user’s current location.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)

        lastMarker = map.addMarker(markerOptions)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
    }

    // Override AppCompatActivity’s onActivityResult() method and start the update request
    // if it has a RESULT_OK result for a REQUEST_CHECK_SETTINGS request.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                Log.i( "HouseActivity","Place: " + place.name + ", " + place.id)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status: Status = Autocomplete.getStatusFromIntent(data!!)
                Log.i("HouseActivity", status.statusMessage!!)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                TODO()
                // The user canceled the operation.
            }
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        startFirstUsageActivityBack()
    }
}
