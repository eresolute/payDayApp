package com.fh.payday.views2.bottombar.location

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.ExpandableListView
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.BranchLocator
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.isEmptyList
import com.fh.payday.viewmodels.LocatorViewModel
import com.fh.payday.views2.locator.BranchLocationAdapter
import com.fh.payday.views2.shared.custom.PermissionsDialog
import com.fh.payday.views2.shared.custom.ProgressDialogFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_branch_atm.*
import kotlinx.android.synthetic.main.bottombar.*
import java.util.*

class BranchATMActivity : BaseActivity(), OnMapReadyCallback, ExpandableListView.OnGroupCollapseListener,
        ExpandableListView.OnGroupExpandListener {

    private lateinit var mapFragment: SupportMapFragment
    private var map: GoogleMap? = null
    private lateinit var listView: ExpandableListView
    private lateinit var listAdapter: BranchLocationAdapter
    private var branchLocators: List<BranchLocator> = ArrayList()
    private lateinit var viewModel: LocatorViewModel
    private lateinit var mToolbarTitle: TextView
    private lateinit var progressBar: ProgressBar
    private var isProcessing = false

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            sortFHLocations(locationResult.lastLocation)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addObserver()
        val type = intent?.extras?.getString("issue") ?: return

        if (type == "branch")
            setToolbarTitle(getString(R.string.branch_locations))
        else
            setToolbarTitle(getString(R.string.atm_locations))

        getLocations(type)


        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    private fun getLocations(type: String) {
        hideNoInternetView()
        if (!isNetworkConnected()) {
            return showNoInternetView { getLocations(type) }
        }
        viewModel.locators("preLogin", "", "", "", 0, type)

    }

    override fun getLayout(): Int {
        return R.layout.activity_branch_atm
    }

    override fun init() {

        attachListeners()
        mToolbarTitle = findViewById(R.id.toolbar_title)

        findViewById<View>(R.id.toolbar_help).visibility = View.INVISIBLE
        findViewById<View>(R.id.toolbar_back).setOnClickListener { onBackPressed() }
        progressBar = findViewById(R.id.progress_bar)
        listView = findViewById(R.id.expandedList)
        viewModel = ViewModelProviders.of(this).get(LocatorViewModel::class.java)

    }

    private fun setToolbarTitle(title: String) {
        mToolbarTitle.text = title
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == LOCATION_SETTINGS_FLAG)
            sortFHLocationsNearby()
        else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) return

        when (grantResults[0]) {
            PackageManager.PERMISSION_GRANTED -> {
                if (requestCode == LOCATION_REQUEST_CODE) {
                    sortFHLocationsNearby()
                }
            }
            PackageManager.PERMISSION_DENIED -> {
                finish()
            }
        }
    }

    override fun showProgress(
            message: String,
            cancellable: Boolean,
            dismissListener: ProgressDialogFragment.OnDismissListener
    ) {
        isProcessing = true
        progress_layout.visibility = View.VISIBLE
        if (message.isEmpty()) {
            tv_progress_message.apply {
                visibility = View.GONE
                text = ""
            }
            return
        }

        tv_progress_message.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    override fun hideProgress() {
        isProcessing = false
        progress_layout.visibility = View.GONE
    }

    private fun sortFHLocations(location: Location) {

        val latLng = LatLng(location.latitude, location.longitude)
        viewModel.userLatLng = latLng
        sortFHLocations(latLng)
    }

    private fun sortFHLocations(latLng: LatLng) {
        hideProgress()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        val data = viewModel.getFHBranches(latLng)
        onSuccessFHLocation(data)
    }

    private fun onSuccessFHLocation(data: List<BranchLocator>?) {
        data ?: return
        setAddresses(data)
        branchLocators = data

        if (branchLocators.isEmpty()) return

        val latLngBounds = LatLngBounds.Builder()

        branchLocators.forEach { branch ->
            val latLng = LatLng(branch.latitude, branch.longitude)
            map?.addMarker(MarkerOptions().position(latLng).title(branch.branchName))
            latLngBounds.include(latLng)
        }
        map?.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 100))
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun showPermissionDialog() {
        PermissionsDialog.Builder()
                .setTitle(getString(R.string.location_permission_title))
                .setDescription(getString(R.string.location_permission_description))
                .setNegativeText(getString(R.string.app_settings))
                .setPositiveText(getString(R.string.not_now))
                .addNegativeListener {
                    finish()
                }
                .build()
                .show(this)
    }

    private fun sortFHLocationsNearby() {

        val userLatLng = viewModel.userLatLng
        if (userLatLng != null) {
            return sortFHLocations(userLatLng)
        }

        if (!isGpsEnabled())
            return showLocationSettings()

        if (!checkPlayServices()) return

        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                return showPermissionDialog()
            }

            return ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE
            )
        }
        showProgress(getString(R.string.locating_your_device))
        fusedLocationClient.requestLocationUpdates(
                createLocationRequest(),
                locationCallback,
                null
        )
    }

    private fun showLocationSettings() {
        val locationRequest = createLocationRequest()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            sortFHLocationsNearby()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(this, LOCATION_SETTINGS_FLAG)
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.message?.let { onFailure(findViewById(R.id.root_view), it) }
                }
            }
        }
    }

    private fun setAddresses(locators: List<BranchLocator>) {

        if (locators.isEmpty()) {
            no_location_view.visibility = View.VISIBLE
            listView.visibility = View.INVISIBLE
            return
        }
        no_location_view.visibility = View.GONE
        listView.visibility = View.VISIBLE
        listAdapter = BranchLocationAdapter(locators)
        listView.setAdapter(listAdapter)

        listView.setOnGroupExpandListener(this)
        listView.setOnGroupCollapseListener(this)
    }


    public override fun onResume() {
        super.onResume()
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

//        for (locator in branchLocators) {
//            if (locator.latitude != null && locator.longitude != null) {
//                val place = LatLng(locator.latitude, locator.longitude)
//                googleMap.addMarker(MarkerOptions().position(place).title(locator.branchName))
//            }
//        }
    }

    override fun onGroupCollapse(groupPosition: Int) {
        if (map != null) {
            val place = LatLng(branchLocators[groupPosition].latitude, branchLocators[groupPosition].longitude)
            map?.addMarker(MarkerOptions().position(place).title(branchLocators[groupPosition].branchName))
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 18f))
        }
    }

    override fun onGroupExpand(groupPosition: Int) {
        if (map != null) {
            val place = LatLng(branchLocators[groupPosition].latitude, branchLocators[groupPosition].longitude)
            map?.addMarker(MarkerOptions().position(place).title(branchLocators[groupPosition].branchName))
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 18f))
        }
    }

    private fun addObserver() {

        viewModel.locatorState.observe(this, Observer {
            val state = it?.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress()
                return@Observer
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    if (state.data != null && !isEmptyList(state.data)) {
                        sortFHLocationsNearby()
                        //setAddresses(state.data)
                        //branchLocators = state.data

                        //Repositions Camera to first questions in the list
//                        if (branchLocators[0].latitude != null && branchLocators[0].longitude != null && map != null)
//                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(branchLocators[0].latitude, branchLocators[0].longitude), 13f))

                    }
                }
                is NetworkState2.Error -> {
                    val error = state as NetworkState2.Error<*>
                    onError(error.message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.root_view), getString(R.string.request_error))
                }
                else -> {
                    onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
                }
            }
        })
    }

    private fun attachListeners() {
        btm_menu_currency_conv.setOnClickListener(this)
        btm_menu_faq.setOnClickListener(this)
        btm_menu_how_to_reg.setOnClickListener(this)
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1011
        private const val LOCATION_SETTINGS_FLAG = 101

    }
}