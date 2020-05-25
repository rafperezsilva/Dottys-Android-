package com.keylimetie.dottys.forgot_password

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.login.DottysLoginActivity

class DottysEnterPasswordActivity : DottysBaseActivity(), DottysForgotPasswordDelegates {
    private var viewFromProfile: Boolean? = false
    private val forgotViewModel = DottysForgotPasswordViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_enter_password)
        this.supportActionBar?.let {actionBarSetting(it, ColorDrawable(resources.getColor(R.color.colorPrimary)))}
        val titleBar = actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = "Forgot Password"
        val code = intent.getStringExtra("DATA_CODE")
        val mail = intent.getStringExtra("EMAIL_FORGOT")
        viewFromProfile = intent.getBooleanExtra("VIEW_FROM_PROFILE", false)
        forgotViewModel.initChangePasswordView(this,code,mail)
    }

    override fun sendVerificationPassword(isSucces: Boolean) {

    }

    override fun sendVerificationRegistrationCode(isSucces: Boolean) { }

    override fun changePassword(isSucces: Boolean) {
        if (isSucces){
            if (viewFromProfile ?: false){
                val intent = Intent(this, DottysMainNavigationActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("VIEW_FROM_PROFILE",   viewFromProfile)
                startActivity(intent)
            } else {
                val intent = Intent(this, DottysLoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}
