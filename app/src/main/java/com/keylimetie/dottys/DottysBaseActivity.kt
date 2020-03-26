package com.keylimetie.dottys

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dottysrewards.dottys.service.VolleyService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.keylimetie.dottys.splash.DottysSplashActivity
import java.text.SimpleDateFormat
import java.util.*


enum class PreferenceTypeKey {
    USER_DATA, TOKEN
}

open class DottysBaseActivity : AppCompatActivity() {
    var sharedPreferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var actionBarView: View? = null
    var baseUrl: String? = null
    var progressBar: ProgressBar? = null
//    val MAP_VIEW_BUNDLE_KEY: String = "mapViewBundleKey"
//    val PERMISSION_ID = 123
//    lateinit var mFusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VolleyService.initialize(this)
        baseUrl = this.resources.getString(R.string.url_base_production)
        progressBar = findViewById(R.id.progress_loader)
        //hideLoader(this)
        sharedPreferences = this.getSharedPreferences(
            PreferenceTypeKey.USER_DATA.name,
            Context.MODE_PRIVATE
        )
        println(getUserPreference().token)
        //println("PERMISION LOCATION "+checkPermission())
        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        getLastLocation()
        val gpsTracker = GpsTracker(this)
        requestLocation(gpsTracker, this)


    }

    fun actionBarSetting(actionBar: ActionBar, coloBackground: ColorDrawable) {
        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar.setDisplayShowCustomEnabled(true)
        actionBar.setCustomView(R.layout.custom_action_bar)
        actionBar.elevation = 1F
        actionBarView = actionBar.customView
        actionBar.setBackgroundDrawable(coloBackground)
        val backButton = actionBarView?.findViewById<ImageButton>(R.id.back_image_button)
        backButton?.setOnClickListener {
            finish()
        }
    }

    fun getDiferencesDays(dateString: String): String {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS").parse(dateString.replace("Z", ""))
        val diferenceAtTime = date.time - Date().time
        return "end in " + (diferenceAtTime / (1000 * 3600 * 24)).toString() + " days"
    }

    fun saveDataPreference(keyPreference: PreferenceTypeKey, jsonData: String) {
        editor!!.putString(keyPreference.name, jsonData)
        editor!!.commit()
    }

    fun removeReferenceData(keyPreference: PreferenceTypeKey) {
        editor = sharedPreferences!!.edit()
        editor!!.remove(keyPreference.name)
        editor!!.commit()
    }

    fun getUserPreference(): DottysLoginResponseModel {
        val textoDate = sharedPreferences!!.getString(PreferenceTypeKey.USER_DATA.name, "")

        return try {
            var person: DottysLoginResponseModel =
                DottysLoginResponseModel.fromJson(
                    textoDate!!
                )
            person

        } catch (e: Exception) {
            println(e)
            DottysLoginResponseModel()
        }
    }

    fun showLoader(context: AppCompatActivity) {
        progressBar = context.findViewById<ProgressBar>(R.id.progress_loader)
        progressBar!!.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun hideLoader(context: AppCompatActivity) {
        progressBar = context.findViewById<ProgressBar>(R.id.progress_loader)
        progressBar?.visibility = View.INVISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }


    fun finishSession(mContext: DottysBaseActivity) {
        Toast.makeText(mContext, "User Logout", Toast.LENGTH_LONG).show()
        val editPref = mContext.sharedPreferences?.edit()
        editPref?.clear()
        editPref?.apply()
        mContext.removeReferenceData(PreferenceTypeKey.USER_DATA)
        val intent = Intent(mContext, DottysSplashActivity::class.java)
        ///**/ intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext.startActivity(intent)
        // mContext.finish()

    }

    /*
    * LOCATIONS PERMISSIONS
    * */
    open fun requestLocation(gpsTracker: GpsTracker?, activity: AppCompatActivity?) {
        try {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    101
                )
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
//        gpsTracker?.let { activity?.let { it1 -> getLocation(it, it1) } }
    }

    fun getLocation(gpsTracker: GpsTracker, activity: Context): LatLng {
        //  gpsTracker = GpsTracker(activity);
        if (gpsTracker.canGetLocation()) {
            val latitude = gpsTracker.getLatitude()
            val longitude = gpsTracker.getLongitude()
            return LatLng(latitude, longitude)
        } else {
            gpsTracker.showSettingsAlert()
        }
        return LatLng(0.0, 0.0)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show()
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show()
        }
    }

    fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }

/*
    private val keyboardLayoutListener = OnGlobalLayoutListener {
        val heightDiff = rootLayout!!.rootView.height - rootLayout!!.height
        val contentViewTop =
            window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
        val broadcastManager =
            LocalBroadcastManager.getInstance(this)
        if (heightDiff <= contentViewTop) {
            onHideKeyboard()
            val intent = Intent("KeyboardWillHide")
            broadcastManager.sendBroadcast(intent)
        } else {
            val keyboardHeight = heightDiff - contentViewTop
            onShowKeyboard(keyboardHeight)
            val intent = Intent("KeyboardWillShow")
            intent.putExtra("KeyboardHeight", keyboardHeight)
            broadcastManager.sendBroadcast(intent)
        }
    }

    private var keyboardListenersAttached = false
    private var rootLayout: ViewGroup? = null

    protected open fun onShowKeyboard(keyboardHeight: Int) {}
    protected open fun onHideKeyboard() {}

    protected open fun attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return
        }
        rootLayout = findViewById<View>(R.id.) as ViewGroup
        rootLayout!!.viewTreeObserver.addOnGlobalLayoutListener(keyboardLayoutListener)
        keyboardListenersAttached = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (keyboardListenersAttached) {
            rootLayout!!.viewTreeObserver.removeGlobalOnLayoutListener(keyboardLayoutListener)
        }
    }
    */

}