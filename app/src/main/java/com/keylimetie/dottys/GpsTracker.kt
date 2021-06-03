package com.keylimetie.dottys

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.math.BigDecimal
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlin.properties.Delegates


@Suppress("DEPRECATED_IDENTITY_EQUALS")
open class GpsTracker(private val mContext: DottysBaseActivity) : Service(),
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
    private var locationGpsManager: LocationManager? = null
    fun getLocation(): Location? {
        try {
            // locationObserver = DottysLocationObserver(mContext)

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
          // canGetLocation = false
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

        alertDialog.setTitle("Allow Background\nLocation Access")
        // Setting Dialog Message
        alertDialog.setCancelable(false)
        alertDialog.setMessage(locationMsgFormatedText())
        // On pressing Settings button
        alertDialog.setNeutralButton("Settings",
            DialogInterface.OnClickListener { dialog, which ->
                val intent =
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                mContext.startActivity(intent)
            })

        // on pressing cancel button
        alertDialog.setPositiveButton("Continue",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        alertDialog.show()
    }

    override fun onLocationChanged(locationGps: Location) {
        Log.d("LCOATION  ",
            "** ON GPS TRACKER Lat -- $locationGps?.latitude // Long -- $locationGps?.latitud")
        if (BigDecimal(locationGps.latitude).setScale(1,
                1) != BigDecimal(mContext.lastKnownLatitudeGps).setScale(1, 1) ||
            BigDecimal(locationGps.longitude).setScale(1,
                1) != BigDecimal(mContext.lastKnownLongitudeGps).setScale(1, 1)
        ) {
            mContext.lastKnownLatitudeGps = locationGps.latitude
            mContext.lastKnownLongitudeGps = locationGps.longitude
            locationObserver?.locationListener = locationGps
        }

    }

    fun locationMsgFormatedText(): Spanned {
val text1 = "Dotty’s requires access to your device location in the background in order to recognize the device’s entry into secured Dotty’s store locations.\nTo enable location permissions in the background:\n1. Tap on “Continue” on this dialog box.\n2. Tap on “Allow in settings” in the next prompt. This will open the location permission screen for Dotty’s.\n3. Select “Allow all the time” on the screen."
val text2 = "This app collects location data to enable<br>to recognize the device’s entry<br>into secured Dotty’s store locations,<br>both while using the app and<br>when the app is closed."
val text3 = "1. Tap on <b>“Continue”</b> on this dialog box."
val text4 = "2. Tap on <b>“Allow in settings”</b> in the next prompt. This will open the location<br>permission screen for Dotty’s."
val text5 = "3. Select <b>“Allow all the time”</b> on the screen."
val text6 = "This app collects location data to<br>enable GPS tracking in order to recognize the device’s entry into secured<br>Dotty’s store locations, even when the<br>app is closed or not in use"
val text7 = "To function properly, Dotty’s requires<br>access to location in order to recognize<br>the device’s proximity and entry into<br>Dotty’s store locations."

       // val boldText = Html.fromHtml("$text2<br><br>$text3<br>$text4<br>$text5<br>")
        return Html.fromHtml("$text6<br><br>$text7")
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(
        provider: String,
        status: Int,
        extras: Bundle,
    ) {
        //  locationObserver?.locationListener = locationGps
    }


    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    companion object {
        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 200 // 200 meters

        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 30)  // 30 minutes
            .toLong()
    }

    init {
//        if (ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
//            ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            showSettingsAlert()
//        }


        getLocation()
    }
}


/* LOCATION CHANGE PROTOCOL */
//region
interface DottysLocationChangeDelegates {
    fun onLocationChangeHandler(locationGps: Location?)

}


class DottysLocationObserver(lisener: DottysLocationChangeDelegates) {
    val location: Location? = null
    var locationListener: Location? by Delegates.observable(
        initialValue = location,
        onChange = { prop, old, new -> lisener.onLocationChangeHandler(new) })
}

fun DottysBaseActivity.locationMsgFormatedText(): String {
     val firts  = "This app collects location data\nto enable GPS tracking in order\nto recognize the device’s entry into\nsecured Dotty’s store locations,\neven when the app is closed or not in use."
     val second = "To function properly, Dotty’s\nrequires access to location in order\nto recognize the device’s proximity\nand entry into Dotty’s store locations."
     return "\n$firts\n\n$second\n\n"
}
