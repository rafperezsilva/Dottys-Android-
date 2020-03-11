package com.keylimetie.dottys.login

import android.content.Intent
import android.os.Bundle
 import com.keylimetie.dottys.*

class DottysLoginActivity : DottysBaseActivity(), DottysLoginDelegate {



    private val viewModel = DottysLoginViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_login)
        viewModel.initView(this)
    }

    override fun onUserLogin(registerUserData: DottysLoginResponseModel) {
        saveDataPreference(PreferenceTypeKey.USER_DATA, registerUserData.toJson())
        val intent = Intent(this, DottysMainNavigationActivity::class.java)
        startActivity(intent)

    }

}
