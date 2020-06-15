package com.keylimetie.dottys.beacon_service

import android.content.Context
import android.util.Log
import com.estimote.sdk.Beacon
import com.estimote.sdk.BeaconManager
import com.estimote.sdk.Nearable
import com.estimote.sdk.Region
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.ui.dashboard.DashboardFragment
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import java.util.*
import kotlin.properties.Delegates

open class DottysBeaconActivity(baseActivity:DottysBaseActivity)    {
  var observer: DottysBeaconActivityObserver? = null

  var baseActivity = baseActivity
  var beaconManager: BeaconManager? = null
  val beaconViewModel = DottysBeaconViewModel()

    fun initBeaconManager() {


    Log.d("BEACON ACTIVITY", "Beacon Service Started")
      baseActivity.sharedPreferences = baseActivity.getSharedPreferences(
          PreferenceTypeKey.USER_DATA.name,
          Context.MODE_PRIVATE
      )
     //currentBeacon =   baseActivity.getBeaconAtStoreLocation()


    if (baseActivity.getBeaconAtStoreLocation()?.size ?: 0 == 0) {
      Log.d("ERROR BEACON", "<<======== NULL BEACON ==============")
        val emptyStatus = DottysBeaconArray(baseActivity.getBeaconAtStoreLocation())
        baseActivity?.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,emptyStatus.toJson())
        observer?.listOfBeacons = DottysBeaconArray(emptyStatus.beaconArray)
      return //START_REDELIVER_INTENT
    }

        beaconManager = BeaconManager(baseActivity)
        beaconManager?.connect {
            for (beacon in baseActivity.getBeaconAtStoreLocation()!!) {
                if(beacon.isConected != true) {
                    beaconManager?.startMonitoring(
                        Region(
                            beacon.id,
                            UUID.fromString(beacon.uuid),
                            beacon.major?.toInt(),
                            beacon.minor?.toInt()
                        )
                    )
                }
            }

        }


        beaconManager?.setForegroundScanPeriod(5,30)
        beaconManager?.setBackgroundScanPeriod(5,30)
        startDiscoveringLisener()
        starMonitoringLisener()
  }


    fun startDiscoveringLisener(){
        beaconManager?.setNearableListener(object : BeaconManager.NearableListener {
            override fun onNearablesDiscovered(p0: MutableList<Nearable>?) {


                Log.d("BEACON ACTIVITY", "Has nearable a reagion ${p0?.first()?.region} beacons")
            }
        })
    }
    private fun starMonitoringLisener(){
        beaconManager?.setMonitoringListener(object : BeaconManager.MonitoringListener {


            override fun onEnteredRegion(region: Region, list: List<Beacon>) {
                var beaconList = DottysBeaconArray()
                when {
                    baseActivity.getBeaconStatus()?.beaconArray?.size ?: 0 > 0 -> {
                        beaconList =  DottysBeaconArray(baseActivity.getBeaconStatus()?.beaconArray ?: ArrayList<DottysBeacon>())
                    }
                    else -> beaconList =    DottysBeaconArray(baseActivity.getBeaconAtStoreLocation())
                }
               
                var currentBeacon = DottysBeacon()
                for (beacon in beaconList.beaconArray ?: baseActivity.getBeaconAtStoreLocation() ?: return) {
                    if (beacon.id == region.identifier){
                        beacon.isConected = true
                        currentBeacon = beacon
                    }
                }
                Log.d("BEACON ACTIVITY ", "PROGRESS -- ${baseActivity.progressBar?.visibility.toString()}")
                Log.d("BEACON ACTIVITY ", "${region.identifier} <<======== ENTER ON BEACON ==============")
                if(baseActivity.progressBar?.visibility != 0) {
                    beaconViewModel.recordBeacon(
                        baseActivity, DottysBeaconRequestModel(
                            region.identifier,
                            region.proximityUUID.toString(),
                            region.major.toLong(),
                            region.minor.toLong(), BeaconEventType.ENTER.name
                        )
                    )
               //     observer?.listOfBeacons = DottysBeaconArray(beaconList.beaconArray)
                }

            }



            override fun onExitedRegion(region: Region) {
                var beaconList: DottysBeaconArray
                  when {
                    baseActivity.getBeaconStatus()?.beaconArray?.size ?: 0 > 0 -> {
                        beaconList =  DottysBeaconArray(baseActivity.getBeaconStatus()?.beaconArray ?: ArrayList<DottysBeacon>())
                    }
                    else -> beaconList =    DottysBeaconArray(baseActivity.getBeaconAtStoreLocation())
                }
                var currentBeacon = DottysBeacon()
                for (beacon in beaconList.beaconArray ?: return) {
                    if (beacon.id == region.identifier){
                        beacon.isConected = false
                        currentBeacon = beacon
                    }
                }
                Log.d("BEACON ACTIVITY ", "PROGRESS -- ${baseActivity.progressBar?.visibility.toString()}")
                Log.d("BEACON ACTIVITY ", "${region.identifier} <<======== EXIT ON BEACON ==============")
                if(baseActivity.progressBar?.visibility != 0) {
                    beaconViewModel.recordBeacon(
                        baseActivity, DottysBeaconRequestModel(
                            region.identifier,
                            region.proximityUUID.toString(),
                            region.major.toLong(),
                            region.minor.toLong(), BeaconEventType.EXIT.name
                        )
                    )
               //     observer?.listOfBeacons = DottysBeaconArray(beaconList.beaconArray)
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
        onChange = { _, _, new -> listeners.onBeaconsServiceChange(new)})
}