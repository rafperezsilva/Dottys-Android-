package com.keylimetie.dottys.forgot_password

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.R
import com.keylimetie.dottys.register.DottysProfilePictureActivity

class DottysEnterVerificationCodeActivity : DottysBaseActivity(), DottysForgotPasswordDelegates {
    var viewFromProfile: Boolean? = null
    private val forgotViewModel = DottysForgotPasswordViewModel()
    var user: DottysLoginResponseModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_enter_verification_code)
        this.supportActionBar?.let {
            actionBarSetting(
                it,
                ColorDrawable(resources.getColor(R.color.colorPrimary))
            )
            val titleBar = actionBarView?.findViewById<TextView>(R.id.title_bar)
            titleBar?.text = "Forgot Password"
            val mail = intent.getStringExtra("EMAIL_FORGOT")
            val isRegisterType = intent.getBooleanExtra("REGISTER_VIEW_TYPE", false)
            val userData = intent.getStringExtra("USER_DATA")
            viewFromProfile = intent.getBooleanExtra("VIEW_FROM_PROFILE", false)
            /*IF LET IMPLEMENTATION*/
            user = if (userData != null) {
                DottysLoginResponseModel.fromJson(userData)
            } else {
                getUserPreference()
            }

            forgotViewModel.initVerificationCodeView(this,mail, isRegisterType)
        }
    }

    override fun sendVerificationPassword(isSucces: Boolean) { }

    override fun sendVerificationRegistrationCode(isSucces: Boolean) {
        if (isSucces) {
            sharedPreferences = getSharedPreferences(
                PreferenceTypeKey.USER_DATA.name,
                Context.MODE_PRIVATE
            )
            editor = sharedPreferences!!.edit()
            user?.toJson()?.let { saveDataPreference(PreferenceTypeKey.USER_DATA, it) }
            var intent = Intent(this, DottysProfilePictureActivity::class.java)
            startActivity(intent)
        }
    }
    override fun changePassword(isSucces: Boolean) {
     }
}
