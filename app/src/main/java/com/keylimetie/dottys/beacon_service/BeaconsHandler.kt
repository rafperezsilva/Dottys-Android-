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
import com.keylimetie.dottys.ui.dashboard.models.BeaconType
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class BeaconsHandler(private val context: DottysBaseActivity, private val observer: BeaconHandlerObserver?): BeaconManager.BeaconRangingListener, BeaconManager.NearableListener , BeaconManager.ScanStatusListener, BeaconManager.BeaconMonitoringListener {
    var handlerData: Handler? = null
    var handlerDataTask: Handler? = null
    var beaconManager: BeaconManager? = null
    var beaconOnConnection = ArrayList<DottysBeacon>()
    var beaconOnDatabase = context.getDottysBeaconsList()//Preferences.getNearestBeaconsStored(context)?.beacons
    var beaconOnDatabaseConnected = context.getBeaconStatus()
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
                    if(beaconOnConnection.filter { it.isConected == true }.isNotEmpty()){
                        checkableTask()
                    }
                }
                Log.e("RUNNEABLE", "RUNNEABLE CHECKER -- ")
                handlerDataTask?.postDelayed(this, 1000*60*1)
            }
       })
    }

    internal fun initBeaconManager() {
        if(context.getCurrentToken().isNullOrEmpty()){return}
        Log.d("BEACON ACTIVITY", "Beacon Service Started")
        connnectBeaconManager()
        beaconManager?.setForegroundScanPeriod(3000, 5000)
        beaconManager?.setBackgroundScanPeriod(3000, 5000)
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
                taskCounter += 1
                Log.e("TASK", "CHECKEABLE $taskCounter * $context * ")
                if(taskCounter > 5){
                   // taskCounter =  0
                    if(beaconOnConnection.size > 0){
                        beaconOnConnection.forEach { it.expiration += 1 }
                        beaconOnConnection.forEach {
                            Log.e("EXPIRE BECON", "${it.expiration} <- MINOR: ${it.minor}")
                        }
                        val beaconExpireAux = ArrayList<DottysBeacon>()
                        for(beaconExpired in beaconOnConnection){
                            if(beaconExpired.expiration < 5 ){
                                if (beaconExpired.expiration == 3) {
                                    beaconExpired.isConected = false
                                }
                                beaconExpireAux.add(beaconExpired)
                            } else {
                                if(beaconExpired.isRegistered) {
                                    recordBeacon(beaconExpired, BeaconEventType.EXIT)
                                }
                            }
                        }
                        beaconOnConnection = beaconExpireAux
                        observer?.listOfBeacons = beaconOnConnection
                    } else {
                        handlerData?.removeCallbacks(this)
                        handlerData = null
                    }
                }
                handlerData?.postDelayed(this, 10000)
            }
        })
    }

    override fun onBeaconsDiscovered(beaconRegion: BeaconRegion?, beacons: MutableList<Beacon>?) {
       connectionToBeaconHandler(beacons, beaconRegion )
        taskCounter = 0
        checkableTask()
     }

    private fun connectionToBeaconHandler(beacons: MutableList<Beacon>?, beaconRegion: BeaconRegion?){
        val beacon = beaconOnDatabase?.filter { it.minor == beaconRegion?.minor?.toLong() }?.first() ?: return
        beaconOnConnection?.forEach {
            Log.d("REGISTERED BEACON", "\n@@@@@@@@@@@@@@@@\nMINOR ${it.minor}-${it.isRegistered}\n@@@@@@@@@@@@@@@@")
        }
        if(beacons?.size ?: 0 > 0){
            /** SI NO CONTIENE NINGUN BEACON*/
            if(beaconOnConnection.isEmpty()){
                beacon.isConected = true
                beacon.isRegistered = false
                beaconOnConnection.add(beacon) //UPDAET VIEW
                recordBeacon(beacon, BeaconEventType.ENTER)
                observer?.listOfBeacons = beaconOnConnection
            } else {
                /** SI LO CONTINE  RESTURA EXPIRATION */
                if(beaconOnConnection.filter { it.minor == beacon.minor }.isNotEmpty()){
                    val beaconRestore = beaconOnConnection.first{ it.minor == beacon.minor }
                    beaconRestore.expiration = 0
                    beaconRestore.isConected = true
                    beaconOnConnection[beaconOnConnection.indexOf(beaconRestore)] = beaconRestore
                    /** REVISAR SI HAY BEACON ENCENDIDOS DE GAMMING PREVIAMENTE **/
                    if(!beaconRestore.isRegistered &&
                        beaconRestore.beaconType == BeaconType.GAMING &&
                        beaconOnConnection.filter { it.beaconType == BeaconType.LOCATION }.isNotEmpty() &&
                        beaconRestore.isConected == true){
                        beaconRestore.isRegistered = true
                        beaconRestore.expiration = 0
                        beaconOnConnection[beaconOnConnection.indexOf(beaconRestore)] = beaconRestore
                        recordBeacon(beacon, BeaconEventType.ENTER)
                    }
                 } else {
                    /** SE AGREGA BEACON A LISTA NO VACIA*/
                    beacon.isConected = true
                    //beacon.isRegistered = true
                    beaconOnConnection.add(beacon) //UPDAET VIEW
                    recordBeacon(beacon, BeaconEventType.ENTER)
                    observer?.listOfBeacons = beaconOnConnection
                }
            }
        }
        else
        {
            if(beaconOnConnection.filter { it.minor?.toInt() == beaconRegion?.minor }.isNotEmpty()){
              val beaconExpired = beaconOnConnection.first { it.minor?.toInt() == beaconRegion?.minor }
                beaconExpired.expiration += 1
                beaconOnConnection[beaconOnConnection.indexOf(beaconExpired)] = beaconExpired
                if(beaconExpired.expiration > 5) {
                    Log.e(
                        "EXPIRED BEACON",
                        "\n********************\n********* -${beaconExpired.minor}- EXP-> ${beaconExpired.expiration} *********\n*************"
                    )
                    beaconExpired.isConected = false
                    beaconOnConnection[beaconOnConnection.indexOf(beaconExpired)] = beaconExpired
                    if(beaconExpired.expiration == 6) {
                        observer?.listOfBeacons = beaconOnConnection
                    }
                    if(beaconExpired.expiration > 10) {
                        //REMOVE BEACON EXPIRED
                        recordBeacon(beaconExpired, BeaconEventType.EXIT)
                        beaconOnConnection.removeAt(beaconOnConnection.indexOf(beaconExpired))
                        if(beaconExpired.expiration == 11) {
                            observer?.listOfBeacons = beaconOnConnection
                        }
                    }
                //UPDATE VIEW
                }
            }
        }



        /********************************
        if(beacons?.size ?: 0 > 0){
            if (beaconOnConnection.filter { it.minor == beacon.minor }.isNotEmpty()) {
                val  beaconRestore =  beaconOnConnection.first { it.minor == beacon.minor }
                beaconRestore.expiration = 0
                beaconRestore.isConected = true
                if(!beaconOnConnection.first{ it.minor == beacon.minor }.isRegistered &&
                    beaconOnConnection.filter { it.beaconType == BeaconType.LOCATION }.isNotEmpty() &&
                    beacon.beaconType == BeaconType.GAMING){
                    beaconRestore.isRegistered = true
                    recordBeacon(beacon, BeaconEventType.ENTER)
                }

                beaconOnConnection[beaconOnConnection.indexOf(beaconRestore)] = beaconRestore
                val beaconsToSave = DottysBeaconArray()
                beaconsToSave.beaconArray = beaconOnConnection
                   context.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,beaconsToSave.toJson())
            }
            else {
                beacon.isConected = true
                beaconOnConnection.add(beacon)
                observer?.listOfBeacons = beaconOnConnection
                recordBeacon(beacon, BeaconEventType.ENTER)
            }
        }
        else {
            if (beaconOnConnection.filter{ it.minor == beacon.minor }.isNotEmpty()) {
                beacon.expiration += 1
                beaconOnConnection[beaconOnConnection.indexOf(beaconOnConnection.first { it.minor == beacon.minor })] = beacon
                if(beacon.expiration > 3){
                    beacon.isConected = false
                    beaconOnConnection[beaconOnConnection.indexOf(beaconOnConnection.first { it.minor == beacon.minor })] = beacon
                    //beaconOnConnection.removeAt(beaconOnConnection.indexOf(beaconOnConnection.first { it.minor == beacon.minor }))
                    if (beaconOnConnection.first { it.minor == beacon.minor }.isRegistered && beacon.expiration > 8) {
                        recordBeacon(beacon, BeaconEventType.EXIT)
                        beaconOnConnection.removeAt(beaconOnConnection.indexOf(beaconOnConnection.first { it.minor == beacon.minor }))
                    }
                    if(beacon.expiration == 4) {
                        observer?.listOfBeacons = beaconOnConnection
                    }
                }
            }
//            else {
//                beacon.isConected = true
//                beaconOnConnection.add(beacon)
//                recordBeacon(beacon, BeaconEventType.ENTER)
//                observer?.listOfBeacons = beaconOnConnection
//            }
        }
        ********************************/


        beaconOnConnection.forEach { if(it.expiration > 0) {
            Log.e("EXPIRED", "\n ** BEACON -${it.minor}- EXP ${it.expiration ?: ""}\n** ** **")
        } }
        Log.i("DISCOVERY", "MINOR -- ${beaconRegion?.minor ?: ""}")
        Log.i("DISCOVERY", "** DATA BECONS -${beacons?.size}- **\n${beaconRegion?.minor ?: ""}\n** ** **")

    }

    private fun recordBeacon(beaconRecorded: DottysBeacon, eventType: BeaconEventType){
        if(beaconOnConnection.none { it.beaconType == BeaconType.LOCATION } &&
            beaconRecorded.beaconType == BeaconType.GAMING &&
            eventType == BeaconEventType.ENTER){
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