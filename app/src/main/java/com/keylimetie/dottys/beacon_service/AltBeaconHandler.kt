import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.util.Log
import com.keylimetie.dottys.DottysBaseActivity
import org.altbeacon.beacon.*

//package com.keylimetie.dottys.beacon_service
//
//import android.content.Context
//import android.content.Intent
//import android.content.ServiceConnection
//import android.os.Bundle
//import android.os.RemoteException
//import android.util.Log
//import com.keylimetie.dottys.DottysBaseActivity
//import org.altbeacon.beacon.*
//
//
class AltBeaconHandler(private val context: DottysBaseActivity) {



//    override fun onBeaconServiceConnect() {
//        beaconManager?.removeAllMonitorNotifiers();
//        beaconManager?.addMonitorNotifier(object : MonitorNotifier {
//            override fun didEnterRegion(p0: Region?) {
//                Log.i(TAG, "I just saw an beacon for the first time!");
//            }
//
//            override fun didExitRegion(p0: Region?) {
//                Log.i(TAG, "I no longer see an beacon");
//            }
//
//            override fun didDetermineStateForRegion(p0: Int, p1: Region?) {
//                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + p0);
//            }
//        })
//
//        beaconManager?.addRangeNotifier { beacons, region ->
//            if (beacons.size > 0) {
//                Log.i(
//                    TAG,
//                    "The first beacon I see is about " + beacons.iterator().next()
//                        .distance.toString() + " meters away."
//                )
//            }
//        }
//
//            try {
//                val beacons = context.getDottysBeaconsList() ?: return
//                for (beacon in beacons){
//                   val region =  Region(beacon.uuid ?: "",
//                       null,
//                        beacon.id
//                    )
//                    beaconManager?.startMonitoringBeaconsInRegion(region)
//                    beaconManager?.startRangingBeaconsInRegion(region)
//                }
//
//
//             } catch (e: RemoteException) { Log.e(TAG, "${e.message}")    }
//    }
//


}