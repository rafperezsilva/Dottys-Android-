package com.keylimetie.dottys.ui.locations

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arlib.floatingsearchview.FloatingSearchView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.GpsTracker
import com.keylimetie.dottys.R
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set
import kotlin.math.roundToInt
import kotlin.properties.Delegates


class LocationsViewModel : ViewModel() {
    var stateMapHeigth = 0
    var activityMain: DottysMainNavigationActivity? = DottysMainNavigationActivity()
    var locationsStores: ArrayList<DottysStoresLocation>? = null
    var locationDataObserver: DottysLocationStoresObserver? = null
    var fragmentMap = DottysLocationsMapFragment()
    var locationFragment = LocationsFragment()
    var rootView: View? = null
    var searchView: FloatingSearchView? = null

    fun initLocationView(
        locationFragment: LocationsFragment,
        activityDrawing: DottysMainNavigationActivity,
        rootView: View
    ) {
        this.locationFragment = locationFragment
        locationDataObserver = DottysLocationStoresObserver(locationFragment)
        this.rootView = rootView

        val gpsTracker = locationFragment.context?.let { GpsTracker(it) }
        var locationUser = gpsTracker?.let {
            locationFragment.context?.let { it1 ->
                activityMain?.getLocation(
                    it,
                    it1
                )
            }
        }

        searchView =
            rootView.findViewById<FloatingSearchView>(R.id.search_store_view)
        searchView?.setOnQueryChangeListener(FloatingSearchView.OnQueryChangeListener { oldQuery, newQuery -> //get suggestions based on newQuery
            println("$oldQuery / $newQuery")
            // locationsStores?.let { filterQueryData(it,newQuery.toString()) }
            if (newQuery == "") {
                hideKeyboard()
                screenDimensionManager(LocationViewType.EXPANDED_TYPE)
            } else {
                screenDimensionManager(LocationViewType.SEARCH_TYPE)
            }
            locationsStores?.let { filterQueryData(it, newQuery.toString()) }?.let {
                initExpandableList(
                    activityDrawing,
                    it
                )
            }
        })
        getLocationsDottysRequest(
            activityDrawing,
            locationUser?.latitude.toString(),
            locationUser?.longitude.toString()
        ,locationFragment)
    }

    fun filterQueryData(
        locations: ArrayList<DottysStoresLocation>,
        query: String
    ): List<DottysStoresLocation> {
        var flocatinoList = locations.filter {
            it.address1?.toLowerCase()?.contains(query) ?: false ||
                    it.city?.toLowerCase()?.contains(query) ?: false ||
                    it.zip?.toLowerCase()?.contains(query) ?: false
        }
        return flocatinoList
    }

    @SuppressLint("ServiceCast")
    fun initExpandableList(context: Context, locations: List<DottysStoresLocation>) {
        var listView = rootView?.findViewById<ExpandableListView>(R.id.list_stores)
        var expandableAdapter: DottysLocationsStoreAdapter? = null
        expandableAdapter = context.let {
            DottysLocationsStoreAdapter(
                it,
                locations as ArrayList<DottysStoresLocation>,
                fragmentMap
            )
        }
        listView?.setAdapter(expandableAdapter)
        listView?.setOnGroupExpandListener {
            for (item in 0..locations.size) {
                if (it != item) {
                    listView.collapseGroup(item)
                }
            }
            hideKeyboard()
            screenDimensionManager(LocationViewType.EXPANDED_TYPE)
        }

        listView?.setOnGroupCollapseListener {
            for (item in 0..locations.size) {
                if (listView.isGroupExpanded(item)) {
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
        fragmentMap.markersList = markersDottysLocation(locations)
        fragmentMap.lcoationStore = locations as ArrayList<DottysStoresLocation>

        System.err.println("OnCreate end")
        fragmentMap.getMapAsync(fragmentMap)
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
        mContext: DottysMainNavigationActivity,
        latitude: String,
        longitude: String, fragment:Fragment
    ) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader(mContext)
/*MOCK LOCATION */
        val locationURL =
            "locations?distance=150&limit=100&page=1&page=1&latitude=" + latitude + "&longitude=" + longitude
     //    val locationURL =
            "locations?distance=150&limit=100&page=1&page=1&latitude=41.603161&longitude=-87.753459300000003"
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            mContext.baseUrl +locationURL,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    mContext.hideLoader(mContext)

                    var user: DottysLocationsStoresModel =
                        DottysLocationsStoresModel.fromLocationJson(
                            response.toString()
                        )
                    locationDataObserver?.dottysLocationsModel = user
                    user.locations?.let { initExpandableList(mContext, it) }
                    locationsStores = user.locations as ArrayList<DottysStoresLocation>?
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext.hideLoader(mContext)
                    Toast.makeText(mContext, "Has no nearest locations", Toast.LENGTH_LONG).show()
                        .run {
//                        if ( locationFragment.fragmentManager?.backStackEntryCount ?: 0 > 0) {
//                            locationFragment.fragmentManager?.popBackStack()
//                        }
                            if(fragment  is LocationsFragment) {
                                val intent =
                                    Intent(mContext, DottysMainNavigationActivity::class.java)
                                mContext.startActivity(intent)
                            }
                        }

                }
            }) { //no semicolon or coma


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
                params["Authorization"] = mContext.getUserPreference().token!!
                return params
            }

        }
        mQueue.add(jsonObjectRequest)
    }
}

interface DottysLocationDelegates {
    fun getStoresLocation(locations: DottysLocationsStoresModel)
    fun allItemsCollapse(isColappse: Boolean)
}

class DottysLocationStoresObserver(lisener: DottysLocationDelegates) {
    var dottysLocationsModel: DottysLocationsStoresModel by Delegates.observable(
        initialValue = DottysLocationsStoresModel(),
        onChange = { prop, old, new -> lisener.getStoresLocation(new) })
    var colapseItems: Boolean by Delegates.observable(
        initialValue = false,
        onChange = { prop, old, new -> lisener.allItemsCollapse(new) })

}