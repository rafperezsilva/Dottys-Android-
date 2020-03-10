package com.keylimetie.dottys.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R

class DottysLoginActivity : DottysBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_login)

    }

    override fun onStart() {
        super.onStart()
        val titleBar = actionBarView?.findViewById<TextView>(R.id.title_bar)

        titleBar?.text = "Log in"
    }
}
