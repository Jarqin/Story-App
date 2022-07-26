package com.dicoding.storyapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var user: UserModel

    private val viewModel: MapsViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    companion object {
        const val USER_EXTRA = "user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = intent.getParcelableExtra(USER_EXTRA)!!
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val map = supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        map.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        with(mMap){
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isIndoorLevelPickerEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = true
        }
        setDataStories()
        getLocation()
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setDataStories() {
        val boundsBuilder = LatLngBounds.Builder()
        viewModel.getStories(user.token).observe(this) {
            if (it != null) {
                when (it) {
                    is ResultResponse.Loading -> {
                        showLoading(true)
                    }
                    is ResultResponse.Success -> {
                        showLoading(false)
                        it.data.forEachIndexed { _, element ->
                            val lastLatLng = LatLng(element.lat, element.lon)
                            mMap.addMarker(MarkerOptions().position(lastLatLng).title(element.name))
                            boundsBuilder.include(lastLatLng)
                            val bounds: LatLngBounds = boundsBuilder.build()
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64))
                        }
                    }
                    is ResultResponse.Error -> {
                        showLoading(false)
                        Helper.showToastShort(this, getString(R.string.error_occurred))
                    }
                }
            }
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}