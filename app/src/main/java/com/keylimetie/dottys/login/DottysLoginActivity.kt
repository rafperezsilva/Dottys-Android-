package com.keylimetie.dottys.login

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.keylimetie.dottys.*

class DottysLoginActivity : DottysBaseActivity(), DottysLoginDelegate {
     internal val viewModel = DottysLoginViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_login)

    }

    override fun onStart() {
        super.onStart()
        viewModel.initView(this)
        this.supportActionBar?.let { actionBarSetting(it, ColorDrawable(resources.getColor(R.color.colorPrimary))) }
        val titleBar = actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = "Log In"
        setBackButton()
        backButton?.setImageResource(R.drawable.close_icon)
        removeReferenceData(PreferenceTypeKey.USER_DATA)
    }

    override fun onUserLogin(registerUserData: DottysLoginResponseModel) {
        saveDataPreference(PreferenceTypeKey.USER_DATA, registerUserData.toJson())
        saveDataPreference(PreferenceTypeKey.TOKEN, registerUserData.token)
        val intent = Intent(this, DottysMainNavigationActivity::class.java)
        startActivity(intent)

    }




}
