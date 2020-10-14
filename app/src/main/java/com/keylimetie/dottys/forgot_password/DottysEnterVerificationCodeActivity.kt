package com.keylimetie.dottys.forgot_password

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.keylimetie.dottys.*
import com.keylimetie.dottys.register.DottysProfilePictureActivity

class DottysEnterVerificationCodeActivity : DottysBaseActivity(), DottysForgotPasswordDelegates {
    var viewFromProfile: Boolean? = null
    private val forgotViewModel = DottysForgotPasswordViewModel()
    var user: DottysLoginResponseModel? = null
    var isFromVerifyCell: Boolean? = null
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
              isFromVerifyCell = intent.getBooleanExtra("VERIFY_CELL", false)
            val isRegisterType = intent.getBooleanExtra("REGISTER_VIEW_TYPE", false)
            val userData = intent.getStringExtra("USER_DATA")
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
