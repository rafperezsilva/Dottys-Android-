package com.keylimetie.dottys.beacon_service

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.Log
import com.estimote.coresdk.observation.region.beacon.BeaconRegion
import com.estimote.coresdk.recognition.packets.Beacon
import com.estimote.coresdk.recognition.packets.Nearable
import com.estimote.coresdk.service.BeaconManager
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import com.keylimetie.dottys.ui.locations.DottysLocationsStoresModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


open class DottysBeaconActivity(baseActivity: DottysBaseActivity)    {
  var observer: DottysBeaconActivityObserver? = null

  var baseActivity = baseActivity
  var beaconManager: BeaconManager? = null
  val beaconViewModel = DottysBeaconViewModel()

      fun requestPermission()  {
          val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
          if (!mBluetoothAdapter.isEnabled) {
              mBluetoothAdapter.enable()
          }

      }
    fun initBeaconManager() {

        requestPermission()
    Log.d("BEACON ACTIVITY", "Beacon Service Started")
      baseActivity.sharedPreferences = baseActivity.getSharedPreferences(
          PreferenceTypeKey.USER_DATA.name,
          Context.MODE_PRIVATE
      )
     //currentBeacon =   baseActivity.getBeaconAtStoreLocation()


    if (baseActivity.getBeaconStatus()?.beaconArray?.size ?: 0 == 0) {
      Log.d("ERROR BEACON", "<<======== NULL BEACON ==============")
        val emptyStatus =  baseActivity.getBeaconsListz()?.filter { it.location?.storeNumber ?: 0 == baseActivity.getUserNearsLocations().locations?.first()?.storeNumber ?: 0} as ArrayList
        baseActivity?.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,
            DottysBeaconArray(emptyStatus).toJson())
        observer?.listOfBeacons = DottysBeaconArray(emptyStatus)
      return //START_REDELIVER_INTENT
    }

        beaconManager = BeaconManager(baseActivity)
        beaconManager?.connect {
            for (beacon in baseActivity.getBeaconStatus()?.beaconArray ?: baseActivity.getBeaconStatus()?.beaconArray ?: return@connect) {
    //          if(beacon.isConected != true) {
                    beaconManager?.startMonitoring(BeaconRegion(
                        beacon.id,
                        UUID.fromString(beacon.uuid),
                        beacon.major?.toInt(),
                        beacon.minor?.toInt()))
               // }
            }

        }


        beaconManager?.setForegroundScanPeriod(5, 30)
        beaconManager?.setBackgroundScanPeriod(5, 30)
        startDiscoveringLisener()
        starMonitoringLisener()
  }


    fun startDiscoveringLisener(){
        beaconManager?.setNearableListener(object : BeaconManager.NearableListener {
            override fun onNearablesDiscovered(nearables: MutableList<Nearable>?) {
                Log.d("BEACON ACTIVITY",
                    "Has nearable a reagion ${nearables?.first()?.identifier} beacons")
            }
        })
    }
    private fun starMonitoringLisener(){
        beaconManager?.setMonitoringListener(object : BeaconManager.BeaconMonitoringListener {


            override fun onEnteredRegion(
                beaconRegion: BeaconRegion?,
                beacons: MutableList<Beacon>?,
            ) {
                var beaconList = DottysBeaconArray()
                when {
                    baseActivity.getBeaconStatus()?.beaconArray?.size ?: 0 > 0 -> {
                        beaconList = DottysBeaconArray(baseActivity.getBeaconStatus()?.beaconArray
                            ?: ArrayList<DottysBeacon>())
                    }
                    else -> beaconList = DottysBeaconArray(baseActivity.getBeaconStatus()?.beaconArray)
                }

                var currentBeacon = DottysBeacon()
                for (beacon in beaconList.beaconArray ?: baseActivity.getBeaconStatus()?.beaconArray
                ?: return) {
                    if (beacon.id == beaconRegion?.identifier) {
                        beacon.isConected = true
                        currentBeacon = beacon
                    }
                }
                Log.d("BEACON ACTIVITY ",
                    "PROGRESS -- ${baseActivity.progressBar?.visibility.toString()}")
                Log.d("BEACON ACTIVITY ",
                    "${beaconRegion?.identifier} <<======== ENTER ON BEACON ==============")
                if (baseActivity.progressBar?.visibility != 0) {
                    beaconViewModel.recordBeacon(
                        baseActivity, DottysBeaconRequestModel(
                            beaconRegion?.identifier,
                            beaconRegion?.proximityUUID.toString(),
                            beaconRegion?.major?.toLong(),
                            beaconRegion?.minor?.toLong(), BeaconEventType.ENTER.name
                        )
                    )
                        observer?.listOfBeacons = DottysBeaconArray(beaconList.beaconArray)
                }
            }

            override fun onExitedRegion(beaconRegion: BeaconRegion?) {
                var beaconList: DottysBeaconArray
                when {
                    baseActivity.getBeaconStatus()?.beaconArray?.size ?: 0 > 0 -> {
                        beaconList = DottysBeaconArray(baseActivity.getBeaconStatus()?.beaconArray
                            ?: ArrayList<DottysBeacon>())
                    }
                    else -> beaconList = DottysBeaconArray(baseActivity.getBeaconStatus()?.beaconArray)
                }
                var currentBeacon = DottysBeacon()
                for (beacon in beaconList.beaconArray ?: return) {
                    if (beacon.id == beaconRegion?.identifier) {
                        beacon.isConected = false
                        currentBeacon = beacon
                    }
                }
                Log.d("BEACON ACTIVITY ",
                    "PROGRESS -- ${baseActivity.progressBar?.visibility.toString()}")
                Log.d("BEACON ACTIVITY ",
                    "${beaconRegion?.identifier} <<======== EXIT ON BEACON ==============")
                if (baseActivity.progressBar?.visibility != 0) {
                    beaconViewModel.recordBeacon(
                        baseActivity, DottysBeaconRequestModel(
                            beaconRegion?.identifier,
                            beaconRegion?.proximityUUID.toString(),
                            beaconRegion?.major?.toLong(),
                            beaconRegion?.minor?.toLong(), BeaconEventType.EXIT.name
                        )
                    )
                       observer?.listOfBeacons = DottysBeaconArray(beaconList.beaconArray)
                }
            }
        })
    }

    fun destroy() {
     Log.d("DESTROY ", "Beacon Service Destroyed")
   // disposable.dispose()
    beaconManager?.disconnect()
  }
}

interface DottysBeaconActivityDelegate {
    fun onBeaconsServiceChange(beaconsData: DottysBeaconArray)
}

class DottysBeaconActivityObserver(listeners: DottysBeaconActivityDelegate) {
    var listOfBeacons: DottysBeaconArray by Delegates.observable(
        initialValue = DottysBeaconArray(),
        onChange = { _, _, new -> listeners.onBeaconsServiceChange(new) })
}