package com.playspinwin.dottys

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.playspinwin.dottys.splash.DottysSplashActivity


class SplashAnimationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN

        )
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            // Setting Dialog Title

            alertDialog.setTitle("Allow Background\nLocation Access")
            // Setting Dialog Message
            alertDialog.setCancelable(false)
            alertDialog.setMessage(DottysBaseActivity().locationMsgFormatedText())
            // On pressing Settings button
            alertDialog.setNeutralButton("Settings",
                DialogInterface.OnClickListener { dialog, which ->
                    val intent =
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                })

            // on pressing cancel button
            alertDialog.setPositiveButton("Continue",
                DialogInterface.OnClickListener { dialog, which ->
                    val intent = Intent(this, DottysSplashActivity::class.java)
                    startActivity(intent)
                    finish()
                    dialog.cancel() })
            alertDialog.show()

        } else{

        val intent = Intent(this, DottysSplashActivity::class.java)
        startActivity(intent)
        finish()
    }
    }
}