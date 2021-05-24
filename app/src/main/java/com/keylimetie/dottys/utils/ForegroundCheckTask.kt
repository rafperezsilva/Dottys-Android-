package com.keylimetie.dottys.utils

import android.app.ActivityManager
import android.content.Context
import android.location.Location
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysLocationChangeDelegates
import com.keylimetie.dottys.DottysLocationObserver
import com.keylimetie.dottys.GpsTracker
import com.keylimetie.dottys.ui.locations.DottysLocationStoresObserver
import com.keylimetie.dottys.ui.locations.LocationsViewModel

enum class TaskBackgroundTimeInterval(val interval: Long) {
    SMALL_INTERVAL  ((1000 * 60* 60)), //1 Hora     0 * 60
    MEDIUM_INTERVAL ((1000 * 60* 120)), //2 Horas   0 * 60
    LONG_INTERVAL   ((1000 * 60* 240)), //4 HORAS   0 * 60
    ZERO_INTERVAL   ((1000 * 60* 15)) //25 Minutes  0 * 60
}
class ForegroundCheckTask(isAppOnBackground: Boolean, baseActivity: DottysBaseActivity) :
    AsyncTask<Context?, Void?, Boolean?>(), DottysLocationChangeDelegates {
     var isOnBackground = isAppOnBackground
     var activity = baseActivity
     var mainHandlerBackgorund:  Handler? = null
     var updateLocationInterval = TaskBackgroundTimeInterval.ZERO_INTERVAL
    protected fun doInBackground(vararg params: Context): Boolean {
        val context = params[0].applicationContext
        return isAppOnForeground(context)
    }

    private fun isAppOnForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses
            ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance === ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName
            ) {
                return true
            }
        }
        return false
    }

     fun triggerBackgroundTask(){

        if (mainHandlerBackgorund != null) {return}
        mainHandlerBackgorund = Handler(Looper.getMainLooper())
        mainHandlerBackgorund?.post(object : Runnable {
            override fun run() {

                val nearStore = activity.getUserNearsLocations().locations
                val newGps = GpsTracker(activity)
                newGps.getLocation(newGps)
                val gps = newGps.getLocation(newGps)//?.locationObserver = DottysLocationObserver(this)
                Log.d("---- ", "**  BACKGROUND THREAD\n LAT: ${gps?.latitude} /// LONG: ${gps?.longitude}")

                if (isOnBackground) {
                    if (nearStore.isNullOrEmpty()) {
                        Log.d("---- ", "** FOREGROUND NO NEAR LOCATION NEARLY")

                    } else {
                        Log.d("---- ", "\n ---------------------------------- \n ** FOREGROUND TASK ACTION TRIGERED ** \n INTERVAL: ${ updateLocationInterval.name} \n STORE: ${nearStore?.first().storeNumber} \n DISTANCE: ${nearStore?.first().distance} \n ----------------------------------  ")
                    }
                    mainHandlerBackgorund?.postDelayed(this, updateLocationInterval.interval)//updateLocationInterval.interval)
                } else {
                    mainHandlerBackgorund?.removeCallbacks(this)
                }
            }
        })

    }
  private fun  getStoresLocation(gps: Location){
      val locationsViewModel = LocationsViewModel(activity)
      if (activity.getUserPreference().token?.isEmpty() ?: return){ return }
      locationsViewModel.locationDataObserver = DottysLocationStoresObserver(activity)
      locationsViewModel.   getLocationsDottysRequest(activity,
          gps?.latitude.toString(),
          gps?.longitude.toString())

  }
    override fun doInBackground(vararg params: Context?): Boolean? {
        Log.d("BACKGROUND TASK", "ENTER IN BACK")
        activity.gpsTracker?.locationObserver = DottysLocationObserver(this)
        triggerBackgroundTask()
        return true
    }

    override fun onLocationChangeHandler(locationGps: Location?) {
        Log.d("---- ", "** 👣 FOREGROUND LOCATION CAHNGE")
        activity.gpsTracker?.locationGps = locationGps
         if (activity.isUpdatingLocation != true) {
             activity.isUpdatingLocation =  true
             getStoresLocation(locationGps ?: return)
         }
    }
}