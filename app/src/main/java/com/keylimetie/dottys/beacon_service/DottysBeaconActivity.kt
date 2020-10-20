package com.keylimetie.dottys.beacon_service

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.estimote.coresdk.observation.region.beacon.BeaconRegion
import com.estimote.coresdk.recognition.packets.Beacon
import com.estimote.coresdk.service.BeaconManager
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


open class DottysBeaconActivity(baseActivity: DottysBaseActivity): BeaconManager.BeaconRangingListener {
    private var expirationBeaconConection = 0
     var observer: DottysBeaconActivityObserver? = null

    var baseActivity = baseActivity
    var beaconManager: BeaconManager? = null
    val beaconViewModel = DottysBeaconViewModel()
    var mainHandler: Handler? = null
    private fun requestPermission() {
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
        if(baseActivity.getUserPreference().token.isNullOrEmpty()) { return }
        manageBeaconList()
     //currentBeacon =   baseActivity.getBeaconAtStoreLocation()

//
//    if (baseActivity.getBeaconStatus()?.beaconArray?.size ?: 0 == 0) {
//      Log.d("ERROR BEACON", "<<======== NULL BEACON ==============")
//        if (baseActivity.getDottysBeaconsList().isNullOrEmpty()) { return }
//        val emptyStatus =  baseActivity.getDottysBeaconsList()  as ArrayList
//        baseActivity?.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,
//            DottysBeaconArray(emptyStatus).toJson())
//        observer?.listOfBeacons = DottysBeaconArray(emptyStatus)
//      return //START_REDELIVER_INTENT
//    }
       connnectBeaconManager()

      Handler(Looper.myLooper()!!).postDelayed(object : Runnable {
          override fun run() {
              connnectBeaconManager()
          }

      } , 25000
      )

       // connnectBeaconManager()
        beaconManager?.setForegroundScanPeriod(5000, 3000)
        beaconManager?.setBackgroundScanPeriod(5000, 3000)
        startDiscoveringLisener()
        starMonitoringLisener()



  }

    private fun  manageBeaconList() {
        val defaultBeacons = if (baseActivity.getDottysBeaconsList().isNullOrEmpty()) { return } else {baseActivity.getDottysBeaconsList()}
        if(baseActivity.getBeaconStatus()?.beaconArray.isNullOrEmpty() ||
            baseActivity.getBeaconStatus()?.beaconArray?.first()?.major != defaultBeacons?.first()?.major){
            baseActivity?.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,
             DottysBeaconArray(defaultBeacons).toJson())
            Log.i("BEACO SERVICE", "######## NULL BEACON HAS BEEN SAVED  #######")

        }
    }

    private fun connnectBeaconManager() {
        if (beaconManager ==  null) {
            beaconManager = BeaconManager(baseActivity)
        }
        beaconManager?.connect {
            for (beacon in baseActivity.getBeaconStatus()?.beaconArray ?: baseActivity.getBeaconStatus()?.beaconArray ?: return@connect) {
                //          if(beacon.isConected != true) {
                val  regionOfBeaon = BeaconRegion(
                    beacon.id,
                    UUID.fromString(beacon.uuid),
                    beacon.major?.toInt(),
                    beacon.minor?.toInt())
                beaconManager?.startMonitoring(regionOfBeaon)
                beaconManager?.startRanging(regionOfBeaon)
                // }
            }

        }

    }


    fun startDiscoveringLisener(){
//        beaconManager?.setNearableListener(object : BeaconManager.NearableListener {
//            override fun onNearablesDiscovered(nearables: MutableList<Nearable>?) {
//                Log.d("BEACON ACTIVITY",
//                    "Has nearable a reagion ${nearables?.first()?.identifier} beacons")
//            }
//        })

        beaconManager?.setRangingListener(this)


    }
    private fun starMonitoringLisener(){
        beaconManager?.setMonitoringListener(object : BeaconManager.BeaconMonitoringListener {
            override fun onEnteredRegion(
                beaconRegion: BeaconRegion?,
                beacons: MutableList<Beacon>?,
            ) {
                onEnterOnBeacon(beaconRegion)
            }

            override fun onExitedRegion(beaconRegion: BeaconRegion?) {
                onExitBeaconRegion(beaconRegion)
            }
        })
    }

    fun destroy() {
     Log.d("DESTROY ", "Beacon Service Destroyed")
   // disposable.dispose()
    beaconManager?.disconnect()
  }

    fun onEnterOnBeacon(beaconRegion: BeaconRegion?){
        var beaconList = DottysBeaconArray()
        beaconList = when {
            baseActivity.getBeaconStatus()?.beaconArray?.size ?: 0 > 0 -> {
                DottysBeaconArray(baseActivity.getBeaconStatus()?.beaconArray
                    ?: ArrayList<DottysBeacon>())
            }
            else -> DottysBeaconArray(baseActivity.getBeaconStatus()?.beaconArray)
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

    fun onExitBeaconRegion(beaconRegion: BeaconRegion?){
        val beaconList: DottysBeaconArray
        beaconList = when {
            baseActivity.getBeaconStatus()?.beaconArray?.size ?: 0 > 0 -> {
                DottysBeaconArray(baseActivity.getBeaconStatus()?.beaconArray
                    ?: ArrayList<DottysBeacon>())
            }
            else -> DottysBeaconArray(baseActivity.getBeaconStatus()?.beaconArray)
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

    override fun onBeaconsDiscovered(beaconRegion: BeaconRegion?, beacons: MutableList<Beacon>?) {
        val mms = if((beacons?.count() ?: 0) > 0) {
          //  beaconManager?.startMonitoring(beaconRegion)
            "KEY: ${beacons?.first()?.uniqueKey}\n" +
            "MINOR: ${beacons?.first()?.minor}\n"+
                    "REGION: ${beaconRegion?.identifier} // MIN: ${beaconRegion?.minor}\n"
        }  else {
            if (baseActivity.getBeaconStatus()?.beaconArray?.filter { it.beaconIdentifier == beaconRegion?.identifier && it.isConected == true }
                    ?.count() ?: 0 > 0) {
                "\nWARNING DELETE ****** \"REGION ${beaconRegion?.identifier} \\n MINOR:${beaconRegion?.minor}\"} ******"
            } else {

                "REGION NO CONECTED ${beaconRegion?.identifier} \n MINOR:${beaconRegion?.minor}"
            }
        }
        Log.e("-- BEACON LISENER -->","****** BEACON DATA *****\n $mms \n*****************************************")
        expirationBeaconConection = 0
        if(mainHandler == null){
            mainHandler = Handler (Looper.getMainLooper())
             mainHandler?.post(object : Runnable {
            override fun run() {
                expirationBeaconConection  += 1
                Log.e("EXPITARION TIME --* ", expirationBeaconConection.toString())


                if(expirationBeaconConection > 10 && !(baseActivity.getBeaconStatus()?.beaconArray.isNullOrEmpty())){
                    val beacons = (baseActivity.getBeaconStatus()?.beaconArray?.filter { it.isConected == true })
                            if(beacons?.count() ?: 0 > 0) {
                                val beacon = beacons?.first()
                                disconetExpiredBeacon(BeaconRegion(beacon?.beaconIdentifier,UUID.fromString(beacon?.uuid ?: ""),
                                    (beacon?.major ?: 0).toInt(),(beacon?.minor ?: 0).toInt()))
                            }
//                    if(expirationBeaconConection > 15 && beacons?.count() ?: 0 == 0){
                        expirationBeaconConection = 0
                //    }
                }
                mainHandler?.postDelayed(this, 5000)
            }
        })
        }
    }

   fun disconetExpiredBeacon(beaconRegion: BeaconRegion?) {
       onExitBeaconRegion(beaconRegion)
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