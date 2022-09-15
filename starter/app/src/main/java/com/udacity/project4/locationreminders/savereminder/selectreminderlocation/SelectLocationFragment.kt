package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

const val DEFAULT_RADIUS_IN_METRES = 300f

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
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


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style
            )
        )
        _viewModel.selectedPOI.value.let {
            _viewModel.setSelectedLocation(
                it ?: PointOfInterest(map?.cameraPosition?.target, null, null)
            )

            if (it == null) {
                moveToMyLocation()
            }
        }
        addGeoFence()

        map?.setOnMapClickListener {
            _viewModel.setSelectedLocation(it)
        }

        map?.setOnPoiClickListener {
            _viewModel.setSelectedLocation(it)
        }
    }

    private fun addMarkerCurrentLocation(latLng: LatLng) {
        selectedLocationMarker?.remove()
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(getString(R.string.dropped_pin))
            .draggable(true)

        selectedLocationMarker = map?.addMarker(markerOptions)
    }

    private fun addGeoFence() {
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
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val criteria = Criteria()
        val provider = locationManager!!.getBestProvider(criteria, true)
        val location = locationManager.getLastKnownLocation(provider!!)
        if (location != null) {
            val locationLatLng = LatLng(location.latitude, location.longitude)
            _viewModel.setSelectedLocation(locationLatLng)
            map?.isMyLocationEnabled = true
        }
    }

    private fun setCameraTo(latLng: LatLng) {
        val cameraPosition =
            CameraPosition.fromLatLngZoom(latLng, 15f)
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)

        map?.animateCamera(cameraUpdate)
    }
}
