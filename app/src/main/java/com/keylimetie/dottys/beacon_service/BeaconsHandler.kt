package com.keylimetie.dottys.beacon_service


import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.util.Log
import androidx.lifecycle.Observer
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.saveDataPreference
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import com.keylimetie.dottys.utils.DottysBeaconReferenceApplication
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.MonitorNotifier
import kotlin.properties.Delegates


class BeaconsHandler(
    private val context: DottysBaseActivity,
    private val observer: BeaconHandlerObserver?
){
    lateinit var dottysBeaconReferenceApplication: DottysBeaconReferenceApplication
    var handlerData = Handler()
    var runnable: Runnable? = null
    
    var handlerDataTask: Handler? = null
    var beaconManager: BeaconManager? = null
    var taskCounter = 0
    //var beaconOnConnection = ArrayList<DottysBeacon>()
    var beaconOnDatabase =
        context.getDottysBeaconsList()//Preferences.getNearestBeaconsStored(context)?.beacons

    fun beaconsConnected(): ArrayList<DottysBeacon> {
        return context.getBeaconStatus()?.beaconArray ?: beaconOnDatabase ?: ArrayList()
    }

    // This gets called from the BeaconReferenceApplication when monitoring events change changes
    val monitoringObserver = Observer<Int> { state ->
       var stateString = "inside"
        if (state == MonitorNotifier.OUTSIDE) {

            stateString == "outside"
               }
        else {
        }
        Log.d("TAG", "♦️🟩🟦🟦🟪 monitoring state changed to : $stateString")


    }

    val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d("TAG", "Ranged: ${beacons.count()} beacons")
        if (beacons.size > 0) {
         Log.e("🎩 COÑO", "${beacons.size} $context")
            try {
                //beacons.forEach { Log.d("POWER ", " 👓 MINOR ${it.id3} 🎯 TX: ${it.rssi}") }
            beaconHandler(beacons)
               // beacons.forEach { Log.d("BEACON DETECTED","AT MINOR ${it.id3}") }
            } catch (e: Exception){
                Log.d("BEACON DETECTED","ERROR: ${e.message}")
            }
        }
    }

    private fun beaconHandler(beacons:Collection<Beacon>){
        val beaconAux = beaconsConnected()
        for(item in beaconsConnected()){
            beacons.forEach { Log.d("INFO BEACON", "👺 MINOR ${it.id3} | RSSI AVG -${it.runningAverageRssi} 🟨 RRSI-${it.rssi}") }
            if(beacons.any { it.id3.toInt() == item.minor?.toInt()}) {
                val beaconActive = beaconsConnected().first { it.minor?.toInt() == beacons.first { it2 -> it2.id3.toInt() == item.minor?.toInt() }.id3.toInt()}
                if (beaconActive.isConected != true) {
                    Handler().postDelayed({
                        observer?.listOfBeacons = beaconsConnected()

                    }, 2000)
                }
                Log.d(
                    "BEACON MANAGER",
                    "${item.expiration} ✋ TO RENOVATE  ${item.minor}  "
                )
                beaconActive.isConected = true
                beaconActive.expiration = 0
                beaconAux[beaconsConnected().indexOf(beaconsConnected().first { it.minor == beaconActive.minor })] =
                    beaconActive
                recordBeacon(beaconActive,BeaconEventType.ENTER)
            } else {

                if(item.isRegistered || item.isConected == true) {

                    item.isConected = item.expiration <= 2
                    item.expiration += 1
                    if (item.isConected == true && item.expiration == 3 )

                       {
                        Handler().postDelayed({
                            observer?.listOfBeacons = beaconsConnected()

                        }, 2000)
                    }
                    Log.d(
                        "BEACON MANAGER",
                        "${item.expiration} ✊ TO EXPIRE  ${item.minor}  "
                    )
                    beaconAux[beaconsConnected().indexOf(beaconsConnected().first { it.minor == item.minor })] =
                        item
                   if( item.expiration > 5){recordBeacon(item,BeaconEventType.EXIT)}
                }
            }
            context.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,
                DottysBeaconArray(beaconAux).toJson())
        }

        beaconTimerScanner()
    }

    fun initBeacon() {
        requestPermission()
        if(beaconManager != null) {return}
        beaconManager = BeaconManager.getInstanceForApplication(context)

        runnable = Runnable { runnableNewAction() }

        beaconManager?.foregroundBetweenScanPeriod = 1500
        beaconManager?.backgroundBetweenScanPeriod = 1500
        beaconManager?.backgroundScanPeriod = 8000
        beaconManager?.foregroundScanPeriod = 8000
         dottysBeaconReferenceApplication = (context.application) as DottysBeaconReferenceApplication
        dottysBeaconReferenceApplication.monitoringData.state.observe(context, monitoringObserver)

        // beaconReferenceApplication.monitoringData.state.observe(context, monitoringObserver)
        dottysBeaconReferenceApplication.rangingData.beacons.observe(context, rangingObserver)
}
    /*fun initBeacon(){
        val rangingObserver = Observer<Collection<Beacon>> { beacons ->
            Log.d("TAG", "Ranged: ${beacons.count()} beacons")
            if (BeaconManager(this).rangedRegions.size > 0) {
             /*   beaconCountTextView.text = "Ranging enabled: ${beacons.count()} beacon(s) detected"
                beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                    beacons
                        .sortedBy { it.distance }
                        .map { "${it.id1}\nid2: ${it.id2} id3:  rssi: ${it.rssi}\nest. distance: ${it.distance} m" }.toTypedArray())
           */
            }
        }
    */

    var breakCounter = 0
    private fun connectionToBeaconHandler2(beacons:Collection<Beacon>){
        breakCounter += 1
        taskCounter = 0
//        if(breakCounter >= 5) {breakCounter = 0}
//        if(breakCounter > 1) {
//            return
//        }
            for (beaconEach in beacons){

                beaconsConnected().forEach {
                    if (it?.minor?.toInt() == beaconEach.id3.toInt() && beaconEach.distance < 1.0) {
                        Log.d("ESTUPIDES", "👊🏿 $beaconEach")
                        connectionToBeaconHandler(beacons as ArrayList<Beacon>, it?.minor?.toInt())
                    } else {
                        connectionToBeaconHandler(null, it?.minor?.toInt())
                    }

            }

         Log.d("ESTUPIDES","✋ $beaconEach")

        }

//
//        breakCounter += 1
//        if(beacons.isEmpty()){return}
//        if(breakCounter >= 20) {breakCounter = 0}
//        if(breakCounter > 1) {
//            return
//        }
//      for (beaconEach in 0 until beaconsConnected().size){
//
//
//            print("⛔️ $beaconEach")
//               connectionToBeaconHandler(
//                   beacons as MutableList,
//                   beaconsConnected()[beaconEach].minor?.toInt()
//               )
//
//        }

    }
 var isUpdatingBeacon = false
    private fun connectionToBeaconHandler(
        beacons: ArrayList<Beacon>?,
        beaconRegion: Int?
    ){
        if(isUpdatingBeacon){return}
        val beacon = beaconOnDatabase?.filter { it.minor?.toInt() == beaconRegion}?.first() ?: return
        beaconsConnected().forEach {
            if (it.isRegistered) {
                Log.d(
                    "REGISTERED BEACON",
                    "BEACON 🔵 AT MINOR ${it.minor} - REGITERED ->${it.isRegistered}"
                )
            }
        }
        //  try {

        // } catch (e: Exception){}
        if (beacons?.size ?: 0 > 0)// &&  beacons?.first { it.minor == beacon.minor?.toInt() }?.rssi ?: -100 > if(beacon.beaconType == BeaconType.LOCATION){-90}else{-60})//
        {
            val rssi = beacons?.first { it.id3.toInt() == beacon.minor?.toInt() }?.rssi ?: -100
            Log.i("LOGGER", "MINOR 🏧 ${beacon.minor?.toInt()} | RSSI 📶 ${rssi}")
            Log.i(
                "LOGGER", "RSSI 🔜 " + if (rssi >= -60) {
                    "✅🈯️🌀🈯️💹"
                } else {
                    "🛑⛔️⛔️🔆⛔️⛔️🛑"
                }
            )
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

            /***if(beaconsConnected().filter { it.minor == beacon.minor && it.isConected != true}.isEmpty()){
            val beaconConnect =  beaconsConnected().first { it.minor == beacon.minor && it.isConected != true}
            beaconConnect.expiration = 0
            beaconConnect.isConected = true
            beaconsConnected()[beaconsConnected().indexOf(beaconsConnected().first().minor == beaconConnect.minor)] = beaconConnect
            observer?.listOfBeacons = beaconsConnected()
            recordBeacon(beacon, BeaconEventType.ENTER)
            } else {
            //RENEW BEACON
            val beaconConnected =  beaconsConnected().first { it.minor == beacon.minor }
            beaconConnected.isConected = true
            beaconConnected.expiration = 0
            beaconsConnected()[beaconsConnected().indexOf(beaconsConnected().first().minor == beaconConnected.minor)] = beaconConnected
            }*/
            context.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,
                DottysBeaconArray(beaconAux).toJson())

        }
        else
        {
            if(beaconsConnected().filter { it.minor == beacon.minor && it.isConected == true || it.isRegistered }.isNotEmpty()){
                if(beaconsConnected().first{ it.minor == beacon.minor}.isConected != true && !beaconsConnected().first{ it.minor == beacon.minor}.isRegistered){return}
                val beaconToExpire = beaconsConnected().first{ it.minor == beacon.minor}
                if(beaconToExpire.isConected == true && beaconToExpire.expiration > 3){
                    Handler().postDelayed({
                        observer?.listOfBeacons = beaconsConnected()
                    }, 1000)
                }
                beaconToExpire.isConected = beaconToExpire.expiration > 3
                beaconToExpire.expiration += 1
                val auxList = beaconsConnected()
                auxList[beaconsConnected().indexOf(beaconsConnected().first{it.minor == beaconToExpire.minor})] = beaconToExpire
                context.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,DottysBeaconArray(auxList).toJson())

                if (beaconToExpire.expiration > 7){
                    recordBeacon(beaconToExpire,BeaconEventType.EXIT)
                }
            }
//            if(beaconsConnected().filter { it.minor == beacon.minor }.isNotEmpty()){
//                val beaconExpired =  beaconsConnected().first { it.minor == beacon.minor }
//                beaconExpired.expiration = beaconExpired.expiration.plus(1)
//                beaconExpired.isConected = false
//                if (beaconExpired.expiration > 5){
//                    recordBeacon(beaconExpired,BeaconEventType.EXIT)
//                }
//            }
        }

        beaconsConnected()?.forEach { if(it.expiration > 0) {
            Log.e("EXPIRED", "BEACON -${it.minor}- 🧨 EXP ${it.expiration}")
        } }
        Log.i("DISCOVERY", "MINOR -- ${beaconRegion ?: ""}")
        Log.i(
            "DISCOVERY",
            "DATA 🚩 IN ARRAY -${beacons?.size}- AT MINOR 🟩 ${beaconRegion ?: ""}"
        )
        beaconTimerScanner()
    }
    private fun recordBeacon(beaconRecorded: DottysBeacon, eventType: BeaconEventType) {
        if (beaconRecorded.isRegistered && eventType == BeaconEventType.ENTER ||
            !beaconRecorded.isRegistered && eventType == BeaconEventType.EXIT ||
            isUpdatingBeacon
        ) {
            return
        }
//        if(beaconsConnected().none { it.isRegistered && it.beaconType == BeaconType.LOCATION } &&
//            beaconRecorded.beaconType ==  BeaconType.GAMING || beaconRecorded.isRegistered && eventType == BeaconEventType.ENTER) {
//                    return
//        }

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

    fun beaconTimerScanner() {
        taskCounter = 0
        handlerData.removeCallbacksAndMessages(null)
        handlerData.removeCallbacks(runnable!!)
        handlerData.postDelayed(Runnable {
            handlerData.postDelayed(runnable!!, context.delayBeaconChecker.toLong())
            runnableNewAction()
        }.also { runnable = it }, context.delayBeaconChecker.toLong())
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

    private fun runnableNewAction(){
            Log.e("RUNNEABLE🕒", "${context.delayBeaconChecker}}")
       // initBeacon()
    }

    private fun runneableAction(){
        taskCounter += 1
        Log.e("TASK", "CHECKEABLE $taskCounter * ${handlerData.looper.queue} * ")
        Log.e("TREAD ->", "ID -> ${handlerData?.looper?.thread?.id} | STATE -> ${handlerData?.looper?.thread?.state} | ISALIVE: -> ${handlerData?.looper?.thread?.isAlive} | ")

        Log.d(
            "CHECKEABLE",
            "REGISTERED ${DottysBeaconArray(beaconsConnected().filter { it.isRegistered } as ArrayList<DottysBeacon>).toJson()} * $context * ")
        if(taskCounter > 3) {
            if (!beaconsConnected().filter { it.isRegistered && it.expiration > 1 }
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
                    handlerData?.removeCallbacksAndMessages(null)
                    // handlerData = null
                    taskCounter = 0
                }

            } else if (beaconsConnected().filter { it.isRegistered }.isNullOrEmpty()) {
                handlerData?.removeCallbacksAndMessages(null)
                //handlerData = null
                taskCounter = 0
            }
        }
        if(taskCounter > 10) {
            taskCounter = 0
            handlerData.removeCallbacksAndMessages(null)
        }
    }
    private fun requestPermission() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()

        }
    }
}
/**    BeaconManager.BeaconRangingListener, BeaconManager.NearableListener,
    BeaconManager.ScanStatusListener, BeaconManager.BeaconMonitoringListener {
    var handlerData = Handler()
    var runnable: Runnable? = null
    var delay = 15000
    var handlerDataTask: Handler? = null
    var beaconManager: BeaconManager? = null

    //var beaconOnConnection = ArrayList<DottysBeacon>()
    var beaconOnDatabase =
        context.getDottysBeaconsList()//Preferences.getNearestBeaconsStored(context)?.beacons

    fun beaconsConnected(): ArrayList<DottysBeacon> {
        return context.getBeaconStatus()?.beaconArray ?: beaconOnDatabase ?: ArrayList()
    }

    var taskCounter = 0
    var sheduleTimeTask: Long = 5000
//    //private var bulkUpdater: BulkUpdater? = null

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
    if (context.getCurrentToken().isNullOrEmpty()) {
        return
    }
    Log.d("BEACON ACTIVITY", "Beacon Service Started")
    connnectBeaconManager()
    runnable = Runnable { runneableAction() }
    beaconManager?.setForegroundScanPeriod(1500, 8000)
    beaconManager?.setBackgroundScanPeriod(1500, 8000)
    startDiscoveringLisener()
    starMonitoringLisener()
    /*beaconManager?.setRangingListener(BeaconManager.BeaconRangingListener { region, list ->

        if (list.size > 0) {
            Log.i("RANGING", "MINOR ** ${list.first().minor} ")
        }
    })*/
    /**
    val cloudCredentials = EstimoteCloudCredentials(EstimoteCredentials.APP_ID, EstimoteCredentials.APP_TOKEN)
    proximityObserver = ProximityObserverBuilder(context, cloudCredentials)
    .onError { throwable: Throwable ->
    Log.e("app", "proximity observer error: $throwable")

    }
    .withBalancedPowerMode()
    .build()

    val zone =    ProximityZoneBuilder().forTag("GAMING")
    .inNearRange().onContextChange { context ->

    Log.d("🏖app", "XXXXXXXXX to $context.size's desk")
    }
    .onEnter { context: ProximityZoneContext ->
    val deskOwner = context.tag//.attachments["desk-owner"]
    // Log.e("PROXIMITY", "✋ ${beaconOnDatabase?.first { it.id == context.deviceId }?.minor}")
    Log.d("🏖app", "Welcome to $deskOwner's desk")

    }
    .onExit { context: ProximityZoneContext? ->
    // Log.e("PROXIMITY", "🥾 ${beaconOnDatabase?.first { it.id == context?.deviceId }?.minor}")
    Log.d("🏖app", "Bye bye, come again!")

    }
    .build()

    proximityObserver?.startObserving(zone)


     */
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
                Log.e("BEACON EVENT", "***** ENTER -------> ${beaconRegion?.minor ?: 0}*")
                /**    if(beacons?.size ?: 0 > 0) {
                beacons?.forEach { beaconDetected ->
                val beaconsTemp = if(beaconsConnected().isNullOrEmpty()){beaconOnDatabase}else{beaconsConnected()}  ?:  ArrayList<DottysBeacon>()
                val connected = beaconsTemp.first{ it.minor?.toInt() == beaconDetected.minor}
                if(connected.isConected == true) {return}
                if(connected.isConected != true){
                connected.isConected = true
                beaconsTemp[beaconsTemp.indexOf(beaconsTemp.first { it.minor == connected.minor})] = connected
                context.saveDataPreference(
                PreferenceTypeKey.BEACON_AT_CONECTION,
                DottysBeaconArray(beaconsTemp).toJson())
                if (!connected.isRegistered){
                recordBeacon(connected, BeaconEventType.ENTER)
                }
                observer?.listOfBeacons = beaconsConnected()
                }
                }

                }*/
            }

            override fun onExitedRegion(beaconRegion: BeaconRegion?) {
                Log.e("BEACON EVENT", "******** EXIT ----> ${beaconRegion?.minor ?: 0}*")
                /**    if(!beaconsConnected().isNullOrEmpty() && beaconsConnected().filter { it.minor?.toInt() == beaconRegion?.minor }.isNotEmpty()){
                val beaconDisconect = beaconsConnected().first { it.minor?.toInt() == beaconRegion?.minor }
                if(beaconDisconect.isConected != true) {return}
                beaconDisconect.isConected = false
                beaconsConnected()[beaconsConnected().indexOf(beaconsConnected().first { it.minor == beaconDisconect.minor})] = beaconDisconect
                context.saveDataPreference(
                PreferenceTypeKey.BEACON_AT_CONECTION,
                DottysBeaconArray(beaconsConnected()).toJson())
                if (beaconDisconect.isRegistered){
                recordBeacon(beaconDisconect, BeaconEventType.ENTER)
                }
                observer?.listOfBeacons = beaconsConnected()

                } */
            }
        })
    }
    //var countestTask = 0
    /**
    private fun checkableTask(){
        taskCounter = 0
        val manejador = Handler(Looper.getMainLooper())
        val runAction = Runnable {
            Log.e("TASK", "CHECKEABLE $taskCounter * ${manejador.javaClass} * ")
            Log.d(
                "CHECKEABLE",
                "REGISTERED ${DottysBeaconArray(beaconsConnected().filter { it.isRegistered } as ArrayList<DottysBeacon>).toJson()} * $context * ")
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
                        //handlerData?.removeCallbacks(this)
                       // handlerData = null
                        taskCounter = 0
                    }

                } else if (beaconsConnected().filter { it.isRegistered }.isNullOrEmpty()) {
                    //handlerData?.removeCallbacks(this)
                    //handlerData = null
                    taskCounter = 0
                }
            }

        }
        manejador.removeCallbacks(runAction)
       // manejador.post {
          //  runAction

            taskCounter = if(beaconsConnected().filter { it.expiration > 0}.isNullOrEmpty()){ taskCounter.plus(
                1
            )}else {0}
            Log.e("XXXX", "TASK COUNTER ------>> $taskCounter")
            manejador.postDelayed(runAction, 18000)
            if(beaconsConnected().filter { it.isRegistered }.isNullOrEmpty()){
                manejador.removeCallbacks(runAction)
                taskCounter = 0
            }
       // }
    }
    */

    private fun runneableAction(){
        taskCounter += 1
            Log.e("TASK", "CHECKEABLE $taskCounter * ${handlerData.looper.queue} * ")
            Log.e("TREAD ->", "ID -> ${handlerData?.looper?.thread?.id} | STATE -> ${handlerData?.looper?.thread?.state} | ISALIVE: -> ${handlerData?.looper?.thread?.isAlive} | ")

        Log.d(
                "CHECKEABLE",
                "REGISTERED ${DottysBeaconArray(beaconsConnected().filter { it.isRegistered } as ArrayList<DottysBeacon>).toJson()} * $context * ")
            if(taskCounter > 3) {
                if (!beaconsConnected().filter { it.isRegistered && it.expiration > 1 }
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
                        handlerData?.removeCallbacksAndMessages(null)
                        // handlerData = null
                        taskCounter = 0
                    }

                } else if (beaconsConnected().filter { it.isRegistered }.isNullOrEmpty()) {
                    handlerData?.removeCallbacksAndMessages(null)
                    //handlerData = null
                    taskCounter = 0
                }
            }
        if(taskCounter > 10) {
            taskCounter = 0
            handlerData.removeCallbacksAndMessages(null)
        }
            }

    fun beaconTimerScanner() {
        taskCounter = 0
        handlerData.removeCallbacksAndMessages(null)
        handlerData.removeCallbacks(runnable!!)
        handlerData.postDelayed(Runnable {
            handlerData.postDelayed(runnable!!, delay.toLong())
            runneableAction()
         Log.e ("HANLDER DATA","This method will run every 10 seconds")
        }.also { runnable = it }, delay.toLong())
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
/**
    private fun checkableTask3(){
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
*/
    fun removeBeaconsLisener(){
        for(region in context.getDottysBeaconsList() ?: arrayListOf()) {
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
    }

    override fun onBeaconsDiscovered(beaconRegion: BeaconRegion?, beacons: MutableList<Beacon>?) {

       connectionToBeaconHandler(beacons, beaconRegion)
        if(context.getCurrentToken().isNullOrEmpty()){
            beaconManager?.stopNearableDiscovery()
            beaconManager?.stopLocationDiscovery()
            beaconManager?.disconnect()
            return
        }

          taskCounter = 0

    }


    private fun connectionToBeaconHandler(
        beacons: MutableList<Beacon>?,
        beaconRegion: BeaconRegion?
    ){
        val beacon = beaconOnDatabase?.filter { it.minor == beaconRegion?.minor?.toLong() }?.first() ?: return
        beaconsConnected().forEach {
            if (it.isRegistered) {
                Log.d(
                    "REGISTERED BEACON",
                    "BEACON 🔵 AT MINOR ${it.minor} - REGITERED ->${it.isRegistered}"
                )
            }
        }
        //  try {

        // } catch (e: Exception){}
        if (beacons?.size ?: 0 > 0)// &&  beacons?.first { it.minor == beacon.minor?.toInt() }?.rssi ?: -100 > if(beacon.beaconType == BeaconType.LOCATION){-90}else{-60})//
        {
            val rssi = beacons?.first { it.minor == beacon.minor?.toInt() }?.rssi ?: -100
            Log.i("LOGGER", "MINOR 🏧 ${beacon.minor?.toInt()} | RSSI 📶 ${rssi}")
            Log.i(
                "LOGGER", "RSSI 🔜 " + if (rssi >= -60) {
                    "✅🈯️🌀🈯️💹"
                } else {
                    "🛑⛔️⛔️🔆⛔️⛔️🛑"
                }
            )
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

            /***if(beaconsConnected().filter { it.minor == beacon.minor && it.isConected != true}.isEmpty()){
                val beaconConnect =  beaconsConnected().first { it.minor == beacon.minor && it.isConected != true}
                beaconConnect.expiration = 0
                beaconConnect.isConected = true
                beaconsConnected()[beaconsConnected().indexOf(beaconsConnected().first().minor == beaconConnect.minor)] = beaconConnect
                observer?.listOfBeacons = beaconsConnected()
                recordBeacon(beacon, BeaconEventType.ENTER)
            } else {
                //RENEW BEACON
                val beaconConnected =  beaconsConnected().first { it.minor == beacon.minor }
                beaconConnected.isConected = true
                beaconConnected.expiration = 0
                beaconsConnected()[beaconsConnected().indexOf(beaconsConnected().first().minor == beaconConnected.minor)] = beaconConnected
            }*/
            context.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,DottysBeaconArray(beaconAux).toJson())

        }
        else
        {
            if(beaconsConnected().filter { it.minor == beacon.minor && it.isConected == true || it.isRegistered }.isNotEmpty()){
                if(beaconsConnected().first{ it.minor == beacon.minor}.isConected != true && !beaconsConnected().first{ it.minor == beacon.minor}.isRegistered){return}
                val beaconToExpire = beaconsConnected().first{ it.minor == beacon.minor}
                        if(beaconToExpire.isConected == true){
                            Handler().postDelayed({
                                observer?.listOfBeacons = beaconsConnected()
                            }, 1000)
                        }
                        beaconToExpire.isConected = false
                        beaconToExpire.expiration += 1
                val auxList = beaconsConnected()
                auxList[beaconsConnected().indexOf(beaconsConnected().first{it.minor == beaconToExpire.minor})] = beaconToExpire
                context.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,DottysBeaconArray(auxList).toJson())

                if (beaconToExpire.expiration > 5){
                              recordBeacon(beaconToExpire,BeaconEventType.EXIT)
                          }
            }
//            if(beaconsConnected().filter { it.minor == beacon.minor }.isNotEmpty()){
//                val beaconExpired =  beaconsConnected().first { it.minor == beacon.minor }
//                beaconExpired.expiration = beaconExpired.expiration.plus(1)
//                beaconExpired.isConected = false
//                if (beaconExpired.expiration > 5){
//                    recordBeacon(beaconExpired,BeaconEventType.EXIT)
//                }
//            }
            }

        beaconsConnected()?.forEach { if(it.expiration > 0) {
            Log.e("EXPIRED", "BEACON -${it.minor}- 🧨 EXP ${it.expiration}")
        } }
        Log.i("DISCOVERY", "MINOR -- ${beaconRegion?.minor ?: ""}")
        Log.i(
            "DISCOVERY",
            "DATA 🚩 IN ARRAY -${beacons?.size}- AT MINOR 🟩 ${beaconRegion?.minor ?: ""}"
        )
        beaconTimerScanner()
    }

    private fun recordBeacon(beaconRecorded: DottysBeacon, eventType: BeaconEventType) {
        if (beaconRecorded.isRegistered && eventType == BeaconEventType.ENTER ||
            !beaconRecorded.isRegistered && eventType == BeaconEventType.EXIT
        ) {
            return
        }
//        if(beaconsConnected().none { it.isRegistered && it.beaconType == BeaconType.LOCATION } &&
//            beaconRecorded.beaconType ==  BeaconType.GAMING || beaconRecorded.isRegistered && eventType == BeaconEventType.ENTER) {
//                    return
//        }
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
                    beacon.minor?.toInt()
                )
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
        Log.i("SCANNER", "STARTED")
    }

    override fun onScanStop() {
        Log.i("SCANNER", "STOPED")
    }

    override fun onEnteredRegion(beaconRegion: BeaconRegion?, beacons: MutableList<Beacon>?) {
        Log.e("ENTER", "-- MINOR ** ${beaconRegion?.minor}")
    }

    override fun onExitedRegion(beaconRegion: BeaconRegion?) {
        Log.e("EXIT", "-- MINOR ** ${beaconRegion?.minor}")
    }

}

*/
interface BeaconHandlerDelegate {
    fun onBeaconViewChange(beaconsData: ArrayList<DottysBeacon>)
}

class BeaconHandlerObserver(listeners: BeaconHandlerDelegate) {
    var listOfBeacons: ArrayList<DottysBeacon> by Delegates.observable(
        initialValue = ArrayList<DottysBeacon>(),
        onChange = { _, _, new -> listeners.onBeaconViewChange(new) })
}