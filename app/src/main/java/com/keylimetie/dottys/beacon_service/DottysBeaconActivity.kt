package com.keylimetie.dottys.beacon_service

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.estimote.coresdk.observation.region.beacon.BeaconRegion
import com.estimote.coresdk.recognition.packets.Beacon
import com.estimote.coresdk.service.BeaconManager
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.saveDataPreference
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


open class DottysBeaconActivity(baseActivity: DottysBaseActivity): BeaconManager.BeaconRangingListener {
    private var expirationBeaconConection = 0
     var observer: DottysBeaconActivityObserver? = null
    var runTask : Runnable? = null
    var baseActivity = baseActivity
    var beaconManager: BeaconManager? = null
    val beaconViewModel = DottysBeaconViewModel()
    var mainHandler: Handler? = null
    var hasBeaconAtRange: Boolean? = null
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
        mainHandler =  Handler(Looper.myLooper()!!)
        //initJobHandler()
       // mainHandler?.post(runneableAction())
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

//      Handler(Looper.myLooper()!!).postDelayed(object : Runnable {
//          override fun run() {
//              connnectBeaconManager()
//          }
//
//      } , 25000
//      )

       // connnectBeaconManager()
        beaconManager?.setForegroundScanPeriod(1000, 1000)
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
        val currenBeaconStatus = baseActivity.getBeaconStatus()?.beaconArray
        hasBeaconAtRange = if(beacons?.count() ?: 0 > 0) {
            val beaconDiscriminator = currenBeaconStatus?.filter { it.minor?.toInt() == beacons?.first()?.minor && it.isConected != true}
            val isConectable = beaconDiscriminator?.count() ?: 0 > 0
            val d = Log.d("IS CONECTABLE ", "$isConectable")
            if (isConectable) {
                onEnterOnBeacon(BeaconRegion(
                    beaconDiscriminator?.first()?.beaconIdentifier,
                    UUID.fromString(beaconDiscriminator?.first()?.uuid),
                    beaconDiscriminator?.first()?.major?.toInt(),
                    beaconDiscriminator?.first()?.minor?.toInt()))
            }
            isConectable
        } else {
            false
        }
        val mms = if((beacons?.count() ?: 0) > 0) {
          //  beaconManager?.startMonitoring(beaconRegion)
            "KEY: ${beacons?.first()?.uniqueKey}\n" +
            "MINOR: ${beacons?.first()?.minor}\n"+
                    "REGION: ${beaconRegion?.identifier} // MIN: ${beaconRegion?.minor}\n"
        }  else {
            if (currenBeaconStatus?.filter { it.beaconIdentifier == beaconRegion?.identifier && it.isConected == true }
                    ?.count() ?: 0 > 0) {
                "\nWARNING DELETE ****** \"REGION ${beaconRegion?.identifier} \\n MINOR:${beaconRegion?.minor}\"} ******"

                    disconetExpiredBeacon(BeaconRegion(beaconRegion?.identifier,
                        beaconRegion?.proximityUUID,
                        beaconRegion?.major,
                        beaconRegion?.minor))


            } else {

                "REGION NO CONECTED ${beaconRegion?.identifier} \n MINOR:${beaconRegion?.minor}"
            }
        }
        Log.i("-- BEACON LISENER -->"," \n****** BEACON DATA *****\n $mms \n*****************************************")
        expirationBeaconConection = 0
        initJobHandler()

    }

    private fun initJobHandler(){
        mainHandler?.removeCallbacks(runneableAction)
        mainHandler =  null
        mainHandler = Handler(Looper.myLooper()!!)
        mainHandler?.post(runneableAction)
    }

    private val runneableAction =   Runnable {
            expirationBeaconConection  += 1
            Log.e("EXPITARION TIME --* ", expirationBeaconConection.toString())
            val beaconAtCurrentStatus = baseActivity.getBeaconStatus()?.beaconArray
            if(expirationBeaconConection > 15 && !(baseActivity.getBeaconStatus()?.beaconArray.isNullOrEmpty())){
                beaconViewModel.isUploading = false
                val beacons = (baseActivity.getBeaconStatus()?.beaconArray?.filter { it.isConected == true })
                val beaconToConnect =  if(beacons?.count() ?: 0 > 0) beaconAtCurrentStatus?.first { it.beaconIdentifier == beacons?.first()?.beaconIdentifier } else DottysBeacon()
                when {
                    beacons?.count() ?: 0 > 0 -> {
                        val beacon = beacons?.first()
                        disconetExpiredBeacon(BeaconRegion(beacon?.beaconIdentifier,UUID.fromString(beacon?.uuid ?: ""),
                            (beacon?.major ?: 0).toInt(),(beacon?.minor ?: 0).toInt()))
                    }
                    beacons?.count() ?: 0 > 0 && beaconToConnect?.isConected != true -> {
                        Log.d(" *** WARNING *** ","\n****** CONNECT TO  ***** \n ID: ${beaconToConnect?.beaconIdentifier} / Minor: ${beaconToConnect?.minor}" )
                    }
                    beacons?.count() ?: 0 == 0 -> {

                    }
                }
                if (hasBeaconAtRange != true) {
                    expirationBeaconConection = 0
                }
            }
            runTaskAction()
        }

    private fun runTaskAction(){
        mainHandler?.postDelayed(runneableAction, 1500)
    }

   private fun disconetExpiredBeacon(beaconRegion: BeaconRegion?) {
       if(beaconViewModel.isUploading != true) {
           beaconViewModel.isUploading = true
           onExitBeaconRegion(beaconRegion)
       }

     //  beaconManager?.startMonitoring(beaconRegion)

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