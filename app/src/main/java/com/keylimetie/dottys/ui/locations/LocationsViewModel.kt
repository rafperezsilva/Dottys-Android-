package com.keylimetie.dottys.ui.locations

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ExpandableListView
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.keylimetie.dottys.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set
import kotlin.math.roundToInt
import kotlin.properties.Delegates


class LocationsViewModel(val contextMain: DottysBaseActivity) : ViewModel(),
    DottysLocationChangeDelegates {
    private var listStoresExpandable: ExpandableListView? = null

    // var activityMain: DottysBaseActivity? = contextMain
    var locationsStores: ArrayList<DottysStoresLocation>? = null
    var locationDataObserver: DottysLocationStoresObserver? = null
    var fragmentMap = DottysLocationsMapFragment()
    var locationFragment = LocationsFragment()
    var rootView: View? = null
    var locationUser: Location? = null
    private var searchView: SearchView? = null

    fun initLocationView(
        locationFragment: LocationsFragment,
        activityDrawing: DottysMainNavigationActivity,
        rootView: View,
    ) {
        this.locationFragment = locationFragment
        locationDataObserver = DottysLocationStoresObserver(locationFragment)
        this.rootView = rootView
        contextMain.locationsBaseViewModel = this

        val gpsTracker =
            locationFragment.context?.let { GpsTracker(it as DottysMainNavigationActivity) }
        gpsTracker?.locationObserver = DottysLocationObserver(this)
        locationUser = gpsTracker?.let {
            locationFragment.context?.let { _ ->
                //activityMain?.getLocation(
                it.getLocation()
                //)
            }
        }
        gpsTracker?.locationObserver = DottysLocationObserver(this)
        searchView =
            rootView.findViewById<SearchView>(R.id.search_store_view)


        searchView?.setOnCloseListener {
            hideKeyboard()
            screenDimensionManager(LocationViewType.COLLAPSE_TYPE)
            true
        }
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }


            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == "") {
                    hideKeyboard()
                    screenDimensionManager(LocationViewType.EXPANDED_TYPE)
                } else {
                    screenDimensionManager(LocationViewType.SEARCH_TYPE)
                }
                locationsStores?.let { filterQueryData(it, newText.toString()) }?.let {
                    fragmentMap.updateMarker()
                    initExpandableList(
                        activityDrawing,
                        it
                    )
                }
                return true
            }

        })
        locationDataObserver = DottysLocationStoresObserver(locationFragment)
        getLocationsDottysRequest(
            activityDrawing,
            locationUser?.latitude.toString(),
            locationUser?.longitude.toString())
    }

    fun filterQueryData(
        locations: ArrayList<DottysStoresLocation>,
        query: String,
    ): List<DottysStoresLocation> {
        return locations.filter {
            it.address1?.toLowerCase()?.contains(query) ?: false ||
                    it.city?.toLowerCase()?.contains(query) ?: false ||
                    it.zip?.toLowerCase()?.contains(query) ?: false
        }
    }

    fun isAllGroupCollapsed(): Boolean {
        for (group in locationsStores?.indices ?: return false) {
            if (listStoresExpandable?.isGroupExpanded(group) == true) return false
        }
        return true
    }

    @SuppressLint("ServiceCast")
    fun initExpandableList(context: Context, locations: List<DottysStoresLocation>) {
        listStoresExpandable = rootView?.findViewById<ExpandableListView>(R.id.list_stores)

        var expandableAdapter: DottysLocationsStoreAdapter? = null
        expandableAdapter = context.let {
            DottysLocationsStoreAdapter(this,
                it,
                locations as ArrayList<DottysStoresLocation>,
                fragmentMap
            )
        }
        listStoresExpandable?.setAdapter(expandableAdapter)
        listStoresExpandable?.setOnGroupExpandListener {
            for (item in 0..locations.size) {
                if (it != item) {
                    listStoresExpandable?.collapseGroup(item)
                }
            }
            hideKeyboard()
            screenDimensionManager(LocationViewType.EXPANDED_TYPE)
        }

        listStoresExpandable?.setOnGroupCollapseListener {
            for (item in 0..locations.size) {
                if (listStoresExpandable?.isGroupExpanded(item) == true) {
                    locationDataObserver?.colapseItems = false
                }
            }
            locationDataObserver?.colapseItems = true
        }

    }


    fun hideKeyboard() {
        val imm: InputMethodManager =
            locationFragment.activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(locationFragment.view?.windowToken, 0)
    }

    fun heightToViewType(viewType: LocationViewType): Double {
        var screenHeigth = locationFragment.resources.displayMetrics?.heightPixels ?: 0
        when (viewType) {
            LocationViewType.COLLAPSE_TYPE -> {
                return screenHeigth * 0.45
            }

            LocationViewType.EXPANDED_TYPE -> {
                return screenHeigth * 0.3
            }

            LocationViewType.SEARCH_TYPE -> {
                return 1.0
            }
        }

    }

    fun screenDimensionManager(viewType: LocationViewType) {

        var mapParams = fragmentMap.view?.layoutParams
        mapParams?.height = heightToViewType(viewType).roundToInt()
    }

    fun initMapWHitMarker(locations: List<DottysStoresLocation>) {
        fragmentMap =
            locationFragment.childFragmentManager.findFragmentById(R.id.map_view_fragment) as DottysLocationsMapFragment
        contextMain.mapFragmentBase = fragmentMap
        fragmentMap.markersList = markersDottysLocation(locations)
        fragmentMap.locationStore = locations as ArrayList<DottysStoresLocation>
        fragmentMap.initialLatitude = locationUser?.latitude ?: 41.8563329
        fragmentMap.initialLongitude = locationUser?.longitude ?: -87.8488141
        fragmentMap.getMapAsync(fragmentMap)

        System.err.println("OnCreate end")

    }

    private fun markersDottysLocation(locations: List<DottysStoresLocation>): ArrayList<MarkerOptions> {
        var markerLocations = ArrayList<MarkerOptions>()
        for (location in locations) {
            val coordinates = location.latitude?.let {
                location.longitude?.let { it1 ->
                    LatLng(
                        it,
                        it1
                    )
                }
            }
            val height = 75
            val width = 155
            val bitmapdraw =
                fragmentMap.context?.resources?.getDrawable(R.mipmap.dottys_image) as BitmapDrawable
            val b = bitmapdraw.bitmap
            val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
            coordinates?.let {
                MarkerOptions().position(it).title(location.storeType?.name + "\n" + location.name)
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
            }?.let {
                markerLocations.add(
                    it
                )
            }
        }
        return markerLocations
    }

    fun getLocationsDottysRequest(
        mContext: DottysBaseActivity,//DottysMainNavigationActivity
        latitude: String,
        longitude: String,
    ) {
//        if(BigDecimal(latitude.toDouble()).setScale(1,1) == BigDecimal(mContext.lastKnownLatitudeGps).setScale(1,1) &&
//           BigDecimal(longitude.toDouble()).setScale(1,1) == BigDecimal(mContext.lastKnownLongitudeGps).setScale(1,1)){
//            return
//        }
        if(mContext.getUserPreference().token.isNullOrEmpty()){return}
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.isUpdatingLocation = true
        mContext.showLoader()
/*MOCK LOCATION */
        val locationURL =
            "locations?distance=25&limit=100&page=1&page=1&latitude=$latitude&longitude=$longitude"
        // val locationURL = "locations?distance=150&limit=300&page=1&page=1&latitude=40.0998935&longitude=-87.6357274"

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            mContext.baseUrl + locationURL,
            null,
            Response.Listener<JSONObject> { response ->

                mContext.hideLoader()
                var stores: DottysLocationsStoresModel =
                    DottysLocationsStoresModel.fromLocationJson(
                        response.toString()
                    )
                val filterLocations =  stores.locations?.filter { it.company == CompanyType.Dotty }
                stores.locations = filterLocations as ArrayList<DottysStoresLocation>?
                Log.d("LOCATIONS RESPONSE -->",stores.toJson())
                mContext.saveDataPreference(PreferenceTypeKey.LOCATIONS, stores.toJson())
                locationDataObserver?.dottysLocationsModel = stores
                stores.locations?.let { initExpandableList(mContext, it) }
                locationsStores = stores.locations
                //mContext.geofencesAtStore = stores.locations?.let { DottysGeofenceActivity(mContext, it) }    /** TODO ADD GEOFENCING **/

                mContext.isUpdatingLocation = false
            },
            Response.ErrorListener {
                mContext.hideLoader()
                mContext.isUpdatingLocation = false
                locationDataObserver?.dottysLocationsModel = mContext.getUserNearsLocations()
            }) {


            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["latitude"] = latitude
                params["longitude"] = longitude
                params["limit"] = "150"
                params["page"] = "1"
                params["distance"] = "50"
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getUserPreference().token ?: ""
                return params
            }

        }
        mQueue.add(jsonObjectRequest)
    }


    override fun onLocationChangeHandler(locationGps: Location?) {
        getLocationsDottysRequest(contextMain,
            locationGps?.latitude.toString(),
            locationGps?.longitude.toString())
    }
}

interface DottysLocationDelegates {
    fun getStoresLocation(locations: DottysLocationsStoresModel)
    fun allItemsCollapse(isColappse: Boolean)
}

class DottysLocationStoresObserver(lisener: DottysLocationDelegates) {
    var dottysLocationsModel: DottysLocationsStoresModel by Delegates.observable(
        initialValue = DottysLocationsStoresModel(),
        onChange = { _, _, new -> lisener.getStoresLocation(new) })
    var colapseItems: Boolean by Delegates.observable(
        initialValue = false,
        onChange = { _, _, new -> lisener.allItemsCollapse(new) })

}