package com.keylimetie.dottys

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.keylimetie.dottys.splash.DottysSplashActivity
import com.keylimetie.dottys.utils.geofence.Constants.REQUEST_PERMISSIONS_REQUEST_CODE


class SplashAnimationActivity : AppCompatActivity() {
    override fun onStart() {
        super.onStart()
        print("ONSTART  SAPLSSH")
    }
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
                    dialog.cancel()
                })
            alertDialog.show()

        } else{

        val intent = Intent(this, DottysSplashActivity::class.java)
        startActivity(intent)
        finish()
    }
    }
}

/**
 * Return the current state of the permissions needed.
 */
  fun DottysBaseActivity.checkMainPermissions():Boolean {
    val permissionState1 = ActivityCompat.checkSelfPermission(this,
        android.Manifest.permission.ACCESS_FINE_LOCATION)

    val permissionState2 = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_COARSE_LOCATION)

    return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED
}
/**
 * Start permissions requests.
 */
  fun DottysBaseActivity.requestMainPermissions() {
    val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
        android.Manifest.permission.ACCESS_FINE_LOCATION)

    val shouldProvideRationale2 = ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.ACCESS_COARSE_LOCATION)


    // Provide an additional rationale to the img_user. This would happen if the img_user denied the
    // request previously, but didn't check the "Don't ask again" checkbox.
    if (shouldProvideRationale || shouldProvideRationale2)
    {
        Log.i("TAG", "Displaying permission rationale to provide additional context.")
        showMainSnackbar(R.string.permission_rationale,
            android.R.string.ok, View.OnClickListener {
                // Request permission
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
            })
    }
    else
    {
        Log.i("TAG", "Requesting permission")
        // Request permission. It's possible this can be auto answered if device policy
        // sets the permission in a given state or the img_user denied the permission
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE)
    }
}

internal fun DottysBaseActivity.showMainSnackbar(mainTextStringId:Int, actionStringId:Int, listener:View.OnClickListener) {
    Snackbar.make(
        this.findViewById(android.R.id.content), this.getString(mainTextStringId),
        Snackbar.LENGTH_INDEFINITE
    ).setAction(this.getString(actionStringId), listener).show()
}