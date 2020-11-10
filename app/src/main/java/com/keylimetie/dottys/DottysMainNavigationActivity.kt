package com.keylimetie.dottys

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.icu.math.BigDecimal
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
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
import com.keylimetie.dottys.ui.dashboard.AnalyticBeacoonsAdapter
import com.keylimetie.dottys.ui.dashboard.DashboardFragment
import com.keylimetie.dottys.ui.dashboard.DottysPagerDelegates
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.drawing.DottysDrawingDelegates
import com.keylimetie.dottys.ui.drawing.DottysDrawingObserver
import com.keylimetie.dottys.ui.drawing.DrawingViewModel
import com.keylimetie.dottys.ui.drawing.RewardsSegment
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingUserModel
import com.keylimetie.dottys.ui.locations.LocationsFragment
import com.keylimetie.dottys.utils.rotateBitmap
import kotlinx.android.synthetic.main.activity_dottys_main_navigation.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.properties.Delegates


class DottysMainNavigationActivity : DottysBaseActivity(), DottysLocationChangeDelegates,
    DottysPagerDelegates, DottysDrawingDelegates, DottysRegisterUserDelegates, View.OnClickListener
      {
    var drawerLayout: DrawerLayout? = null
    var viewAnalitycs: ConstraintLayout? = null
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
                    /// R.id.nav_contact_suppport,
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
                    //window.statusBarColor = ContextCompat.getColor(this, R.color.colorDottysGrey)
//                    @Suppress("DEPRECATION")
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        window.insetsController?.hide(WindowInsets.Type.statusBars())
//                    } else {
//                        window.setFlags(
//                            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                            WindowManager.LayoutParams.FLAG_FULLSCREEN)
//                    }
//                    window.setFlags(
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN
//
//                    )
//                    val decorView = window.decorView
//                    val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
//                    decorView.systemUiVisibility = uiOptions
                }
            }
        }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.e("LOG    ", "${item?.itemId}")
        return super.onContextItemSelected(item)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.e("LOG    ", "${item?.itemId}")
        return super.onOptionsItemSelected(item)
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
//            R.id.nav_contact_suppport -> {
//                return "Help"
//            }
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

        mainNavigationActivity = this
         viewAnalitycs    = findViewById<ConstraintLayout>(R.id.analitycs_floating_view)
        viewAnalitycs?.animate()?.translationY(-screenSize.y.toFloat())?.setDuration(800)?.start()


    }

    override fun onRestart() {
        super.onRestart()
         requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN

        )

    }


    fun initAnalitycsItems(beaconList: ArrayList<DottysBeacon>?) {
        val storeLocation =
            findViewById<TextView>(R.id.location_analitycs_store) //?: return
        val closeAnalyticButton     =  findViewById<Button>(R.id.close_analytics_buttom) //?: return
        val userHostIdTextView      =  findViewById<TextView>(R.id.user_host_id) //?: return
        val locationEnableTextView  =  findViewById<TextView>(R.id.location_enable_textview) //?: return
        val locationDeviceTextView  =  findViewById<TextView>(R.id.location_device_analytic_textview) //?: return
        val sendToSupportButton     =  findViewById<Button>(R.id.send_to_support_button) //?: return
        val isPushEnable            =  findViewById<TextView>(R.id.is_push_enable_textview) //?: return
      //  isPushEnable?.text = if(mainFragmentActivity?.isPushNotificationEnabled() == true) "Enable" else "Disable"
        sendToSupportButton?.setOnClickListener(this)
        closeAnalyticButton?.setOnClickListener(this)

        userHostIdTextView.text = getUserPreference()?.id ?: ""
        val trackerLocation =  gpsTracker
        var isEnableLocation = "Disable"
        if (trackerLocation?.isGPSEnabled == true) {
            isEnableLocation = "Enable"
        }
        val lat = trackerLocation?.locationGps?.latitude ?:  gpsTracker?.getLatitude()
        val long = trackerLocation?.locationGps?.longitude ?: gpsTracker?.getLongitude()

        val latitude = BigDecimal(lat.toString()).setScale(3, 1)
        val longitude = BigDecimal(long.toString()).setScale(3, 1)

        locationDeviceTextView?.text = "Lat: $latitude | Long: $longitude"
        locationEnableTextView?.text = isEnableLocation
        if (storeLocation != null) {
            storeLocation.text = if(!getBeaconStatus()?.beaconArray.isNullOrEmpty())
                (getBeaconStatus()?.beaconArray?.first()?.location?.storeNumber ?: 0).toString()
            else
                "Has no nearest locations"

//                "Store #${
//                    mainFragmentActivity?.getBeaconStatus().let {
//                        it.let {
//                            it?.beaconArray?.let { it.first()?.location?.storeNumber }
//                        }
//                    }
//                        ?: ""
//                }"


        }
        var listViewRewards =
            findViewById<ListView>(R.id.beacons_analytics_listview)
        if (this == null) {
            return
        }
        listViewRewards?.adapter = AnalyticBeacoonsAdapter(
            this,
            beaconList ?: getBeaconStatus()?.beaconArray ?: return
        )
    }

    private fun initDrawerSetting() {

       drawerLayout  = findViewById<DrawerLayout>(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val footerLabel = findViewById<TextView>(R.id.footer_label)
        footerLabel.text =
            "© 2020 Illinois Cafe' & Service Company, LLC.\nAll rights reserved.\n${getVersionApp(
                this)}"
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

        drawerLayout?.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(p0: Int) {
                hideCustomKeyboard()
            }

            override fun onDrawerSlide(p0: View, p1: Float) {
            }

            override fun onDrawerClosed(p0: View) {
            }

            override fun onDrawerOpened(p0: View) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
        })
                navView?.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        //
    }
    val mOnNavigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId){
            R.id.nav_contact_suppport -> {
                initAnalitycsItems(getDottysBeaconsList())
                viewAnalitycs?.animate()?.translationY(0f)?.setDuration(800)?.start()
            }
            else ->{
               controller.navigate(item.itemId)
            }
        }
        drawerLayout?.close()
        return@OnNavigationItemSelectedListener true
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

    override fun getDrawingSelected(position: Int) {
        drawingItemSelected = position
        val drawingViewModel = DrawingViewModel()
        drawingViewModel.drawingObserver = DottysDrawingObserver(this)
        drawingViewModel.getUserDrawings(this)

    }

    override fun getUserRewards(rewards: DottysDrawingRewardsModel) {
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

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.close_analytics_buttom -> {
                viewAnalitycs?.animate()?.translationY(-screenSize.y.toFloat())?.setDuration(800)?.start()
            }
            R.id.send_to_support_button -> {
                sendMailToSupport(this)
            }
        }
     }

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



