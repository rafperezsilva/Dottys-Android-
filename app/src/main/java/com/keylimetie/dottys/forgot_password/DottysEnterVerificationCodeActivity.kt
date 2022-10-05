package com.keylimetie.dottys.forgot_password

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.ColorDrawable
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.viewModelScope
import com.keylimetie.dottys.*
import com.keylimetie.dottys.models.DottysLoginResponseModel
import com.keylimetie.dottys.register.DottysProfilePictureActivity

class DottysEnterVerificationCodeActivity : DottysBaseActivity(), DottysForgotPasswordDelegates {
    private val SMS_RECEIVED: String = "android.provider.Telephony.SMS_RECEIVED"

    var receiver: BroadcastReceiver? = null
    var viewFromProfile: Boolean? = null
    internal val forgotViewModel = DottysForgotPasswordViewModel()
    var user: DottysLoginResponseModel? = null
    var isFromVerifyCell: Boolean? = null

    private val intentFilter = IntentFilter().apply {
        addAction(SMS_RECEIVED)
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_enter_verification_code)
        this.supportActionBar?.let {
            actionBarSetting(
                it,
                ColorDrawable(resources.getColor(R.color.colorPrimary))
            )
            val titleBar = actionBarView?.findViewById<TextView>(R.id.title_bar)
            receiver = SmsListener(this)
            val mail = intent.getStringExtra("EMAIL_FORGOT")
              isFromVerifyCell = intent.getBooleanExtra("VERIFY_CELL", false)
            val isRegisterType = intent.getBooleanExtra("REGISTER_VIEW_TYPE", false)
            val userData = intent.getStringExtra("USER_DATA")
            val smsCode = intent.getStringExtra("SMS_CODE")
            for(i in forgotViewModel.editTextArray.indices){
                if (smsCode?.length ?: 0 > 0){
                    forgotViewModel.editTextArray[i]?.setText("${smsCode?.get(i)}")
                }
            }
//                for (i in  forgotViewModel.editTextArray.indices) {
//                    print(array[i])
//                }if (smsCode?.length ?: 0 > 0){
//                it.setText(it.)
//            }


            titleBar?.text = if (isRegisterType){
                "Register"
            }else {
                "Forgot Password"
            }
            viewFromProfile = intent.getBooleanExtra("VIEW_FROM_PROFILE", false)
            /*IF LET IMPLEMENTATION*/
            user = if (userData != null) {
                DottysLoginResponseModel.fromJson(userData)
            } else {
                getUserPreference()
            }

            mail?.let { it1 -> forgotViewModel.initVerificationCodeView(this, it1, isRegisterType) }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver,intentFilter)
        Log.i("RESUME FP","REUSUME")
    }

    override fun sendVerificationPassword(isSucces: Boolean) { }

    override fun sendVerificationRegistrationCode(isSucces: Boolean) {
        if (isSucces) {
            if (isFromVerifyCell == true){
                var intent = Intent(this, DottysMainNavigationActivity::class.java)
                startActivity(intent)
            } else {
                sharedPreferences = getSharedPreferences(
                    PreferenceTypeKey.USER_DATA.name,
                    Context.MODE_PRIVATE
                )
                editor = sharedPreferences!!.edit()
                user?.toJson()?.let { saveDataPreference(PreferenceTypeKey.USER_DATA, it) }
                var intent = Intent(this, DottysProfilePictureActivity::class.java)
                startActivity(intent)
            }
        } else {
            forgotViewModel.clearDataInFields(forgotViewModel.editTextArray)
        }
    }

    override fun changePassword(isSucces: Boolean) {
     }
}
