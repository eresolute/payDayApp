package com.fh.payday.views2.locator

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
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.BranchLocator
import com.fh.payday.datasource.models.IntlBranchLocator
import com.fh.payday.datasource.models.IsoAlpha3
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.BitmapUtils
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnCountrySelectListener
import com.fh.payday.viewmodels.LocatorViewModel
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import com.fh.payday.views2.shared.custom.PermissionsDialog
import com.fh.payday.views2.shared.custom.ProgressDialogFragment
import com.fh.payday.views2.shared.custom.showCountries
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_branch_location.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.fragment_beneficiary_details.*
import kotlinx.android.synthetic.main.search_branches.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.concurrent.TimeUnit


class BranchLocationActivity : BaseActivity(), OnMapReadyCallback,
    ExpandableListView.OnGroupCollapseListener,
    ExpandableListView.OnGroupExpandListener {

    private lateinit var mapFragment: SupportMapFragment
    private var map: GoogleMap? = null
    private lateinit var listView: ExpandableListView
    private lateinit var listAdapter: BranchLocationAdapter
    private lateinit var intlBranchAdapter: IntlBranchLocationAdapter
    private var branchLocators: List<BranchLocator> = ArrayList()
    private var intlBranchLocators: List<IntlBranchLocator> = ArrayList()
    private var intlBranchLocatorsDropDown: List<IntlBranchLocator> = ArrayList()
    private lateinit var type: String
    private lateinit var user: User
    private var isProcessing = false

    private val disposable by lazy { CompositeDisposable() }

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            sortUAEXLocations(locationResult.lastLocation)
        }
    }

    private val locationCallback2 = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            sortFHLocations(locationResult.lastLocation)
        }
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(LocatorViewModel::class.java)
    }

    private val marker by lazy {
        try {
            BitmapUtils.bitmapDescriptorFromVector(this, R.drawable.ic_exchange_house_marker)
        } catch (e: IllegalArgumentException) {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btm_home.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)
        toolbar_back.setOnClickListener(this)
        toolbar_help.setOnClickListener(this)
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        fusedLocationClient.removeLocationUpdates(locationCallback2)
        disposable.dispose()
        super.onDestroy()
    }

    override fun getLayout() = R.layout.activity_branch_location

    override fun init() {
        user = UserPreferences.instance.getUser(this) ?: return logout()

        listView = findViewById(R.id.expandedList)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        handleToolBar()

        when (intent?.extras?.getString("issue")) {
            TYPE_UAE_EXCHANGES -> initUAEXView()
            else -> initNormalView()
        }
    }

    private fun initUAEXView() {
        til_search.hint = null
        et_search.hint = resources.getString(R.string.branch_location)
        til_emirates.hint = null
        et_emirates.hint = resources.getString(R.string.select_emirates)
        filter_layout.visibility = View.VISIBLE
        intlBranchAdapter = IntlBranchLocationAdapter(ArrayList())
        listView.setAdapter(intlBranchAdapter)
        et_search.setOnEditorActionListener { _, _, _ ->
            hideKeyboard()
            true
        }
        et_search.setDrawableClickListener {
            hideKeyboard()
        }
        addExchangeObserver()
        et_emirates.setOnClickListener {
            showCityDialog()
        }
    }

    private fun initNormalView() {
        filter_layout.visibility = View.GONE
        addLocationsObserver()
    }

    public override fun onResume() {
        super.onResume()
        et_search.text = null
        et_emirates.text = null
        mapFragment.getMapAsync(this)
        fetchLocations(intent?.extras?.getString("issue"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == LOCATION_SETTINGS_FLAG) {
            sortUAEXLocationsNearby()
        } else if (resultCode == Activity.RESULT_OK && requestCode == LOCATION_SETTINGS_FLAG2)
            sortFHLocationsNearby()
        else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty()) return

        when (grantResults[0]) {
            PackageManager.PERMISSION_GRANTED -> {
                if (requestCode == LOCATION_REQUEST_CODE) {
                    sortUAEXLocationsNearby()
                } else if (requestCode == LOCATION_REQUEST_CODE2) {
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

    private fun handleToolBar() {
        when (intent?.extras?.getString("issue", "") ?: return) {
            TYPE_BRANCHES -> setToolbarTitle(getString(R.string.branch_locations))
            TYPE_ATM -> setToolbarTitle(getString(R.string.atm_locations))
            TYPE_UAE_EXCHANGES -> setToolbarTitle(getString(R.string.locate_uae_exchange))
            TYPE_AL_ANSARI -> setToolbarTitle(getString(R.string.locate_al_ansari))
        }
    }

    private fun setToolbarTitle(title: String) {
        toolbar_title.text = title
    }

    private fun fetchLocations(type: String?) {
        this.type = type ?: return

        hideNoInternetView()
        if (!isNetworkConnected()) {
            return showNoInternetView { fetchLocations(type) }
        }

        when (type) {
            TYPE_UAE_EXCHANGES -> {
                val allowed = intent.getBooleanExtra("allowed", true)
                if (!allowed) {
                    viewModel.locateExchange(
                        user.token, user.refreshToken, user.refreshToken,
                        user.customerId.toLong()
                    )
                } else
                    viewModel.locateExchangeRx(
                        user.token, user.refreshToken, user.refreshToken,
                        user.customerId.toLong()
                    )
            }
            else -> {
                viewModel.locators(
                    "postLogin", user.token, user.sessionId,
                    user.refreshToken, user.customerId.toLong(), type
                )
            }
        }
    }

    private fun sortFHLocationsNearby() {
        val userLatLng = viewModel.userLatLng
        if (userLatLng != null) {
            return sortFHLocations(userLatLng)
        }


        if (!isGpsEnabled())
            return showLocationSettings("FH")

        if (!checkPlayServices()) return

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                return showPermissionDialog()
            }

            return ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE2
            )
        }

        showProgress(getString(R.string.locating_your_device))

        fusedLocationClient.requestLocationUpdates(
            createLocationRequest(),
            locationCallback2,
            null
        )
    }

    private fun sortFHLocations(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        viewModel.userLatLng = latLng
        sortFHLocations(latLng)
    }

    private fun sortFHLocations(latLng: LatLng) {
        hideProgress()

        fusedLocationClient.removeLocationUpdates(locationCallback2)
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

    private fun sortUAEXLocationsNearby() {
        attachFilterListener()
        val userLatLng = viewModel.userLatLng
        if (userLatLng != null) {
            return sortUAEXLocations(userLatLng)
        }


        if (!isGpsEnabled())
            return showLocationSettings("UAEX")

        if (!checkPlayServices()) return

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
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

    private fun sortUAEXLocations(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        viewModel.userLatLng = latLng
        sortUAEXLocations(latLng)
    }

    private fun sortUAEXLocations(latLng: LatLng) {
        hideProgress()

        fusedLocationClient.removeLocationUpdates(locationCallback)
        val data = viewModel.getUAEXBranches(latLng)
        onSuccessUAEXLocation(data)
    }

    private fun showLocationSettings(officeName: String) {
        val locationRequest = createLocationRequest()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            if ("FH".equals(officeName, true))
                sortFHLocationsNearby()
            else
                sortUAEXLocationsNearby()
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    if ("FH".equals(officeName, true))
                        e.startResolutionForResult(
                            this@BranchLocationActivity,
                            LOCATION_SETTINGS_FLAG2
                        )
                    else
                        e.startResolutionForResult(
                            this@BranchLocationActivity,
                            LOCATION_SETTINGS_FLAG
                        )
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.message?.let { onFailure(findViewById(R.id.root_view), it) }
                }
            }
        }
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

    private fun setAddresses(locators: List<BranchLocator>) {
        if (locators.isEmpty()) {
            no_location_view.text = getString(R.string.no_location_availible)
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

    private fun setIntlAddresses(
        locators: List<IntlBranchLocator>,
        emptyListMessage: String = getString(R.string.no_location_availible)
    ) {
        if (locators.isEmpty()) {
            no_location_view.text = emptyListMessage
            no_location_view.visibility = View.VISIBLE
            listView.visibility = View.INVISIBLE
            return
        }
        no_location_view.visibility = View.GONE
        listView.visibility = View.VISIBLE
        intlBranchAdapter.filter(locators)

        listView.setOnGroupExpandListener(this)
        listView.setOnGroupCollapseListener(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    override fun onGroupCollapse(groupPosition: Int) {
        if (type == TYPE_UAE_EXCHANGES) {
            if (map != null) {
                val place = LatLng(
                    intlBranchLocators[groupPosition].latitude,
                    intlBranchLocators[groupPosition].longitude
                )
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 7f))
            }
        } else {
            if (map != null) {
                val place = LatLng(
                    branchLocators[groupPosition].latitude,
                    branchLocators[groupPosition].longitude
                )
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 7f))
            }
        }

    }

    override fun onGroupExpand(groupPosition: Int) {
        if (type == TYPE_UAE_EXCHANGES) {
            if (map != null) {
                val place = LatLng(
                    intlBranchLocators[groupPosition].latitude,
                    intlBranchLocators[groupPosition].longitude
                )
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 15f))
            }
        } else {
            if (map != null) {
                val place = LatLng(
                    branchLocators[groupPosition].latitude,
                    branchLocators[groupPosition].longitude
                )
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 7f))
            }
        }

    }

    private fun addLocationsObserver() {
        viewModel.locatorState.observe(this, Observer {
            val state = it?.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress()
                return@Observer
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> sortFHLocationsNearby() //onSuccess(state.data)
                is NetworkState2.Error -> {
                    val error = state as NetworkState2.Error<*>
                    if (error.isSessionExpired)
                        return@Observer onSessionExpired(error.message)
                    onError(error.message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view), state.throwable)
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addExchangeObserver() {
        viewModel.intlLocatorState.observe(this, Observer {
            val state = it?.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress()
                return@Observer
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> sortUAEXLocationsNearby()
                is NetworkState2.Error -> {
                    val error = state as NetworkState2.Error<*>
                    if (error.isSessionExpired)
                        return@Observer onSessionExpired(error.message)
                    onError(error.message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view), state.throwable)
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun onSuccessUAEXLocation(data: List<IntlBranchLocator>?) {
        data ?: return
        setIntlAddresses(data)
        intlBranchLocators = data
        intlBranchLocatorsDropDown = data

        if (intlBranchLocators.isEmpty()) {
            no_location_view.text = getString(R.string.no_location_availible)
            no_location_view.visibility = View.VISIBLE
            listView.visibility = View.INVISIBLE
        }

        val latLngBounds = LatLngBounds.Builder()

        intlBranchLocators.forEach { branch ->
            val latLng = LatLng(branch.latitude, branch.longitude)
            map?.addMarker(MarkerOptions().position(latLng).title(branch.branchName))
                ?.setIcon(marker)
            latLngBounds.include(latLng)
        }
        map?.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 100))
    }

    private fun attachFilterListener() {
        // if (et_emirates.text.toString().isEmpty()) return
        val d = et_search.textChanges()
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribe {
                viewModel
                    .search(it.toString(), et_emirates.text.toString())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe filter@{
                        if (viewModel.filteredXBranches.isNullOrEmpty()){
                            no_location_view.text = getString(R.string.no_location_available_search)
                            no_location_view.visibility = View.VISIBLE
                            listView.visibility = View.INVISIBLE
                            return@filter
                        }
                        no_location_view.visibility = View.GONE
                        listView.visibility = View.VISIBLE
                        intlBranchLocators = ArrayList()
                        intlBranchAdapter.filter(viewModel.filteredXBranches)
                        intlBranchLocators = viewModel.filteredXBranches
                    }
            }

        val d1 = et_emirates.textChanges()
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribe {
                viewModel
                    .search(et_search.text.toString(), it.toString())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe filter@{
                        if (viewModel.filteredXBranches.isNullOrEmpty()){
                            no_location_view.text = getString(R.string.no_location_available_search)
                            no_location_view.visibility = View.VISIBLE
                            listView.visibility = View.INVISIBLE
                            return@filter
                        }
                        no_location_view.visibility = View.GONE
                        listView.visibility = View.VISIBLE
                        intlBranchLocators = ArrayList()
                        intlBranchAdapter.filter(viewModel.filteredXBranches)
                        intlBranchLocators = viewModel.filteredXBranches
                    }
            }

        disposable.add(d)
        disposable.add(d1)
    }

    private fun showCityDialog() {
        if (intlBranchLocatorsDropDown.isEmpty()) return
        val cityNames = ArrayList<IsoAlpha3>()
        intlBranchLocatorsDropDown.filter { item ->
            cityNames.add(IsoAlpha3(item.city, ""))
        }
        val filteredState = cityNames.distinct()
        showCountries(
            this,
            filteredState,
            getString(R.string.select_emirates),
            object : OnCountrySelectListener {
                override fun onCountrySelect(countryName: IsoAlpha3) {
                    et_emirates.setText(countryName.country)
                }
            },
            getString(R.string.no_emirates_found)
        )
    }

    companion object {
        private const val TYPE_BRANCHES = "branch"
        private const val TYPE_ATM = ""
        private const val TYPE_UAE_EXCHANGES = "uae_exchange"
        private const val TYPE_AL_ANSARI = "al_ansari"

        private const val LOCATION_REQUEST_CODE = 1011
        private const val LOCATION_REQUEST_CODE2 = 1012
        private const val LOCATION_SETTINGS_FLAG = 100
        private const val LOCATION_SETTINGS_FLAG2 = 101
    }

}

