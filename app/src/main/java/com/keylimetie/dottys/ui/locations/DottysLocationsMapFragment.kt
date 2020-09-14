package com.keylimetie.dottys.ui.locations

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.keylimetie.dottys.R

class DottysLocationsMapFragment : SupportMapFragment(), OnMapReadyCallback,
    DottysStoreListDelegates {
    private lateinit var mMap: GoogleMap

    var initial_latitude = -34.0
    var initial_longitude = 151.0
    var initial_marker = "Seed nay"
    var markersList = ArrayList<MarkerOptions>()
    var lcoationStore = ArrayList<DottysStoresLocation>()


    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)

        attrs
        context
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.MyyMap, 0, 0
        )

        try {
            val provided_latitude =
                typedArray.getFloat(R.styleable.MyyMap_latitude, initial_latitude.toFloat())
            initial_latitude = provided_latitude.toDouble()

            val provided_longitude =
                typedArray.getFloat(R.styleable.MyyMap_longitude, initial_longitude.toFloat())
            initial_longitude = provided_longitude.toDouble()

            val provided_marker =
                typedArray.getString(R.styleable.MyyMap_marker)
            provided_marker?.apply { initial_marker = provided_marker }

        } catch (e: Exception) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
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
        var sydney: LatLng? = null
        for (locationPosition in 0..maxMarkers) {
            sydney = lcoationStore[locationPosition].longitude?.let {
                lcoationStore[locationPosition].latitude?.let { it1 ->
                    LatLng(
                        it1,
                        it
                    )
                }
            }
            mMap.addMarker(markersList[locationPosition])//sydney?.let { MarkerOptions().position(it) })
        }
        val sydney2 = LatLng(41.587138, -88.309830)
        mMap.addMarker(sydney2.let {
            MarkerOptions().position(it)
        })
        // mMap.addMarker(MarkerOptions().position(sydney).title(initial_marker).icon(BitmapDescriptorFactory.fromResource(R.mipmap.cash_image)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney2, 10.0f))
    }

    override fun onItemSelected(location: DottysStoresLocation) {
        val loc = location.longitude?.let { location.latitude?.let { it1 -> LatLng(it1, it) } }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12.0f))
    }


}
