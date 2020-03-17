package com.keylimetie.dottys

import android.os.Bundle
import android.view.Menu
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
import com.keylimetie.dottys.ui.reusable_fragment.ReusableFragment

class DottysMainNavigationActivity : DottysBaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var controller: NavController // don't forget to initialize
    private lateinit var toolbar: Toolbar
     var selectedItemId: Int? = 0

    private val listener = NavController.OnDestinationChangedListener { controller, destination, arguments ->
        val logoAppBar = findViewById<ImageView>(R.id.logo_appbar)
        setTitleToolbar(destination.id)
        selectedItemId = destination.id

        when (destination.id) {
            R.id.nav_locations, R.id.nav_profile,
            R.id.nav_terms_and_conditions, R.id.nav_privacy_policy,
            R.id.nav_contact_suppport -> {
                 toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                logoAppBar.visibility = View.INVISIBLE
                window.statusBarColor = ContextCompat.getColor(this,R.color.colorPrimary)

            }
            else -> {
                when(destination.id){
                    R.id.nav_dashboard -> {
                        logoAppBar.visibility = View.VISIBLE
                    }
                    else -> {
                        logoAppBar.visibility = View.INVISIBLE
                    }
                }
                toolbar.setBackgroundColor(resources.getColor(R.color.colorDottysGrey))
                window.statusBarColor = ContextCompat.getColor(this,R.color.colorDottysGrey)
            }
        }
    }

    fun getToolbarTitle(itemId:Int):String{
        when (itemId){
            R.id.nav_locations -> {return "Locations"}
            R.id.nav_rewards -> {return "REDEEM REWARDS"}
            R.id.nav_profile -> {return "Profile"}
            R.id.nav_contact_suppport -> {return "Help"}
            R.id.nav_replay_tutorial -> {return "Replay Tutorial"}
            R.id.nav_drawing -> {return "CONVERT POINTS"}
            R.id.nav_terms_and_conditions, R.id.nav_privacy_policy -> {return "Legal"}
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
    }

     fun initDrawerSetting(){

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
                 R.id.nav_contact_suppport,  R.id.nav_logout
             ), drawerLayout
         )

         setupActionBarWithNavController(controller, appBarConfiguration)
         navView.setupWithNavController(controller)
         drawerLayout.addDrawerListener(object:DrawerLayout.DrawerListener{
             override fun onDrawerStateChanged(p0: Int) {
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

    fun setTitleToolbar(idItem:Int){
        var textTitle = findViewById<TextView>(R.id.title_tool_bar_textview)
        textTitle.text =  getToolbarTitle(idItem)
    }
    override fun onResume() {
        super.onResume()
        controller.addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        super.onPause()
        controller.removeOnDestinationChangedListener(listener)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.dottys_main_navigation, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {



    }
}
