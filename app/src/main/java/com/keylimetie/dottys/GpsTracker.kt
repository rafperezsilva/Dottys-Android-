package com.keylimetie.dottys

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager

import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import com.keylimetie.dottys.ui.dashboard.DottysDashboardDelegates
import kotlin.properties.Delegates

class GpsTracker(private val mContext: DottysMainNavigationActivity) : Service(),
    LocationListener {
    var locationObserver: DottysLocationObserver? = null
    // flag for GPS status
    var isGPSEnabled = false

    // flag for network status
    var isNetworkEnabled = false

    // flag for GPS status
    var canGetLocation = false
    var locationGps: Location? = null
    var latitudeGps = 0.0
    var longitudeGps = 0.0

    // Declaring a Location Manager
    protected var locationGpsManager: LocationManager? = null
    fun getLocation(): Location? {
        try {
            locationObserver = DottysLocationObserver(mContext)

            locationGpsManager =
                mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // getting GPS status
            isGPSEnabled = locationGpsManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // getting network status
            isNetworkEnabled = locationGpsManager!!
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                canGetLocation = true
                // First get locationGps from Network Provider
                if (isNetworkEnabled) {
                    //check the network permission
                    if (ActivityCompat.checkSelfPermission(
                            mContext,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            mContext,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) !== PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            mContext as Activity,
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ),
                            101
                        )
                    }
                    locationGpsManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                    )
                    Log.d("Network", "Network")
                    if (locationGpsManager != null) {
                        locationGps = locationGpsManager!!
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (locationGps != null) {
                            latitudeGps = locationGps!!.latitude
                            longitudeGps = locationGps!!.longitude
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (locationGps == null) {
                        //check the network permission
                        if (ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) !== PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                mContext as Activity,
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ),
                                101
                            )
                        }
                        locationGpsManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                        )
                        Log.d("GPS Enabled", "GPS Enabled")
                        if (locationGpsManager != null) {
                            locationGps = locationGpsManager!!
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (locationGps != null) {
                                latitudeGps = locationGps!!.latitude
                                longitudeGps = locationGps!!.longitude
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return locationGps
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    fun stopUsingGPS() {
        if (locationGpsManager != null) {
            locationGpsManager!!.removeUpdates(this@GpsTracker)
        }
    }

    /**
     * Function to get latitudeGps
     */
    fun getLatitude(): Double {
        if (locationGps != null) {
            latitudeGps = locationGps!!.latitude
        }
        // return latitudeGps
        return latitudeGps
    }

    /**
     * Function to get longitudeGps
     */
    fun getLongitude(): Double {
        if (locationGps != null) {
            longitudeGps = locationGps!!.longitude
        }
        // return longitudeGps
        return longitudeGps
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     */
    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    fun showSettingsAlert() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(mContext)
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings")
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
            DialogInterface.OnClickListener { dialog, which ->
                val intent =
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                mContext.startActivity(intent)
            })
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        alertDialog.show()
    }

    override fun onLocationChanged(locationGps: Location) {
        print("Lat -- $locationGps?.latitude // Long -- $locationGps?.latitud")
        locationObserver?.locationListener = locationGps
    }
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(
        provider: String,
        status: Int,
        extras: Bundle
    ) {
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    companion object {
        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters

        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1) / 4 // 1 minute
            .toLong()
    }

    init {
        getLocation()
    }
}


/* LOCATION CHANGE PROTOCOL */
//region
interface DottysLocationDelegates {
    fun onLocationChangeHandler(locationGps: Location?)

}


class DottysLocationObserver(lisener: DottysLocationDelegates) {
    val location: Location? =  null
    var locationListener: Location? by Delegates.observable(
        initialValue = location,
        onChange = { prop, old, new -> lisener.onLocationChangeHandler(new) })
}