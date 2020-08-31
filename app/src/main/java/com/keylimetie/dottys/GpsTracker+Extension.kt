package com.keylimetie.dottys

import com.google.android.gms.maps.model.LatLng

fun GpsTracker.getLocation(gps: GpsTracker): LatLng? {
    //  gpsTracker = GpsTracker(activity);
    if (gps.canGetLocation()) {
        val latitude = this.getLatitude()
        val longitude = this.getLongitude()
        return LatLng(latitude, longitude)
    } else {
        gps.showSettingsAlert()
    }
    return LatLng(41.8563329, -87.84881410)
}
