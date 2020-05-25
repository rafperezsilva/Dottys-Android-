package com.keylimetie.dottys.forgot_password

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R

class DottysVerificationTypeActivity : DottysBaseActivity(), DottysForgotPasswordDelegates {
    private var viewFromProfile: Boolean? = null
    private val forgotViewModel = DottysForgotPasswordViewModel()
    var strUser: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.let {actionBarSetting(it, ColorDrawable(resources.getColor(R.color.colorPrimary)))}
        val titleBar = actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = "Forgot Password"
        setContentView(R.layout.activity_dottys_verification_type)
          strUser = intent.getStringExtra("EMAIL_FORGOT")
          viewFromProfile = intent.getBooleanExtra("VIEW_FROM_PROFILE", false)
        forgotViewModel.initValidationView(this, strUser ?: "")

    }

    override fun sendVerificationPassword(isSucces: Boolean) {
        if(isSucces){
            var intent = Intent(this, DottysEnterVerificationCodeActivity::class.java)
            intent.putExtra("EMAIL_FORGOT", strUser)
            intent.putExtra("VIEW_FROM_PROFILE",   viewFromProfile)
            startActivity(intent)
        }
    }

    override fun sendVerificationRegistrationCode(isSucces: Boolean) {
        if (isSucces) {
//            sharedPreferences = getSharedPreferences(
//                PreferenceTypeKey.USER_DATA.name,
//                Context.MODE_PRIVATE
//            )
//            editor = sharedPreferences!!.edit()
//            saveDataPreference(PreferenceTypeKey.USER_DATA,us)
//            var intent = Intent(this, DottysProfilePictureActivity::class.java)
//            startActivity(intent)
        }
    }

    override fun changePassword(isSucces: Boolean) {
        if(isSucces){
            finish()
//            var intent = Intent(this, DottysEnterPasswordActivity::class.java)
//            intent.putExtra("DATA_CODE","123456")
//            startActivity(intent)
        }
    }
}
