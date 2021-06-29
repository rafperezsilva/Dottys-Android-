package com.keylimetie.dottys.beacon_service


import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.saveDataPreference
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import com.keylimetie.dottys.utils.DottysBeaconReferenceApplication
import org.altbeacon.beacon.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class BeaconsHandler(
    private val context: DottysBaseActivity,
    private val observer: BeaconHandlerObserver?
){
    lateinit var dottysBeaconReferenceApplication: DottysBeaconReferenceApplication
    var handlerData = Handler(Looper.getMainLooper())//ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    var runnable: Runnable? = null
    
    //var handlerDataTask: Handler? = null
    var beaconManager: BeaconManager? = null
    var taskCounter = 0
    //var beaconOnConnection = ArrayList<DottysBeacon>()
    var beaconOnDatabase =
        context.getDottysBeaconsList()//Preferences.getNearestBeaconsStored(context)?.beacons

    fun beaconsConnected(): ArrayList<DottysBeacon> {
        return context.getBeaconStatus()?.beaconArray ?: beaconOnDatabase ?: ArrayList()
    }

    // This gets called from the BeaconReferenceApplication when monitoring events change changes
    private val monitoringObserver = Observer<Int> { state ->
       var stateString = "inside"
        if (state == MonitorNotifier.OUTSIDE) {

            stateString == "outside"
               }
        else {
        }
        Log.d("TAG", "♦️🟩🟦🟦🟪 monitoring state changed to : $stateString")


    }

    private val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d("TAG", "Ranged: ${beacons.count()} beacons")
        if (beacons.size > 0) {
         Log.e("🎩 RANGIN BEACONS", "${beacons.size} $context")
            try {
                //beacons.forEach { Log.d("POWER ", " 👓 MINOR ${it.id3} 🎯 TX: ${it.rssi}") }
            beaconHandler(beacons)
               // beacons.forEach { Log.d("BEACON DETECTED","AT MINOR ${it.id3}") }
            } catch (e: Exception){
                Log.d("BEACON DETECTED","ERROR: ${e.message}")
            }
        }
    }

    internal fun beaconHandler(beacons:Collection<Beacon>){
        Log.e("CHECK TASK 🏁", "${context.delayBeaconChecker} | $taskCounter")
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
            taskCounter = 0

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
        dottysBeaconReferenceApplication.beaconsObserver =   BeaconEventObserver(context)
        val regions = ArrayList<Region>()
        beaconsConnected().forEach { regions.add(
            Region(
                Identifier.parse(it.id).toString(),
                Identifier.fromUuid(UUID.fromString(it.uuid))!!,
                Identifier.fromInt(it.major?.toInt() ?: 0), Identifier.fromInt(it.minor?.toInt() ?: 0)))
        }
        dottysBeaconReferenceApplication.initBootStarpRegion(regions)
        // beaconReferenceApplication.monitoringData.state.observe(context, monitoringObserver)
        dottysBeaconReferenceApplication.rangingData.beacons.observe(context, rangingObserver)
}

 var isUpdatingBeacon = false

    private fun recordBeacon(beaconRecorded: DottysBeacon, eventType: BeaconEventType) {
        if (beaconRecorded.isRegistered && eventType == BeaconEventType.ENTER ||
            !beaconRecorded.isRegistered && eventType == BeaconEventType.EXIT ||
            isUpdatingBeacon
        ) {
            return
        }
        isUpdatingBeacon = true
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

    internal fun beaconTimerScanner() {
        taskCounter = 0
        handlerData.removeCallbacksAndMessages(null)
        handlerData.removeCallbacks(runnable!!)
        handlerData.postDelayed(Runnable {
            handlerData.postDelayed(runnable!!, context.delayBeaconChecker.toLong())
            runnableNewAction()
        }.also { runnable = it }, context.delayBeaconChecker.toLong())
    }

    private fun runnableNewAction(){
        if(beaconsConnected().filter { it.isRegistered || it.isConected == true}.size == 0){
            handlerData.removeCallbacksAndMessages(null)
            handlerData.removeCallbacks(runnable!!)
            return
        }
        Log.e("RUNNEABLE🕒", "${context.delayBeaconChecker} | $taskCounter")
        if(taskCounter == 7){
            taskCounter=0
            beaconsConnected().filter { it.isRegistered }.forEach {
                recordBeacon(it,BeaconEventType.EXIT)
            }
        }
        if(taskCounter > 7) {
            taskCounter = 0
        }
                    taskCounter +=1

    }

    private fun requestPermission() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()
        }
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