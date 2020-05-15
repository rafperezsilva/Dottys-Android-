package com.keylimetie.dottys.beacon_service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.estimote.sdk.Beacon
import com.estimote.sdk.BeaconManager
import com.estimote.sdk.Region
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import io.reactivex.disposables.Disposable

import java.util.*
import kotlin.properties.Delegates

open class DottysBeaconService  : Service() {
  var currentBeacon: DottysBeacon? = null
  var mainNavActivity: DottysMainNavigationActivity? = null //DottysMainNavigationActivity()
  var baseActivity = DottysBaseActivity()
  //lateinit var dashboardFragment: DashboardFragment
//  companion object {
//    val TAG: String = "Beacon-Service"
//    var BEACON_REGION = "B9407F30-F5F8-466E-AFF9-25556B57FE6D"
//    var REGION_ID = "candy_beacon_xxx" //name defined in estimote cloud account
//    val BEACON_MAJOR = 251 //major from estimote cloud account beacon info
//    val BEACON_MINOR = 2 //minor from estimote cloud account beacon info
//  }


  //  val baseVC = DottysBaseActivity()
  var beaconManager: BeaconManager? = null

//  val service: ApiInterface = ApiClient().build().create(ApiInterface::class.java)
//    var disposable: Disposable? = null
//
//  override fun onCreate() {
//    super.onCreate()
//
//  }

  override fun onBind(arg0: Intent): IBinder? {
    return null
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    Log.d(currentBeacon?.beaconType?.value, "Beacon Service Started")
      baseActivity.sharedPreferences = this.getSharedPreferences(
          PreferenceTypeKey.USER_DATA.name,
          Context.MODE_PRIVATE
      )
    currentBeacon =   baseActivity.getBeaconAtStoreLocation()?.first()


    if (currentBeacon == null) {
      Log.d("ERROR BEACON", "<<======== NULL BEACON ==============")
      return START_REDELIVER_INTENT
    }

    for (beacon in baseActivity.getBeaconAtStoreLocation()!!) {
          beaconManager = BeaconManager(applicationContext)
          beaconManager?.connect {
              beaconManager?.startMonitoring(
                  Region(
                      currentBeacon?.id,
                      UUID.fromString(currentBeacon?.uuid),
                      currentBeacon?.major?.toInt(),
                      currentBeacon?.minor?.toInt()
                  )
              )
          }

      }
    beaconManager?.setMonitoringListener(object : BeaconManager.MonitoringListener {
      override fun onEnteredRegion(region: Region, list: List<Beacon>) {
         // serviceInternalBeaconObserver = dashboardFragment?.let { DottysBeaconServiceObserver(it) }
         // baseVC.getBeaconAtStoreLocation().fil
         var beaconList = baseActivity.getBeaconAtStoreLocation()
          for (beacon in beaconList!!){
              if (beacon.id == region.identifier){
                  beacon.isConected = true
              }
          }

          Log.d(currentBeacon?.beaconType?.value, "<<======== ENTER ON BEACON ==============")
          var beacons = DottysBeaconArray()
          beacons.beaconArray = beaconList
          var beaconArrayList = beacons
          //baseVC.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,beaconArrayList.toJson())
          beaconArrayList.beaconArray.let {
              if (it != null) {
              //    listenerBeaconStatus(beacons, mainNavActivity)
              }
          }
      }



      override fun onExitedRegion(region: Region) {
         // serviceInternalBeaconObserver = dashboardFragment?.let { DottysBeaconServiceObserver(it) }
          var beaconList = baseActivity.getBeaconAtStoreLocation()
          for (beacon in beaconList!!){
              if (beacon.id == region.identifier){
                  beacon.isConected = false
              }
          }
          var beacons = DottysBeaconArray()
          beacons.beaconArray = beaconList
          var beaconArrayList = beacons
          //baseVC.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,beaconArrayList.toJson())
          beaconArrayList.beaconArray.let {
              if (it != null) {
              //  listenerBeaconStatus(beacons, mainNavActivity)
              }
          }
          Log.d(currentBeacon?.beaconType?.value, "=========== EXIT ON BEACON =============>>")

//        disposable = service.beaconOut(UserRequest("email@email.com")).subscribeOn(
//            Schedulers.io()).observeOn(
//            AndroidSchedulers.mainThread()).subscribe { userResponse ->
//          showNotification("Beacon OUT: ", "Bye bye status: " + userResponse.status)
//          Log.d(TAG, "========================================> Beacon OUT: " + userResponse.status)
//        }
      }
    })


    return START_REDELIVER_INTENT
  }

    fun listenerBeaconStatus(beacons: DottysBeaconArray, delegate: DottysBeaconServiceDelegate?) {

        val textView = DottysBeaconServiceObserver().apply {
            delegate?.let { listeners.add(it) }      }

        with(textView) {
            listOfBeacons = beacons
        }


    }

//  fun showNotification(title: String, message: String) {
//    val notifyIntent = Intent(this, MainActivity::class.java)
//    notifyIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//    val pendingIntent = PendingIntent.getActivities(this, 0,
//        arrayOf(notifyIntent), PendingIntent.FLAG_UPDATE_CURRENT)
//    val notification = Notification.Builder(this)
//        .setSmallIcon(android.R.drawable.ic_dialog_info)
//        .setContentTitle(title)
//        .setContentText(message)
//        .setAutoCancel(true)
//        .setContentIntent(pendingIntent)
//        .build()
//    notification.defaults = notification.defaults or Notification.DEFAULT_SOUND
//    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    notificationManager.notify(1, notification)
//  }

  override fun onDestroy() {
      super.onDestroy()

      Log.d(currentBeacon?.beaconType?.value, "Beacon Service Destroyed")
   // disposable.dispose()
    beaconManager?.disconnect()
  }
}

interface DottysBeaconServiceDelegate {
    fun onBeaconsServiceChange(beaconsData: DottysBeaconArray)
}

class DottysBeaconServiceObserver {
    //val  beaconList = DottysBeaconArray(lisener,ArrayList<DottysBeacon>())
    val listeners = mutableListOf<DottysBeaconServiceDelegate>()
    var listOfBeacons: DottysBeaconArray by Delegates.observable(
        initialValue = DottysBeaconArray(),
        onChange = { _, _, new -> listeners})
}