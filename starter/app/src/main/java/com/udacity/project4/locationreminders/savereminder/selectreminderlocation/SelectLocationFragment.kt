package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
const val LOCATION_PERMISSION_INDEX = 0
const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
const val DEFAULT_RADIUS_IN_METRES = 300f
val runningQOrLater =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback,
    OnMyLocationButtonClickListener {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding
    private var map: GoogleMap? = null
    private var selectedLocationMarker: Marker? = null
    private var selectedLocationCircle: Circle? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        setUpMap()

        binding.btnSave.setOnClickListener {
            onLocationSelected()
        }

        _viewModel.selectedLocation.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                selectedLocationMarker?.position = it.latLng
                selectedLocationCircle?.center = it.latLng
                setCameraTo(it.latLng)
                addMarkerCurrentLocation(it.latLng)
            }
        })

        return binding.root
    }

    private fun setUpMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun onLocationSelected() {
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map?.setOnMyLocationButtonClickListener(this)

        map?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style
            )
        )
        _viewModel.selectedPOI.value.let {
            if (it == null) {
                if (foregroundLocationPermissionApproved()) {
                    checkDeviceLocationSettings()
                    map?.isMyLocationEnabled = true
                } else {
                    requestForegroundLocationPermissions()
                }
            }
        }
        drawCircleOnMap()

        map?.setOnMapClickListener {
            _viewModel.setSelectedLocation(it)
        }

        map?.setOnPoiClickListener {
            _viewModel.setSelectedLocation(it)
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        checkDeviceLocationSettings()
        return false
    }

    private fun addMarkerCurrentLocation(latLng: LatLng) {
        selectedLocationMarker?.remove()
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(getString(R.string.dropped_pin))
            .draggable(true)

        selectedLocationMarker = map?.addMarker(markerOptions)
    }

    private fun drawCircleOnMap() {
        val circleOptions = CircleOptions()
            .center(map?.cameraPosition?.target)
            .fillColor(ResourcesCompat.getColor(resources, R.color.geo_fence_fill_color, null))
            .strokeColor(ResourcesCompat.getColor(resources, R.color.geo_fence_stork_color, null))
            .strokeWidth(4f)
            .radius(DEFAULT_RADIUS_IN_METRES.toDouble())

        selectedLocationCircle = map?.addCircle(circleOptions)
    }

    @SuppressLint("MissingPermission")
    private fun moveToMyLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    val locationLatLng = LatLng(location.latitude, location.longitude)
                    _viewModel.setSelectedLocation(locationLatLng)
                } else {
                    _viewModel.setSelectedLocation(
                        PointOfInterest(
                            map?.cameraPosition?.target,
                            null,
                            null
                        )
                    )
                }

            }

    }

    private fun setCameraTo(latLng: LatLng) {
        val cameraPosition =
            CameraPosition.fromLatLngZoom(latLng, 15f)
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)

        map?.animateCamera(cameraUpdate)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == RESULT_OK) {
                checkDeviceLocationSettings()
            } else {
                checkDeviceLocationSettings(false)
            }
        }
    }

    private fun foregroundLocationPermissionApproved(): Boolean =
        PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )


    private fun requestForegroundLocationPermissions() {
        if (foregroundLocationPermissionApproved())
            return

        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE


        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_LONG
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                moveToMyLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
        ) {
            // Permission denied.
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()

        } else {
            checkDeviceLocationSettings()
            map?.isMyLocationEnabled = true
        }
    }
}
