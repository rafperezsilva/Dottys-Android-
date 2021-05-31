package com.keylimetie.dottys

 import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
 import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.keylimetie.dottys.splash.DottysSplashActivity


class SplashAnimationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN

        )

//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//            == PackageManager.PERMISSION_GRANTED
//        ) {
//            if (checkSelfPermission(Manifest.permission)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
//                    val builder = AlertDialog.Builder(this)
//                    builder.setTitle("This app needs background location access")
//                    builder.setMessage("Please grant location access so this app can detect beacons in the background.")
//                    builder.setPositiveButton(R.string.ok, null)
//                    builder.setOnDismissListener {
//                        requestPermissions(
//                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
//                            PERMISSION_REQUEST_BACKGROUND_LOCATION
//                        )
//                    }
//                    builder.show()
//                } else {
//                    val builder = AlertDialog.Builder(this)
//                    builder.setTitle("Functionality limited")
//                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.")
//                    builder.setPositiveButton(R.string.ok, null)
//                    builder.setOnDismissListener { }
//                    builder.show()
//                }
//            }
//        } else {
//            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                requestPermissions(
//                    arrayOf(
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                    ),
//                    PERMISSION_REQUEST_FINE_LOCATION
//                )
//            } else {
//                val builder = AlertDialog.Builder(this)
//                builder.setTitle("Functionality limited")
//                builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.")
//                builder.setPositiveButton(R.string.ok, null)
//                builder.setOnDismissListener { }
//                builder.show()
//            }
//        }


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