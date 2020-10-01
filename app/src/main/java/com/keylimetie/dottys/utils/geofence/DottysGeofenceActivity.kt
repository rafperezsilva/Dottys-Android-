/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keylimetie.dottys.utils.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.keylimetie.dottys.BuildConfig
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.ui.locations.DottysStoresLocation
import com.keylimetie.dottys.utils.geofence.DottysGeofenceActivity
import com.keylimetie.dottys.utils.geofence.GeofenceErrorMessages.getErrorString

/**
 * Demonstrates how to create and remove geofences using the GeofencingApi. Uses an IntentService
 * to monitor geofence transitions and creates notifications whenever a device enters or exits
 * a geofence.
 *
 *
 * This sample requires a device's Location settings to be turned on. It also requires
 * the ACCESS_FINE_LOCATION permission, as specified in AndroidManifest.xml.
 *
 *
 */
class DottysGeofenceActivity(
    activity: DottysBaseActivity,
    storeList: ArrayList<DottysStoresLocation>
) : OnCompleteListener<Void?> {
    private val baseActivity = activity
    private val storeDottysList = storeList

    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum class PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    /**
     * Provides access to the Geofencing API.
     */
    private var mGeofencingClient: GeofencingClient? = null

    /**
     * The list of geofences used in this sample.
     */
    private var mGeofenceList: ArrayList<Geofence>? = null

    /**
     * Used when requesting to add or remove geofences.
     */
    private var mGeofencePendingIntent: PendingIntent? = null

    // Buttons for kicking off the process of adding or removing geofences.
    private var mAddGeofencesButton: Button? = null
    private var mRemoveGeofencesButton: Button? = null
    private var mPendingGeofenceTask = PendingGeofenceTask.NONE

    init {
        onCreateGeofence()
    }
    private fun onCreateGeofence() {

        // Empty list for storing geofences.
        mGeofenceList = getGeofencesStores()

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null
        //setButtonsEnabledState()

        // Get the geofences used. Geofence data is hard coded in this sample.
       // populateGeofenceList()
        mGeofencingClient = LocationServices.getGeofencingClient(baseActivity)
        mPendingGeofenceTask = PendingGeofenceTask.ADD
        onStartGeofece()
    }


    private fun onStartGeofece() {

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            performPendingGeofenceTask()
        }
    }


    /**
     * Build an array for Id's and Lat Long
     * for the 15 nearest Dotty's stores
     */
    private fun getGeofencesStores(): ArrayList<Geofence> {
        val geofences = ArrayList<Geofence>()

        for (store in storeDottysList) {

            val locationGPS =
                store.latitude?.let { store.longitude?.let { it1 -> LatLng(it, it1) } }
            val geofence = Geofence.Builder()
                .setRequestId(store.regionID) // Geofence ID
                .setCircularRegion(
                    locationGPS?.latitude ?: return geofences,
                    locationGPS.longitude,
                    750f
                ) // defining fence region
                .setExpirationDuration(Geofence.NEVER_EXPIRE)// expiring date
                .setLoiteringDelay(10000)
                // Transition types that it should look for
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
            if (geofences.count() >= 15) {
                return geofences
            }
            geofences.add(geofence)
        }
        return geofences
    }


    // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
    // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
    // is already inside that geofence.

    // Add the geofences to be monitored by geofencing service.

    // Return a GeofencingRequest.
    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private val geofencingRequest: GeofencingRequest
        private get() {
            val builder = GeofencingRequest.Builder()

            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

            // Add the geofences to be monitored by geofencing service.
            builder.addGeofences(mGeofenceList)

            // Return a GeofencingRequest.
            return builder.build()
        }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
//    fun addGeofencesButtonHandler(view: View?) {
//        if (!checkPermissions()) {
//            mPendingGeofenceTask = PendingGeofenceTask.ADD
//            requestPermissions()
//            return
//        }
//        addGeofences()
//    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun addGeofences() {
        if (!checkPermissions()) {
            showSnackbar(baseActivity.getString(R.string.insufficient_permissions))
            return
        }
        if (ActivityCompat.checkSelfPermission(
                baseActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            showSnackbar(
                R.string.permission_denied_explanation, R.string.settings
            ) { // Build intent that displays the App settings screen.
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                baseActivity.startActivity(intent)


            }
            return
        }
        mGeofencingClient!!.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnCompleteListener(this)
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
//    fun removeGeofencesButtonHandler(view: View?) {
//        if (!checkPermissions()) {
//            mPendingGeofenceTask = PendingGeofenceTask.REMOVE
//            requestPermissions()
//            return
//        }
//        removeGeofences()
//    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences() {
        if (!checkPermissions()) {
            showSnackbar(baseActivity.getString(R.string.insufficient_permissions))
            return
        }
        mGeofencingClient!!.removeGeofences(geofencePendingIntent).addOnCompleteListener(this)
    }

    /**
     * Runs when the result of calling [.addGeofences] and/or [.removeGeofences]
     * is available.
     * @param task the resulting Task, containing either a result or error.
     */
    override fun onComplete(task: Task<Void?>) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE
        if (task.isSuccessful) {
            updateGeofencesAdded(!geofencesAdded)
           // setButtonsEnabledState()
            val messageId: Int =
                if (geofencesAdded) R.string.geofences_added else R.string.geofences_removed
            Toast.makeText(baseActivity, baseActivity.getString(messageId), Toast.LENGTH_SHORT)
                .show()
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            val errorMessage = getErrorString(baseActivity, task.exception)
            Log.w(TAG, errorMessage)
        }
    }// Reuse the PendingIntent if we already have it.
    // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
    // addGeofences() and removeGeofences().
    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private val geofencePendingIntent: PendingIntent?
        private get() {
            // Reuse the PendingIntent if we already have it.
            if (mGeofencePendingIntent != null) {
                return mGeofencePendingIntent
            }
            val intent = Intent(baseActivity, GeofenceBroadcastReceiver::class.java)
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            mGeofencePendingIntent =
                PendingIntent.getBroadcast(
                    baseActivity,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            return mGeofencePendingIntent
        }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
//    private fun populateGeofenceList() {
//        for (value in this.mGeofenceList ?: return) {
//            mGeofenceList!!.add(
//                Geofence.Builder() // Set the request ID of the geofence. This is a string to identify this
//                    // geofence.
//                    .setRequestId(key) // Set the circular region of this geofence.
//                    .setCircularRegion(
//                        value.latitude,
//                        value.longitude,
//                        Constants.GEOFENCE_RADIUS_IN_METERS
//                    ) // Set the expiration duration of the geofence. This geofence gets automatically
//                    // removed after this period of time.
//                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS) // Set the transition types of interest. Alerts are only generated for these
//                    // transition. We track entry and exit transitions in this sample.
//                    .setTransitionTypes(
//                        Geofence.GEOFENCE_TRANSITION_ENTER or
//                                Geofence.GEOFENCE_TRANSITION_EXIT
//                    ) // Create the geofence.
//                    .build()
//            )
//        }
//    }

    /**
     * Ensures that only one button is enabled at any time. The Add Geofences button is enabled
     * if the user hasn't yet added geofences. The Remove Geofences button is enabled if the
     * user has added geofences.
     */
//    private fun setButtonsEnabledState() {
//        if (geofencesAdded) {
//            mAddGeofencesButton!!.isEnabled = false
//            mRemoveGeofencesButton!!.isEnabled = true
//        } else {
//            mAddGeofencesButton!!.isEnabled = true
//            mRemoveGeofencesButton!!.isEnabled = false
//        }
//    }

    /**
     * Shows a [Snackbar] using `text`.
     *
     * @param text The Snackbar text.
     */
    private fun showSnackbar(text: String) {
        val container = baseActivity.findViewById<View>(R.id.content)
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * Shows a [Snackbar].
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private fun showSnackbar(
        mainTextStringId: Int, actionStringId: Int,
        listener: View.OnClickListener
    ) {
        Snackbar.make(
            baseActivity.findViewById(R.id.content),
            baseActivity.getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(baseActivity.getString(actionStringId), listener).show()
    }

    /**
     * Returns true if geofences were added, otherwise false.
     */
    private val geofencesAdded: Boolean
        private get() = PreferenceManager.getDefaultSharedPreferences(baseActivity).getBoolean(
            Constants.GEOFENCES_ADDED_KEY, false
        )

    /**
     * Stores whether geofences were added ore removed in [SharedPreferences];
     *
     * @param added Whether geofences were added or removed.
     */
    private fun updateGeofencesAdded(added: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(baseActivity)
            .edit()
            .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
            .apply()
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    private fun performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeofences()
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            removeGeofences()
        }
    }



    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            baseActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            baseActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(
                R.string.permission_rationale, R.string.app_name
            ) { // Request permission
                ActivityCompat.requestPermissions(
                    baseActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE
                )
            }
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                baseActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.")
                performPendingGeofenceTask()
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(
                    R.string.permission_denied_explanation, R.string.settings
                ) { // Build intent that displays the App settings screen.
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    baseActivity.startActivity(intent)


                }
                mPendingGeofenceTask = PendingGeofenceTask.NONE
            }
        }
    }

    companion object {
        private val TAG = DottysGeofenceActivity::class.java.simpleName
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}