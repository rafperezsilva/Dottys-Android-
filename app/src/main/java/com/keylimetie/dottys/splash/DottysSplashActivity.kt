package com.keylimetie.dottys.splash

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R


class DottysSplashActivity : DottysBaseActivity() {
      val viewModel = DottysSplashViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN

        )

    }

    override fun onStart() {
        super.onStart()


//        val intent = Intent(this,  DottysRouletteActivity::class.java)
//        startActivity(intent)
//  return
        viewModel.initView(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.conainer.alpha = 1f
    }
}
