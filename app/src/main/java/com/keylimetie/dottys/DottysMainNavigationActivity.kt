package com.keylimetie.dottys

import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.keylimetie.dottys.ui.dashboard.DashboardFragment
import com.keylimetie.dottys.ui.dashboard.DashboardViewModel
import com.keylimetie.dottys.ui.drawing.RewardsSegment
import com.keylimetie.dottys.ui.locations.DottysLocationsStoresModel
import com.keylimetie.dottys.ui.locations.LocationsViewModel


class DottysMainNavigationActivity : DottysBaseActivity(), DottysLocationDelegates,
    com.keylimetie.dottys.ui.locations.DottysLocationDelegates {

    var segmentSelect: RewardsSegment? =  null
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var controller: NavController // don't forget to initialize
    private lateinit var toolbar: Toolbar
    var selectedItemId: Int? = 0

    private val listener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            val logoAppBar = findViewById<ImageView>(R.id.logo_appbar)
            setTitleToolbar(destination.id)
            selectedItemId = destination.id

            when (destination.id) {
                R.id.nav_locations, R.id.nav_profile,
                R.id.nav_terms_and_conditions, R.id.nav_privacy_policy,
                R.id.nav_contact_suppport -> {
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
                             when(destination.id) {

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
                    toolbar.setBackgroundColor(resources.getColor(R.color.colorDottysGrey))
                    window.statusBarColor = ContextCompat.getColor(this, R.color.colorDottysGrey)
                }
            }
        }

    fun getToolbarTitle(itemId: Int): String {
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
            R.id.nav_replay_tutorial -> {
                return "Replay Tutorial"
            }
            R.id.nav_drawing -> {
                return "CONVERT POINTS"
            }
            R.id.nav_terms_and_conditions, R.id.nav_privacy_policy -> {
                return "Legal"
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
        if(intent.getBooleanExtra("VIEW_FROM_PROFILE", false)) {
            controller.navigate(R.id.nav_profile, intent.extras)
        }
        if (gpsTracker == null) {
            gpsTracker = GpsTracker(this)
        }
        gpsTracker?.getLocation()?.let { gpsTracker?.onLocationChanged(it) }
 }

    fun initDrawerSetting() {

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        controller = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard, R.id.nav_locations, R.id.nav_rewards,
                R.id.nav_drawing, R.id.nav_privacy_policy, R.id.nav_profile,
                R.id.nav_terms_and_conditions, R.id.nav_replay_tutorial,
                R.id.nav_contact_suppport, R.id.nav_logout
            ), drawerLayout
        )

        setupActionBarWithNavController(controller, appBarConfiguration)
        navView.setupWithNavController(controller)
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

    fun setTitleToolbar(idItem: Int) {
        var textTitle = findViewById<TextView>(R.id.title_tool_bar_textview)
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


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.dottys_main_navigation, menu)
//        return true
//    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {


    }
    override fun onLocationChangeHandler(locationGps: Location?) {
        print(locationGps?.latitude)
       Toast.makeText(this, "Location has chande to \n Lat: ${locationGps?.latitude}\nLong: ${locationGps?.longitude}", Toast.LENGTH_LONG).show()
         val locationsViewModel = LocationsViewModel()
//        val vcMain = DottysMainNavigationActivity()
       locationsViewModel.getLocationsDottysRequest(this,locationGps?.latitude.toString(),locationGps?.longitude.toString(), null)

    }

    override fun getStoresLocation(locations: DottysLocationsStoresModel) {
       // var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        editor =  sharedPreferences!!.edit()
        saveDataPreference(PreferenceTypeKey.LOCATIONS,locations.toJson())
        val currentFragment = DashboardViewModel()
        currentFragment.initAnalitycsItems()
        //currentFragment.getBeaconList("")
    }

    override fun allItemsCollapse(isColappse: Boolean) {

    }

//    override fun onLocationChangeHandler(locationGps: Location?) {
//        Toast.makeText(this, "Location has chande to \n Lat: ${locationGps?.latitude}\nLong: ${locationGps?.longitude}", Toast.LENGTH_LONG).show()
//        val locationsViewModel = LocationsViewModel()
//        val vcMain = DottysMainNavigationActivity()
//        locationsViewModel.getLocationsDottysRequest(vcMain,locationGps?.latitude.toString(),locationGps?.longitude.toString(), null)
//    }

    fun getForegroundFragment(): Fragment? {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_dashboard)
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }
}



