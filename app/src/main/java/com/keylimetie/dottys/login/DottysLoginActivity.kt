package com.keylimetie.dottys.login

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import com.keylimetie.dottys.*
import com.keylimetie.dottys.splash.getVersionApp
import com.keylimetie.dottys.ui.profile.DottysProfileDelegates
import com.keylimetie.dottys.ui.profile.DottysProfileObserver
import com.keylimetie.dottys.ui.profile.ProfileViewModel
import com.keylimetie.dottys.utils.geofence.GeofenceBroadcastReceiver


class DottysLoginActivity : DottysBaseActivity(), DottysLoginDelegate, DottysProfileDelegates {
     internal val viewModel = DottysLoginViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_login)

    }

    override fun onStart() {
        super.onStart()
        viewModel.initView(this)
        this.supportActionBar?.let { actionBarSetting(
            it,
            ColorDrawable(resources.getColor(R.color.colorPrimary))
        ) }
        val titleBar = actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = "Log In"
        setBackButton()
        backButton?.setImageResource(R.drawable.close_icon)
        removeReferenceData(PreferenceTypeKey.USER_DATA)
    }

    override fun onUserLogin(registerUserData: DottysLoginResponseModel) {
        saveDataPreference(PreferenceTypeKey.USER_DATA, registerUserData.toJson())
        saveDataPreference(PreferenceTypeKey.TOKEN, registerUserData.token)
        registerUserData.bluetooth = BluetoothAdapter.getDefaultAdapter().isEnabled
        registerUserData.trackLocation = if (GpsTracker(this).canGetLocation) {"ALWAYS"} else {"NEVER"}
        registerUserData.notifications = isPushNotificationEnable() ?: false
        registerUserData.deviceId = Settings.Secure.getString(contentResolver,Settings.Secure.ANDROID_ID)
        registerUserData.backgroundAppRefresh = true//isMyServiceRunning(GeofenceBroadcastReceiver::class.java)
        registerUserData.appVersion = "Android_${getVersionApp(this)}"
        val profileViewModel = ProfileViewModel(null, registerUserData)
        registerUserData.let {
            it?.let { it1 -> profileViewModel.uploadProfile(it1, this) }
            profileViewModel.profileUpdateObserver = DottysProfileObserver(this)
        }


    }
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    override fun onProfileUpdated(userProfile: DottysLoginResponseModel?) {
        val intent = Intent(this, DottysMainNavigationActivity::class.java)
        startActivity(intent)
        finish()
    }


}
