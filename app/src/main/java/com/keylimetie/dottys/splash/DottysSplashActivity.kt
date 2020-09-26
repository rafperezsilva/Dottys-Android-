package com.keylimetie.dottys.splash

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.game_play.roulette.DottysRouletteActivity


class DottysSplashActivity : DottysBaseActivity() {
    private val viewModel = DottysSplashViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

          setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN

        )



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
