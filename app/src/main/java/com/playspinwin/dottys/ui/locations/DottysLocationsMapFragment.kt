package com.playspinwin.dottys.ui.locations

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.playspinwin.dottys.DottysBaseActivity
import com.playspinwin.dottys.DottysMainNavigationActivity
import com.playspinwin.dottys.GpsTracker
import com.playspinwin.dottys.R
import com.playspinwin.dottys.utils.getLocation

class DottysLocationsMapFragment : SupportMapFragment(), OnMapReadyCallback,
    DottysStoreListDelegates {
    private lateinit var mMap: GoogleMap
    private var mapIsReady = false
    var initialLatitude: Double? = 41.8563329
    var initialLongitude: Double? = -87.8488141
    var initialMarker = "Seed nay"
    var markersList = ArrayList<MarkerOptions>()
    var locationStore = ArrayList<DottysStoresLocation>()
    var markerPosition: Marker? = null

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)

        attrs
        context
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.MyyMap, 0, 0
        )

        try {
            val providedLatitude =
                typedArray.getFloat(R.styleable.MyyMap_latitude, initialLatitude?.toFloat() ?: 0f)
            initialLatitude = providedLatitude.toDouble()

            val providedLongitude =
                typedArray.getFloat(R.styleable.MyyMap_longitude, initialLongitude?.toFloat() ?: 0f)
            initialLongitude = providedLongitude.toDouble()

            val providedMarker =
                typedArray.getString(R.styleable.MyyMap_marker)
            providedMarker?.apply { initialMarker = providedMarker }

        } catch (e: Exception) {
            DottysBaseActivity().showSnackBarMessage(activity as DottysMainNavigationActivity,e.toString())
        }


        typedArray.recycle()
    }


    override fun onMapReady(map: GoogleMap?) {
        System.err.println("OnMapReady start")
        mMap = map as GoogleMap
        var maxMarkers = 0
//       if (markersList.size >= 30){
//           maxMarkers = 30
//       } else {
        maxMarkers = markersList.size - 1
        //  }
        //       var sydney: LatLng? = null
        for (locationPosition in 0..maxMarkers) {
//            sydney = locationStore[locationPosition].longitude?.let {
//                locationStore[locationPosition].latitude?.let { it1 ->
//                    LatLng(
//                        it1,
//                        it
//                    )
//                }
//            }
            mMap.addMarker(markersList[locationPosition])//sydney?.let { MarkerOptions().position(it) })
        }
        mapIsReady = true
        updateMarker()
    }

    fun updateMarker() {
        if (!mapIsReady) {
            return
        }
        val gps = GpsTracker(activity as DottysMainNavigationActivity)
        val loc = gps.getLocation(gps)
        val currentPositionMarker = loc ?: return
        markerPosition?.remove()
        markerPosition = mMap.addMarker(currentPositionMarker.let {
            MarkerOptions().position(it)
        })
//        // mMap.addMarker(MarkerOptions().position(sydney).title(initial_marker).icon(BitmapDescriptorFactory.fromResource(R.mipmap.cash_image)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPositionMarker, 14.0f))
    }

    override fun onItemSelected(location: DottysStoresLocation) {
        val loc = location.longitude?.let { location.latitude?.let { it1 -> LatLng(it1, it) } }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f))
    }


}
