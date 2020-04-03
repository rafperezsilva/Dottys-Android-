package com.keylimetie.dottys.forgot_password

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R

class DottysForgotPasswordMainActivity : DottysBaseActivity() {
    private val forgotViewModel = DottysForgotPasswordViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_forgot_password_main)
        this.supportActionBar?.let {
            actionBarSetting(
                it,
                ColorDrawable(resources.getColor(R.color.colorPrimary))
            )
        }
        val titleBar = actionBarView?.findViewById<TextView>(R.id.title_bar)
        titleBar?.text = "Forgot Password"
        forgotViewModel.initForgotPasswordView(this)
    }
}
