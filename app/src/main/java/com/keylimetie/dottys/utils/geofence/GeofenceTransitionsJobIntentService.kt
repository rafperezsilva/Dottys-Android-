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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import com.keylimetie.dottys.ui.locations.DottysLocationsStoresModel
import com.keylimetie.dottys.ui.locations.DottysStoresLocation
import kotlin.collections.ArrayList

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
class GeofenceTransitionsJobIntentService: JobIntentService() {
    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */
    var stores: ArrayList<DottysStoresLocation>? = null

    override fun onHandleWork(intent: Intent) {
        val beacons = intent.getStringExtra("beacon_data")
        val beacondata = DottysLocationsStoresModel.fromLocationJson(beacons ?: return)
        stores = beacondata.locations
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(this,
                geofencingEvent.errorCode)
            Log.e(TAG, errorMessage!!)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                triggeringGeofences)

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails, geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            Log.i(TAG, geofenceTransitionDetails)
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition))
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString = getTransitionString(geofenceTransition)

        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = ArrayList<String?>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)
        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    fun getCurrentStore(storeID: String, isFirtsLocation: Boolean):DottysStoresLocation{
        if(isFirtsLocation) {
            return stores?.filter { it.id == storeID.split(": ")[1] }?.first() ?: DottysStoresLocation()
        } else {
            return stores?.get(1) ?: DottysStoresLocation()
        }
    }

    private fun sendNotification(notificationDetails: String, hasEnterInRegion: Boolean) {
        val store =  getCurrentStore(notificationDetails, true)
        val secondStore =  getCurrentStore(notificationDetails, false)
        val notificationTitle = if(hasEnterInRegion) "Welcome ${store?.name}" else "See you soon"
        val notificationDescription = if(hasEnterInRegion) "You have a ${store?.storeType?.name} store nearby, on ${store?.address1}, ${store?.distance} miles away"
                                              else "You got another ${secondStore?.storeType?.name} store, on ${secondStore?.address1}, ${secondStore?.distance} miles away"

        // Get an instance of the Notification manager
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel)
        }

        // Create an explicit content Intent that starts the main Activity.
        val notificationIntent = Intent(baseContext, DottysGeofence::class.java)

        // Construct a task stack.
        val stackBuilder = TaskStackBuilder.create(this)

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(DottysMainNavigationActivity::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack.
        val notificationPendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // Get a notification builder that's compatible with platform versions >= 4
        val builder = NotificationCompat.Builder(this)

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.dottys_notification_icon) // In a real app, you may want to use a library like Volley
            // to decode the Bitmap.
            .setLargeIcon(BitmapFactory.decodeResource(resources,
                R.mipmap.ic_launcher))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVibrate(LongArray(3))
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationDescription))
            .setColor(baseContext.resources.getColor(R.color.colorPrimary))
            .setContentTitle(notificationTitle)
            .setContentIntent(notificationPendingIntent)

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID) // Channel ID
        }

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true)

        // Issue the notification
        mNotificationManager.notify(0, builder.build())
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> getString(R.string.geofence_transition_entered)
            Geofence.GEOFENCE_TRANSITION_EXIT -> getString(R.string.geofence_transition_exited)
            else -> getString(R.string.unknown_geofence_transition)
        }
    }

    companion object {
        private const val JOB_ID = 573
        private const val TAG = "GeofenceTransitionsIS"
        private const val CHANNEL_ID = "channel_01"

        /**
         * Convenience method for enqueuing work in to this service.
         */
        fun enqueueWork(context: Context?, intent: Intent?) {
            enqueueWork(context!!,
                GeofenceTransitionsJobIntentService::class.java,
                JOB_ID,
                intent!!)
        }
    }
}