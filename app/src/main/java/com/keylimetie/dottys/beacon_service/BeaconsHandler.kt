package com.keylimetie.dottys.beacon_service

import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.estimote.coresdk.observation.region.beacon.BeaconRegion
import com.estimote.coresdk.recognition.packets.Beacon
import com.estimote.coresdk.recognition.packets.Nearable
import com.estimote.coresdk.service.BeaconManager
import com.estimote.mgmtsdk.connection.api.DeviceConnectionProvider
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.saveDataPreference
import com.keylimetie.dottys.ui.dashboard.models.BeaconType
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class BeaconsHandler(
    private val context: DottysBaseActivity,
    private val observer: BeaconHandlerObserver?
) :
    BeaconManager.BeaconRangingListener
    , BeaconManager.NearableListener
    ,BeaconManager.ScanStatusListener
//    ,BeaconManager.BeaconMonitoringListener
{
    var handlerData = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null
    var delay = 60000
    var handlerDataTask: Handler? = null
    var beaconManager: BeaconManager? = null

    //var beaconOnConnection = ArrayList<DottysBeacon>()
    var beaconOnDatabase =
        context.getDottysBeaconsList()//Preferences.getNearestBeaconsStored(context)?.beacons

    fun beaconsConnected(): ArrayList<DottysBeacon> {
        return when {
            context.getBeaconStatus()?.beaconArray.isNullOrEmpty() -> {
                beaconOnDatabase ?: ArrayList()
            }
            beaconOnDatabase?.first()?.location?.storeNumber != context.getBeaconStatus()?.beaconArray?.first()?.location?.storeNumber -> {
                  beaconOnDatabase!!
            }
            else -> {
                context.getBeaconStatus()?.beaconArray ?: ArrayList()
            }
        }
    }

    var taskCounter = 0
    var sheduleTimeTask: Long = 5000

    init {
        requestPermission()
    }

    private fun requestPermission() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()

        }
    }

     internal fun initBeaconManager() {
        if (context.getCurrentToken().isNullOrEmpty()) {
            return
        }
        Log.d("BEACON ACTIVITY", "Beacon Service Started")
        connnectBeaconManager()
        runnable = Runnable {
            removeTaskHandler()
            runneableAction()
            Log.d("RUNNEABLE", "🟡 $taskCounter")
            handlerData.postDelayed(runnable!!, 12000)
        }



    }


    /*private fun initProximyAction(){
    val cloudCredentials = EstimoteCloudCredentials(EstimoteCredentials.APP_ID , EstimoteCredentials.APP_TOKEN)
        val venueZone = ProximityZoneBuilder().
            .inFarRange()
            .onEnter {/* do something here */}
            .onExit {/* do something here */}
            .onContextChange {/* do something here */}
            .build()

    val proximityObserver = ProximityObserverBuilder(context, cloudCredentials)
        .withBalancedPowerMode()
        .onError { error -> Log.e("PROXIMITY 🔴 error","${error.message}") }
        .build()
    }*/
    private fun startDiscoveringLisener() {

        beaconManager?.setRangingListener(this)
    }

    fun removeTaskHandler(){
        try {
            handlerData.removeCallbacks(runnable!!, null)
            handlerData.removeCallbacksAndMessages(null)
        } catch (e:Exception) {Log.e("REMOVE HANDLER ERROR", "${e.message}2")}
    }

    private fun starMonitoringLisener() {
        beaconManager?.setMonitoringListener(object : BeaconManager.BeaconMonitoringListener {
            override fun onEnteredRegion(
                beaconRegion: BeaconRegion?,
                beacons: MutableList<Beacon>?
            ) {
                Log.e("BEACON EVENT", "***** ENTER -------> ${beaconRegion?.minor ?: 0}*")
                taskCounter = 0

                Handler().postDelayed({
                  //  connectionToBeaconHandler(beacons, beaconRegion)
                }, 15000)
            }

            override fun onExitedRegion(beaconRegion: BeaconRegion?) {
                connectionToBeaconHandler(null, beaconRegion)
                Log.e("BEACON EVENT", "******** EXIT ----> ${beaconRegion?.minor ?: 0}*")
            }
        })
    }

    private fun runneableAction() {
        Log.e("TASK", "CHECKEABLE $taskCounter 🌀 ${handlerData.looper.thread.stackTrace.size} ♦ ${handlerData.looper.thread.countStackFrames()}  ")

//        Log.e(
//            "TREAD ->",
//            "ID -> ${handlerData?.looper?.thread?.id} | STATE -> ${handlerData?.looper?.thread?.state} | ISALIVE: -> ${handlerData?.looper?.thread?.isAlive} | "
//        )

//        Log.d(
//            "CHECKEABLE",
//            "REGISTERED ${DottysBeaconArray(beaconsConnected().filter { it.isRegistered } as ArrayList<DottysBeacon>).toJson()} * $context * ")
        if (taskCounter > 4) {
            if (!beaconsConnected().filter { it.isRegistered && it.expiration > 1 }
                    .isNullOrEmpty()) {
                val expiredBeacons = beaconsConnected()
                expiredBeacons.forEach {
                //    if (it.expiration > 0) {
                        if (it.isRegistered || it.isConected == true) {
                            it.expiration += 1
                        } else {
                            it.expiration = 0
                        }
                        if (it.isRegistered && it.expiration ?: 0 >= 4) {
                            recordBeacon(it, BeaconEventType.EXIT)
                        }
                  //  }
                }
                context.saveDataPreference(
                    PreferenceTypeKey.BEACON_AT_CONECTION,
                    DottysBeaconArray(expiredBeacons).toJson()
                )
            } else if (taskCounter >= 5) {
                if (!beaconsConnected().filter { it.isRegistered }.isNullOrEmpty()) {
                    val expiredBeacons = beaconsConnected()
                    expiredBeacons.forEach {
                        if (it.isRegistered) {
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
                    removeTaskHandler()
                    taskCounter = 0
                }

            } else if (beaconsConnected().filter { it.isRegistered }.isNullOrEmpty()) {
                removeTaskHandler()
                taskCounter = 0
            }
        }
        if (taskCounter > 10) {
            taskCounter = 0
            removeTaskHandler()
        }
        taskCounter += 1
    }

    fun beaconTimerScanner() {
        taskCounter = 0
        removeTaskHandler()
        handlerData.post(runnable!!)
//        handlerData?.removeCallbacks {}
//        if(handlerData != null){return}
//        handlerData = Handler(Looper.getMainLooper())
//        handlerData?.removeCallbacks(Runnable { runneableAction() })
//        handlerData?.postDelayed(object : Runnable {
//            override fun run() {
//                runneableAction() // this method will contain your almost-finished HTTP calls
//                handlerData?.postDelayed(this, 18000)
//            }
//        }, 18000)
    }

    private fun removeBeaconsLisener() {
        for (region in context.getDottysBeaconsList() ?: arrayListOf()) {
            beaconManager?.stopMonitoring(region.id)
            beaconManager?.stopRanging(
                BeaconRegion(
                    region.id ?: "",
                    UUID.fromString(region.uuid),
                    region.major?.toInt(),
                    region.minor?.toInt()
                )
            )
        }
        beaconManager?.stopConfigurableDevicesDiscovery()
        beaconManager?.disconnect()
    }

    var restartManagerount = 0
    override fun onBeaconsDiscovered(beaconRegion: BeaconRegion?, beacons: MutableList<Beacon>?) {
        val beacon = beaconOnDatabase?.filter { it.minor == beaconRegion?.minor?.toLong() }?.first()
            ?: return
        beaconsConnected()?.forEach {
            if (it.expiration > 0) {
                Log.e("EXPIRED", "🌀 BEACON - ${it.minor} - 🧨 EXP ${it.expiration ?: ""}")
            }
        }
        //Log.i("DISCOVERY", "MINOR -- ${beaconRegion?.minor ?: ""}")
        val sata = if(beacons?.size ?: 0 > 0){"🟩 ${beacons?.size ?: 0}"}else{"🟥 ${ beacons?.size ?: 0}"} + " TASK COUNTER ♈️ $taskCounter"
        Log.i("DISCOVERY", "🔆 SCAN MINOR - ${beaconRegion?.minor ?: ""} | IN LIST: $sata")
        connectionToBeaconHandler(beacons, beaconRegion)

        if(restartManagerount > 15) {
            restartManagerount = 0
        }
        else
        {
            if(restartManagerount >= 10){
                restartManagerount = 0
                //removeBeaconsLisener()
                removeTaskHandler()
                Handler().postDelayed({
                    Log.d("BEACONS MANAGER",
                        "🆚 BEACON MANAGER 🔛 RESTART TASK COUNTER ♈️ $taskCounter"
                    )
                 //   connnectBeaconManager()
                }, 1000)
            }
            restartManagerount += 1
        }

        if (context.getCurrentToken().isNullOrEmpty()) {
            removeBeaconsLisener()
            return
        }

        taskCounter = 0

    }

    private fun connectionToBeaconHandler2(
        beacons: MutableList<Beacon>?,
        beaconRegion: BeaconRegion?
    ) {
        val beacon = beaconOnDatabase?.filter { it.minor == beaconRegion?.minor?.toLong() }?.first()
        beaconsConnected().forEach {if(it.isRegistered) {Log.d("REGISTERED BEACON", "✅ MINOR ${it.minor} - ${it.isRegistered}")}}
        if(beaconsConnected().isEmpty()) {return}
        val activeListAux = beaconsConnected()
        val activeAux = activeListAux.first { it.minor == beacon?.minor }
        var mssgTest = "MOCK"

        when {
            beacons?.size ?: 0 > 0
            && beacons?.first { it.minor == beacon?.minor?.toInt() }?.rssi ?: -100 > if (beacon?.beaconType == BeaconType.LOCATION) {-90} else {-70}
            -> {
                 /** BEACON ACTIVE */
                    if(activeAux.isConected != true) {
                        Handler().postDelayed({
                            observer?.listOfBeacons = beaconsConnected()
                            print("🔶🟩 $mssgTest")
                                              }, 2000)
                    }

                    activeAux.isConected = true
                    activeAux.expiration = 0
                if(!activeAux.isRegistered){
                    recordBeacon(activeAux,BeaconEventType.ENTER)
                }


            }
            else -> {
                /** BEACON EMPTY */
                if(activeAux.expiration in 2..3 && activeAux.isConected == true){
                    if(activeAux.isConected != true) {
                        Handler().postDelayed({
                            observer?.listOfBeacons = beaconsConnected()
                        }, 1000)
                    }
                }
                activeAux.isConected = activeAux.expiration <= 3 && activeAux.isConected == true
                activeAux.expiration = if(activeAux.isRegistered){activeAux.expiration.plus(1)}else{0}
            }
        }
        try {
            if(activeAux.expiration >= 5 ){
                activeAux.expiration = 0
                recordBeacon(activeAux, BeaconEventType.EXIT)
            }
            activeListAux[beaconsConnected().indexOf(beaconsConnected().first { it.minor == beacon?.minor })] =
                activeAux
            context.saveDataPreference(
                PreferenceTypeKey.BEACON_AT_CONECTION,
                DottysBeaconArray(activeListAux).toJson()
            )
        } catch (e:Exception) { Log.e("ERROR SAVE DATA" , "On beacon data update 🟥 ${e.message}")}
        mssgTest = "REAL"
    }

    private fun connectionToBeaconHandler(
        beacons: MutableList<Beacon>?,
        beaconRegion: BeaconRegion?
    ) {
        val beacon = beaconOnDatabase?.filter { it.minor == beaconRegion?.minor?.toLong() }?.first()
            ?: return
        beaconsConnected().forEach {
            if (it.isRegistered) {
                Log.d(
                    "REGISTERED BEACON", "✅ MINOR ${it.minor} - ${it.isRegistered}"
                )
            }
        }

        if (beacons?.size ?: 0 > 0 && beacons?.first { it.minor == beacon.minor?.toInt() }?.rssi ?: -100 > if (beacon.beaconType == BeaconType.LOCATION) {
                -90
            } else {
                -70
            }
        )//
        {
            if (beaconsConnected().isNullOrEmpty()){return}
            val beaconConnect = beaconsConnected().first { it.minor == beacon.minor }
            if (beaconConnect.isConected != true) {
                Handler().postDelayed({
                    observer?.listOfBeacons = beaconsConnected()
                }, 1000)
            }
            beaconConnect.isConected = true
            beaconConnect.expiration = 0
            val beaconAux = beaconsConnected()
            beaconAux[beaconsConnected().indexOf(beaconsConnected().first { it.minor == beaconConnect.minor })] =
                beaconConnect
            recordBeacon(beaconConnect, BeaconEventType.ENTER)

            context.saveDataPreference(
                PreferenceTypeKey.BEACON_AT_CONECTION,
                DottysBeaconArray(beaconAux).toJson()
            )

        } else {
            if (beaconsConnected().filter { it.minor == beacon.minor && it.isConected == true || it.isRegistered }
                    .isNotEmpty()) {
                if (beaconsConnected().first { it.minor == beacon.minor }.isConected != true && !beaconsConnected().first { it.minor == beacon.minor }.isRegistered) {
                    return
                }
                val beaconToExpire = beaconsConnected().first { it.minor == beacon.minor }
                if (beaconToExpire.isConected == true) {
                    Handler().postDelayed({
                        observer?.listOfBeacons = beaconsConnected()
                    }, 1000)
                }
                beaconToExpire.isConected = false
                beaconToExpire.expiration += 1
                val auxList = beaconsConnected()
                auxList[beaconsConnected().indexOf(beaconsConnected().first { it.minor == beaconToExpire.minor })] =
                    beaconToExpire
                context.saveDataPreference(
                    PreferenceTypeKey.BEACON_AT_CONECTION,
                    DottysBeaconArray(auxList).toJson()
                )

                if (beaconToExpire.expiration > 5) {
                    recordBeacon(beaconToExpire, BeaconEventType.EXIT)
                }
            }

        }


if(beaconsConnected().filter { it.isRegistered }.size < 2) {
    beaconTimerScanner()
}
    }

    private fun recordBeacon(beaconRecorded: DottysBeacon, eventType: BeaconEventType) {
        if (beaconRecorded.isRegistered && eventType == BeaconEventType.ENTER ||
            !beaconRecorded.isRegistered && eventType == BeaconEventType.EXIT
        ) {
            return
        }
        if (beaconsConnected().none { it.isRegistered && it.beaconType == BeaconType.LOCATION } &&
            beaconRecorded.beaconType == BeaconType.GAMING || beaconRecorded.isRegistered && eventType == BeaconEventType.ENTER) {
            return
        }
        BeaconRest(context).recordBeaconEvent(
            DottysBeaconRequestModel(
                beaconRecorded.beaconIdentifier,
                beaconRecorded.uuid,
                beaconRecorded.major,
                beaconRecorded.minor,
                eventType.name
            ),
            BeaconEventObserver(context as DottysBaseActivity)
        )
    }

    private fun connnectBeaconManager() {

        val connectionProvider = DeviceConnectionProvider(context)
        connectionProvider.connectToService {
            Log.d("SERVICE CONNECTED", "👁‍🗨")

        }


        /*
        */
        if (beaconManager == null) {
            beaconManager = BeaconManager(context)
            beaconManager?.setForegroundScanPeriod(1000, 8000)
            beaconManager?.setBackgroundScanPeriod(1000, 8000)
        }
        beaconManager?.connect(object : BeaconManager.ServiceReadyCallback {
            override fun onServiceReady() {
                for (beacon in context.getDottysBeaconsList() ?: return) {
                    //          if(beacon.isConected != true) {
                    val regionOfBeaon = BeaconRegion(
                        beacon.id,
                        UUID.fromString(beacon.uuid),
                        beacon.major?.toInt(),
                        beacon.minor?.toInt()
                    )
                    beaconManager?.startMonitoring(regionOfBeaon)
                    beaconManager?.startRanging(regionOfBeaon)
                    // }
                }
                startDiscoveringLisener()
                starMonitoringLisener()
            }

        })
            /*
            */


    }

    override fun onNearablesDiscovered(nearables: MutableList<Nearable>?) {
        if (!nearables.isNullOrEmpty())
            Log.i(
                "NEARABLES",
                "REGION -${nearables?.size}- ${nearables?.first().beaconRegion ?: ""}"
            )
    }

    override fun onScanStart() {
        Log.i("SCANNER", "STARTED")
    }

    override fun onScanStop() {
        Log.i("SCANNER", "STOPED")
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