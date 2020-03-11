package com.keylimetie.dottys

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.keylimetie.dottys.ui.dashboard.DashboardFragment
import com.keylimetie.dottys.ui.gallery.GalleryFragment

class DottysMainNavigationActivity : DottysBaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var controller: NavController // don't forget to initialize
    private lateinit var toolbar: Toolbar

    private val listener = NavController.OnDestinationChangedListener { controller, destination, arguments ->

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // react on change
        // you can check destination.id or destination.label and act based on that
        val logoAppBar = findViewById<ImageView>(R.id.logo_appbar)
        controller.currentDestination
        when (destination.id) {
            R.id.nav_dashboard -> {
                Toast.makeText(this,"DASHBOARD",Toast.LENGTH_SHORT).show()
                toolbar.setBackgroundColor(resources.getColor(R.color.colorDottysGrey))
                logoAppBar.visibility = View.VISIBLE
                window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorDottysGrey))

            }
            R.id.nav_gallery -> {
                Toast.makeText(this,"GALERY",Toast.LENGTH_SHORT).show()
                toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                logoAppBar.visibility = View.INVISIBLE
                window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimary))

//                var params = logoAppBar.layoutParams
//                params.height = 0
//                logoAppBar.layoutParams = params
            }
            else -> {
                logoAppBar.visibility = View.INVISIBLE
            }
        }
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

        )
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_main_navigation)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        controller = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send
            ), drawerLayout
        )


        setupActionBarWithNavController(controller, appBarConfiguration)
        navView.setupWithNavController(controller)

    }

    override fun onResume() {
        super.onResume()
        controller.addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        super.onPause()
        controller.removeOnDestinationChangedListener(listener)
    }

 //region
//    private fun onItemNacItemSelected(navView: NavigationView, toolbar: Toolbar, drawerLayout: DrawerLayout, navController: NavController){
//        val fragmentManager: FragmentManager = supportFragmentManager
//        val ft: FragmentTransaction = fragmentManager.beginTransaction()
//        navView.setNavigationItemSelectedListener {
//            when (it.itemId) {
//                R.id.nav_dashboard -> {
//                    toolbar.setBackgroundColor(R.color.colorPrimary)
//                    drawerLayout.closeDrawers()
//                    ft.replace(R.layout.fragment_home,DashboardFragment())
//                    true
//                }
//                R.id.nav_gallery -> {
//                    toolbar.setBackgroundColor(R.color.colorAccent)
//                    drawerLayout.closeDrawers()
//                    ft.replace(R.layout.fragment_gallery,GalleryFragment())
//                    true
//                }
//                else -> true
//            }
//
//        }
//        ft.commit()

//     }
//endregion

    private fun getCheckedItem(navigationView: NavigationView): Int {
        val menu = navigationView.menu
        for (i in 0 until menu.size()) {
            val item: MenuItem = menu[i]
            if (item.isChecked) {
                return i
            }
        }
        return -1
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.dottys_main_navigation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS

        )
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
