package com.keylimetie.dottys.forgot_password

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.widget.TextView
import android.widget.Toast
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.R
import com.keylimetie.dottys.register.DottysProfilePictureActivity

class DottysVerificationTypeActivity : DottysBaseActivity(), DottysForgotPasswordDelegates {
    private val forgotViewModel = DottysForgotPasswordViewModel()
    var strUser: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.let {actionBarSetting(it, ColorDrawable(resources.getColor(R.color.colorPrimary)))}
        val titleBar = actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = "Forgot Password"
        setContentView(R.layout.activity_dottys_verification_type)
//        var bundle: Bundle? = intent.extras
//         message =
//            bundle?.get("EMAIL_FORGOT") as SpannableString //?.getString("EMAIL_FORGOT", "")
          strUser = intent.getStringExtra("EMAIL_FORGOT") // 2

        forgotViewModel.initValidationView(this, strUser ?: "")
        //  Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }

    override fun sendVerificationPassword(isSucces: Boolean) {
        if(isSucces){
            var intent = Intent(this, DottysEnterVerificationCodeActivity::class.java)
            intent.putExtra("EMAIL_FORGOT", strUser)
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
