package com.keylimetie.dottys.beacon_service

import android.content.Context
import android.util.Log
import com.estimote.sdk.Beacon
import com.estimote.sdk.BeaconManager
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
  var mainNavActivity: DashboardFragment? = null //DottysMainNavigationActivity()
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
    beaconManager?.setMonitoringListener(object : BeaconManager.MonitoringListener {
      override fun onEnteredRegion(region: Region, list: List<Beacon>) {
         // serviceInternalBeaconObserver = dashboardFragment?.let { DottysBeaconServiceObserver(it) }
         // baseVC.getBeaconAtStoreLocation().fil
          observer = mainNavActivity?.let { DottysBeaconActivityObserver(it) }
         var beaconList = baseActivity.getBeaconStatus() ?: DottysBeaconArray(baseActivity.getBeaconAtStoreLocation())
         var currentBeacon = DottysBeacon()
          for (beacon in beaconList.beaconArray!!) {
              if (beacon.id == region.identifier){
                  beacon.isConected = true
                  currentBeacon = beacon
              }
          }
          Log.d("PROGRESS -- ", baseActivity.progressBar?.visibility.toString())
          Log.d(region.identifier, "<<======== ENTER ON BEACON ==============")
          if(baseActivity.progressBar?.visibility != 0) {
              beaconViewModel.recordBeacon(
                  baseActivity, DottysBeaconRequestModel(
                      currentBeacon.id,
                      currentBeacon.uuid,
                      currentBeacon.major,
                      currentBeacon.minor, BeaconEventType.ENTER.name
                  )
              )
              observer?.listOfBeacons = DottysBeaconArray(beaconList.beaconArray)
          }

      }



      override fun onExitedRegion(region: Region) {
          observer = mainNavActivity?.let { DottysBeaconActivityObserver(it) }
          var beaconList = baseActivity.getBeaconStatus() ?: DottysBeaconArray(baseActivity.getBeaconAtStoreLocation())
          var currentBeacon = DottysBeacon()
          for (beacon in beaconList.beaconArray ?: return) {
              if (beacon.id == region.identifier){
                  beacon.isConected = false
                  currentBeacon = beacon
              }
          }
          Log.d(region.identifier, "=========== EXIT ON BEACON =============>>")
          Log.d("PROGRESS -- ", baseActivity.progressBar?.visibility.toString())
          if(baseActivity.progressBar?.visibility != 0) {
              beaconViewModel.recordBeacon(
                  baseActivity, DottysBeaconRequestModel(
                      currentBeacon.id,
                      currentBeacon.uuid,
                      currentBeacon.major,
                      currentBeacon.minor, BeaconEventType.EXIT.name
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
        onChange = { _, _, new -> listeners.onBeaconsServiceChange(new)})
}