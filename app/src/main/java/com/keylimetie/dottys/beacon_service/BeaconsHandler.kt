package com.keylimetie.dottys.beacon_service

import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.estimote.coresdk.observation.region.beacon.BeaconRegion
import com.estimote.coresdk.recognition.packets.Beacon
import com.estimote.coresdk.recognition.packets.Nearable
import com.estimote.coresdk.service.BeaconManager
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.saveDataPreference
import com.keylimetie.dottys.ui.dashboard.models.BeaconType
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import com.keylimetie.dottys.utils.Preferences
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class BeaconsHandler(private val context: DottysBaseActivity, private val observer: BeaconHandlerObserver?): BeaconManager.BeaconRangingListener, BeaconManager.NearableListener , BeaconManager.ScanStatusListener, BeaconManager.BeaconMonitoringListener {
    var handlerData: Handler? = null
    var handlerDataTask: Handler? = null
    var beaconManager: BeaconManager? = null
    //var beaconOnConnection = ArrayList<DottysBeacon>()
    var beaconOnDatabase = context.getDottysBeaconsList()//Preferences.getNearestBeaconsStored(context)?.beacons
    fun beaconsConnected(): ArrayList<DottysBeacon>   { return context.getBeaconStatus()?.beaconArray ?: ArrayList() }
    var taskCounter = 0
    var sheduleTimeTask: Long  = 5000

    init {
        requestPermission()
    }

    private fun requestPermission() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()

        }
    }
/*
    internal fun runeableConnectionChecker(){
//        val mainHandler = Handler(Looper.getMainLooper())
        if (handlerDataTask != null) {return}
        Log.e("RUNNEABLE", "REANENABLE CHECKER INITED")
        handlerDataTask = Handler(Looper.getMainLooper())
        handlerDataTask?.post(object : Runnable {
            override fun run() {
                context.getUserNearsLocations()?.locations?.let{
                    if(it.isNullOrEmpty()){
                        Log.e("RUNNEABLE", "REANENABLE CHECKER ** STOPED")
                        handlerDataTask?.removeCallbacks(this)
                        return@let
                    }
                    val dist = it.first().distance ?: 2.0
                    if(dist <= 1){
                        initBeaconManager()
                    } else {
                        Log.e("RUNNEABLE", "REANENABLE CHECKER ** STOPED")
                        handlerDataTask?.removeCallbacks(this)
                    }
                    if(beaconOnDatabaseConnected?.beaconArray?.filter { it.isConected == true }?.isNotEmpty() == true){
                        checkableTask()
                    }
                }
                Log.e("RUNNEABLE", "RUNNEABLE CHECKER -- ")
                handlerDataTask?.postDelayed(this, 1000*60*1)
            }
       })
    }
*/
    internal fun initBeaconManager() {
        if(context.getCurrentToken().isNullOrEmpty()){return}
        Log.d("BEACON ACTIVITY", "Beacon Service Started")
        connnectBeaconManager()
        beaconManager?.setForegroundScanPeriod(8000, 10000)
        beaconManager?.setBackgroundScanPeriod(8000, 10000)
        startDiscoveringLisener()
        starMonitoringLisener()
        /*beaconManager?.setRangingListener(BeaconManager.BeaconRangingListener { region, list ->

            if (list.size > 0) {
                Log.i("RANGING", "MINOR ** ${list.first().minor} ")
            }
        })*/
    }

    private fun startDiscoveringLisener(){
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
                beacons: MutableList<Beacon>?
            ) {
                Log.e("BEACON EVENT", "*${beacons?.size ?: 0}* ENTER *${beaconRegion?.minor ?: 0}*")
                 taskCounter = 0
            }

            override fun onExitedRegion(beaconRegion: BeaconRegion?) {
                Log.e("BEACON EVENT", "** EXIT *${beaconRegion?.minor ?: 0}*")
            }
        })
    }


    private fun checkableTask(){
//        val mainHandler = Handler(Looper.getMainLooper())
       if(handlerData != null) {return}
        handlerData = Handler(Looper.getMainLooper())
        handlerData?.post(object : Runnable {
            override fun run() {

                Log.e("TASK", "CHECKEABLE $taskCounter * $context * ")
                Log.d("CHECKEABLE", "REGISTERED ${DottysBeaconArray(beaconsConnected().filter { it.isRegistered } as ArrayList<DottysBeacon>).toJson()} * $context * ")
                if(taskCounter > 3) {


                    if (!beaconsConnected().filter { it.isRegistered && it.expiration > 0 }
                            .isNullOrEmpty()) {
                        val expiredBeacons = beaconsConnected()
                        expiredBeacons.forEach {
                            if (it.expiration > 0) {
                                if (it.isRegistered || it.isConected == true) {
                                    it.expiration += 1
                                } else {
                                    it.expiration = 0
                                }
                                if (it.isRegistered && it.expiration ?: 0 >= 4) {
                                    recordBeacon(it, BeaconEventType.EXIT)
                                }
                            }
                        }
                        context.saveDataPreference(
                            PreferenceTypeKey.BEACON_AT_CONECTION,
                            DottysBeaconArray(expiredBeacons).toJson()
                        )
                    } else if (taskCounter >= 4) {
                        if (!beaconsConnected().filter { it.isRegistered }.isNullOrEmpty()) {
                            val expiredBeacons = beaconsConnected()
                            expiredBeacons.forEach {
                                if(it.isRegistered) {
                                    it.expiration += 1
                                    it.isConected = false
                                    if (it.expiration ?: 0 >= 4) {
                                        recordBeacon(it, BeaconEventType.EXIT)
                                    }
                                }
                            }
                            context.saveDataPreference(
                                PreferenceTypeKey.BEACON_AT_CONECTION,
                                DottysBeaconArray(expiredBeacons).toJson()
                            )
                            observer?.listOfBeacons = beaconsConnected()
                        } else {
                            handlerData?.removeCallbacks(this)
                            handlerData = null
                            taskCounter = 0
                        }

                    } else if (beaconsConnected().filter { it.isRegistered }.isNullOrEmpty()) {
                        handlerData?.removeCallbacks(this)
                        handlerData = null
                        taskCounter = 0
                    }
                }
                taskCounter += 1
                handlerData?.postDelayed(this, 18000)
            }
        })
    }

    fun removeBeaconsLisener(){
        for(region in context.getDottysBeaconsList() ?: arrayListOf()) {
            beaconManager?.stopMonitoring(region.id)
            beaconManager?.stopRanging(BeaconRegion(region.id ?: "", UUID.fromString(region.uuid) ,region.major?.toInt(),region.minor?.toInt()))
        }
    }

    override fun onBeaconsDiscovered(beaconRegion: BeaconRegion?, beacons: MutableList<Beacon>?) {

       connectionToBeaconHandler(beacons, beaconRegion )
        if(context.getCurrentToken().isNullOrEmpty()){
            beaconManager?.stopNearableDiscovery()
            beaconManager?.stopLocationDiscovery()
            beaconManager?.disconnect()
            return
        }
         checkableTask()
        taskCounter = 0

    }



    private fun connectionToBeaconHandler(beacons: MutableList<Beacon>?, beaconRegion: BeaconRegion?){
        val beacon = beaconOnDatabase?.filter { it.minor == beaconRegion?.minor?.toLong() }?.first() ?: return
        beaconsConnected().forEach {
            if(it.isRegistered) {
                Log.d(
                    "REGISTERED BEACON",
                    "\n@@@@@@@@@@@@@@@@\n    MINOR ${it.minor}-${it.isRegistered}\n@@@@@@@@@@@@@@@@"
                )
            }
        }
        if(beacons?.size ?: 0 > 0)//&& beacons?.first{ it.minor == beacon.minor?.toInt() }?.rssi ?: -100 > if(beacon.beaconType == BeaconType.LOCATION){-80}else{-50}
        {
            taskCounter = 0
            if(beaconsConnected().filter { it.minor == beacon.minor && it.isConected == true }.isNullOrEmpty()){
                if(!beaconsConnected().filter{ it.minor == beacon.minor && it.isRegistered}.isNullOrEmpty()) {

                    val beaconRegenerate = beaconsConnected().first{ it.minor == beacon.minor && it.isRegistered}
                    beaconRegenerate.expiration = 0
                    beaconRegenerate.isConected = true
                    val beaconRenovation = beaconsConnected()
                        beaconRenovation.set(beaconsConnected().indexOf(beaconsConnected()?.first { it.minor == beaconRegenerate.minor} ?: 0),beaconRegenerate)
                    context.saveDataPreference(
                        PreferenceTypeKey.BEACON_AT_CONECTION,
                        DottysBeaconArray(beaconRenovation).toJson()
                    )
                    observer?.listOfBeacons = beaconRenovation
                } else {
                    /** SI NO CONTIENE EL BEACON*/
                    beacon.isConected = true
                    beacon.isRegistered = false
                    val beaconsInit = beaconsConnected()
                    beaconsInit?.add(beacon)
                    context.saveDataPreference(
                        PreferenceTypeKey.BEACON_AT_CONECTION,
                        DottysBeaconArray(beaconsInit).toJson()
                    )
                    beaconsConnected()?.let {
                        observer?.listOfBeacons = it
                        recordBeacon(beacon, BeaconEventType.ENTER)
                    }
                }


            } else {
                /** SI CONTIENE EL BEACON */
                beaconsConnected()?.let { beacons ->
                    val beaconRenovation = beaconsConnected()?.get(beacons.indexOf(beaconsConnected()?.first { it.minor == beacon.minor} ?: 0))
                    beaconRenovation?.expiration = 0
                    if(!beaconRenovation.isRegistered && beaconRenovation.isConected == true) {
                        recordBeacon(beaconRenovation, BeaconEventType.ENTER)
                        observer?.listOfBeacons = beaconsConnected()
                    }
                    if (beaconRenovation != null) {
                        beacons[beacons.indexOf(beacons.first { it.minor == beaconRenovation?.minor})] = beaconRenovation
                    }
                    context.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,DottysBeaconArray(beacons).toJson())
                }
            }
        }
        else
        {
            if(!beaconsConnected()?.filter { it.minor == beacon.minor && it.isConected == true}.isNullOrEmpty()){
                beaconsConnected()?.let { beacons ->
                    try {

                    val beaconRenovation = beaconsConnected()?.get(beacons.indexOf(beaconsConnected()?.first { it.minor == beacon.minor && it.isRegistered} ?: 0))
                    beaconRenovation?.expiration = beaconRenovation?.expiration?.plus(1) ?: 0
                    when (beaconRenovation?.expiration ?: 0) {
                       2 -> {
                           beaconRenovation.isConected = false
                       }
                        5 -> {
                            recordBeacon(beaconRenovation, BeaconEventType.EXIT)
                        }
                    }
                    if (beaconRenovation != null) {
                        beacons[beacons.indexOf(beacons.first { it.minor == beaconRenovation?.minor})] = beaconRenovation
                    }
                    context.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,DottysBeaconArray(beacons).toJson())
                    when (beaconRenovation?.expiration ?: 0) {
                        2 -> {
                            observer?.listOfBeacons = beaconsConnected()
                        }
                      else -> {
                          return
                      }
                    }

                    } catch (e:Exception){
                        Log.e("RENOVATION BEACON","${e.message}")
                    }
                }
            } else if (beaconsConnected().filter { it.isRegistered && it.expiration > 0}.isNotEmpty()){
                val expireRegiteredBeacon = beaconsConnected()

                try {
                    val expireRegitered =
                        (beaconsConnected().first { it.minor == beacon.minor && it.isRegistered && it.isConected != true})
                    if(beacons?.filter { it.minor  == expireRegitered.minor?.toInt()}?.isNotEmpty() == true){
                        expireRegitered.expiration = 0
                        expireRegiteredBeacon[beaconsConnected().indexOf(beaconsConnected().first { it.minor == beacon.minor })] =
                            expireRegitered
                        context.saveDataPreference(
                            PreferenceTypeKey.BEACON_AT_CONECTION,
                            DottysBeaconArray(expireRegiteredBeacon).toJson())
                        return
                    }
                    expireRegitered.expiration += 1
                    if(expireRegitered.expiration == 5 && expireRegitered.isRegistered){
                        recordBeacon(expireRegitered, BeaconEventType.EXIT)
                    }
                    expireRegiteredBeacon[beaconsConnected().indexOf(beaconsConnected().first { it.minor == beacon.minor })] =
                        expireRegitered
                    context.saveDataPreference(
                        PreferenceTypeKey.BEACON_AT_CONECTION,
                        DottysBeaconArray(expireRegiteredBeacon).toJson()
                    )
                } catch (e:Exception){
                    Log.e("EXPIRE REGISTERED","${e.message}")
                }
            }
        }

        beaconsConnected()?.forEach { if(it.expiration > 0) {
            Log.e("EXPIRED", "\n ** BEACON -${it.minor}- EXP ${it.expiration ?: ""}\n** ** **")
        } }
        Log.i("DISCOVERY", "MINOR -- ${beaconRegion?.minor ?: ""}")
        Log.i("DISCOVERY", "** DATA BECONS -${beacons?.size}- **\n${beaconRegion?.minor ?: ""}\n** ** **")

    }

    private fun recordBeacon(beaconRecorded: DottysBeacon, eventType: BeaconEventType){
        if(beaconsConnected().none { it.isRegistered && it.beaconType == BeaconType.LOCATION } &&
            beaconRecorded.beaconType ==  BeaconType.GAMING || beaconRecorded.isRegistered && eventType == BeaconEventType.ENTER) {
                    return
        }
        BeaconRest(context).recordBeaconEvent(DottysBeaconRequestModel(
            beaconRecorded.beaconIdentifier,
            beaconRecorded.uuid,
            beaconRecorded.major,
            beaconRecorded.minor,
            eventType.name),
            BeaconEventObserver(context as DottysBaseActivity)
        )
    }

    private fun connnectBeaconManager() {
        if (beaconManager ==  null) {
            beaconManager = BeaconManager(context)
        }
        beaconManager?.connect {
            for (beacon in  context.getDottysBeaconsList() ?: return@connect) {
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

    override fun onNearablesDiscovered(nearables: MutableList<Nearable>?) {
        if(!nearables.isNullOrEmpty())
        Log.i("NEARABLES", "REGION -${nearables?.size}- ${nearables?.first().beaconRegion ?: ""}")
    }

    override fun onScanStart() {
        Log.i("SCANNER","STARTED")
    }

    override fun onScanStop() {
        Log.i("SCANNER","STOPED")
    }

    override fun onEnteredRegion(beaconRegion: BeaconRegion?, beacons: MutableList<Beacon>?) {
        Log.e("ENTER","-- MINOR ** ${beaconRegion?.minor}")
    }

    override fun onExitedRegion(beaconRegion: BeaconRegion?) {
        Log.e("EXIT","-- MINOR ** ${beaconRegion?.minor}")
    }

}


interface BeaconHandlerDelegate {
    fun onBeaconViewChange(beaconsData: ArrayList<DottysBeacon>)
}

class BeaconHandlerObserver(listeners: BeaconHandlerDelegate) {
    var listOfBeacons: ArrayList<DottysBeacon> by Delegates.observable(
        initialValue = ArrayList<DottysBeacon>(),
        onChange = { _, _, new -> listeners.onBeaconViewChange(new) })
}