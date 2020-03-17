package com.keylimetie.dottys.login

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.keylimetie.dottys.*

class DottysLoginActivity : DottysBaseActivity(), DottysLoginDelegate {



    private val viewModel = DottysLoginViewModel()

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_login)
        viewModel.initView(this)
         this.supportActionBar?.let { actionBarSetting(it, ColorDrawable(resources.getColor(R.color.colorPrimary    ))) }
    }

    override fun onUserLogin(registerUserData: DottysLoginResponseModel) {
        saveDataPreference(PreferenceTypeKey.USER_DATA, registerUserData.toJson())
        val intent = Intent(this, DottysMainNavigationActivity::class.java)
        startActivity(intent)

    }

}
