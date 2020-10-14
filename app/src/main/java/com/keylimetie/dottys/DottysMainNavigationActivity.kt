package com.keylimetie.dottys

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.keylimetie.dottys.redeem.DottysRewardRedeemedActivity
import com.keylimetie.dottys.register.DottysRegisterUserDelegates
import com.keylimetie.dottys.register.DottysRegisterUserObserver
import com.keylimetie.dottys.register.DottysRegisterViewModel
import com.keylimetie.dottys.splash.getVersionApp
import com.keylimetie.dottys.ui.dashboard.DottysPagerDelegates
import com.keylimetie.dottys.ui.drawing.DottysDrawingDelegates
import com.keylimetie.dottys.ui.drawing.DottysDrawingObserver
import com.keylimetie.dottys.ui.drawing.DrawingViewModel
import com.keylimetie.dottys.ui.drawing.RewardsSegment
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingUserModel
import com.keylimetie.dottys.utils.rotateBitmap
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.properties.Delegates


class DottysMainNavigationActivity : DottysBaseActivity(), DottysLocationChangeDelegates,
    DottysPagerDelegates, DottysDrawingDelegates, DottysRegisterUserDelegates {
    //, DottysBeaconStatusDelegate {
    val registerViewModel = DottysRegisterViewModel(this)
    var cameraPictureObserver: DottysProfilePictureObserver? = null
    private var drawingItemSelected: Int? = 0
    private var navView: NavigationView? = null
    var segmentSelect: RewardsSegment? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var controller: NavController // don't forget to initialize
    private lateinit var toolbar: Toolbar
    var selectedItemId: Int? = 0
    var image_uri: Uri? = null
    private val listener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            val logoAppBar = findViewById<ImageView>(R.id.logo_appbar)
            setTitleToolbar(destination.id)
            selectedItemId = destination.id
            when (destination.id) {
                R.id.nav_locations, R.id.nav_profile,
                R.id.nav_terms_and_conditions, R.id.nav_privacy_policy,
                R.id.nav_contact_suppport,
                -> {
                    toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    logoAppBar.visibility = View.INVISIBLE
                    window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
                }
                else -> {
                    when (destination.id) {
                        R.id.nav_dashboard -> {
                            logoAppBar.visibility = View.VISIBLE
                        }
                        else -> {
                            intent.putExtra("IS_DASHBOARD_BUTTON", false)
                            when (destination.id) {
                                R.id.nav_drawing -> {
                                    segmentSelect = RewardsSegment.DRAWING_ENTRIES
                                }
                                R.id.nav_rewards -> {
                                    segmentSelect = RewardsSegment.CASH_REWARDS
                                }
                            }
                            logoAppBar.visibility = View.INVISIBLE
                        }
                    }
                    toolbar.run { setBackgroundColor(resources.getColor(R.color.colorDottysGrey)) }
                    window.statusBarColor = ContextCompat.getColor(this, R.color.colorDottysGrey)
                }
            }
        }

    private fun getToolbarTitle(itemId: Int): String {
        when (itemId) {
            R.id.nav_locations -> {
                return "Locations"
            }
            R.id.nav_rewards -> {
                return "REDEEM REWARDS"
            }
            R.id.nav_profile -> {
                return "My Profile"
            }
            R.id.nav_contact_suppport -> {
                return "Help"
            }
//            R.id.nav_replay_tutorial -> {
//                return "Replay Tutorial"
//            }
            R.id.nav_drawing -> {
                return "CONVERT POINTS"
            }
            R.id.nav_terms_and_conditions -> {
                return "Terms & Conditions"
            }
            R.id.nav_privacy_policy -> {
                return "Privacy Policy"
            }
        }
        return ""
    }

    fun getSelectedItem(): Int? {
        return selectedItemId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_main_navigation)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        initDrawerSetting()
        if (intent.getBooleanExtra("VIEW_FROM_PROFILE", false)) {
            controller.navigate(R.id.nav_profile, intent.extras)
        }
//        if (gpsTracker == null) {
//            gpsTracker = GpsTracker(this)
//        }
//        gpsTracker?.getLocation()?.let { gpsTracker?.onLocationChanged(it) }
        mainNavigationActivity = this
        // getBeaconStatus()?.let { beaconService.listenerBeaconStatus(it, this) }

    }

    private fun initDrawerSetting() {

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val footerLabel = findViewById<TextView>(R.id.footer_label)
        footerLabel.text =
            "© 2020 Laredo Hospitality Group.\nAll rights reserved.\n${getVersionApp(this)}"
        controller = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard, R.id.nav_locations, R.id.nav_rewards,
                R.id.nav_drawing, R.id.nav_privacy_policy, R.id.nav_profile,
                R.id.nav_terms_and_conditions, //R.id.nav_replay_tutorial,
                R.id.nav_contact_suppport, R.id.nav_logout
            ), drawerLayout
        )

        setupActionBarWithNavController(controller, appBarConfiguration)
        navView?.setupWithNavController(controller)
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(p0: Int) {
                hideCustomKeyboard()
            }

            override fun onDrawerSlide(p0: View, p1: Float) {
            }

            override fun onDrawerClosed(p0: View) {
                // Toast.makeText(baseContext,"CLOSE  ",Toast.LENGTH_SHORT).show()
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }

            override fun onDrawerOpened(p0: View) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
//                     WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//                     WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                )
            }
        })
    }

    private fun setTitleToolbar(idItem: Int) {
        val textTitle = findViewById<TextView>(R.id.title_tool_bar_textview)
        textTitle.text = getToolbarTitle(idItem)
    }

    override fun onResume() {
        super.onResume()
        controller.addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        super.onPause()
        controller.removeOnDestinationChangedListener(listener)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {


    }
//FIXME DELETE IF NO USING IN
//    override fun onLocationChangeHandler(locationGps: Location?) {
//        //print(locationGps?.latitude)
//       //Toast.makeText(this, "Location has chande to \n Lat: ${locationGps?.latitude}\nLong: ${locationGps?.longitude}", Toast.LENGTH_LONG).show()
//        val dashboardFragment = DashboardFragment()
//        val locationsViewModel = LocationsViewModel(this)
//        if (getUserPreference().token?.isEmpty() ?: return){ return }
//       locationsViewModel.locationDataObserver = DottysLocationStoresObserver(dashboardFragment)
//       locationsViewModel.getLocationsDottysRequest(this,locationGps?.latitude.toString(),locationGps?.longitude.toString(), null)
//
//    }

    override fun getDrawingSelected(position: Int) {
        drawingItemSelected = position
        val drawingViewModel = DrawingViewModel()
        drawingViewModel.drawingObserver = DottysDrawingObserver(this)
        drawingViewModel.getUserDrawings(this)

    }

    override fun getUserRewards(rewards: DottysDrawingRewardsModel) {
        TODO("Not yet implemented")
    }

    override fun getUserDrawings(drawing: DottysDrawingUserModel) {
        var intent = Intent(this, DottysRewardRedeemedActivity::class.java)
        intent.putExtra("REDEEM_REWARDS_VIEW_TYPE", "DRAWING_ENTRIES")
        intent.putExtra("DRAWING_DATA",
            drawing.drawings?.reversed()?.get(drawingItemSelected ?: 0)?.toJson().toString())
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        registerViewModel.activityRegisterObserver = DottysRegisterUserObserver(this)
        if (resultCode == Activity.RESULT_OK) {
            try {

                val bitmapBase = MediaStore.Images.Media.getBitmap(contentResolver, image_uri)
                val bitmap = if (bitmapBase.width > bitmapBase.height) bitmapBase.rotateBitmap() else bitmapBase
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                cameraPictureObserver?.imageFromCamera = bitmap
                //val byteArray = pictureActivity.resizeBitmap(bitmap)
                registerViewModel.uploadImgage(this, stream.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }



    }

    override fun registerUser(userData: DottysLoginResponseModel) {}

    override fun imageProfileHasUploaded(hasUploaded: Boolean) {}

}


interface DottysOnProfilePictureTakenDelegate {
    fun onPictureTaken(bitmap: Bitmap?)
}

class DottysProfilePictureObserver(lisener: DottysOnProfilePictureTakenDelegate) {

    var imageFromCamera: Bitmap by Delegates.observable(
        initialValue = emptyBitmap(),
        onChange = { _, _, new -> lisener.onPictureTaken(new) })

    private fun emptyBitmap(): Bitmap {
        val w = 1
        val h = 1
        val conf = Bitmap.Config.ARGB_8888
        return Bitmap.createBitmap(w, h, conf)
//        val canvas = Canvas(bmp)
    }
}



