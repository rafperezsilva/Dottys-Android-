package com.keylimetie.dottys.splash

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.ViewPager
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.login.DottysLoginActivity
import com.keylimetie.dottys.register.DottysRegisterActivity
import com.viewpagerindicator.CirclePageIndicator


class DottysSplashViewModel : ViewModel() {
    private lateinit var viewPager: ViewPager
    private lateinit var loginButton: Button
    private lateinit var sigupButton: Button

      lateinit var conainer: ConstraintLayout

     fun initView(context: DottysSplashActivity){
        viewPager = context.findViewById(R.id.splashViewPager)
        loginButton = context.findViewById(R.id.login_splash_button)
        sigupButton = context.findViewById(R.id.sign_up_splash_button)
         conainer = context.findViewById(R.id.splash_page_container)
         loginButton.isEnabled = true
         sigupButton.isEnabled = true
          conainer.alpha = 0f
         if ((context.getUserPreference().token?.isEmpty() != false).not()) {
             val intent = Intent(context, DottysMainNavigationActivity::class.java)
             context.startActivity(intent)
         }
         context.windowManager.defaultDisplay.getMetrics(context.displayMetrics)
         val appVersionLabel = context.findViewById<TextView>(R.id.version_app)
         appVersionLabel.text = context.getVersionApp(context)
         val indicator =  context.findViewById(R.id.indicator) as CirclePageIndicator
         viewPager.adapter = CustomPagerAdapter(
             context,
             initSplashData(context),
             context.displayMetrics
         )
         indicator.setViewPager(viewPager)
         buttonLisener(context)


    }

    private fun initSplashData(context:Activity):ArrayList<DottysPagerModel>{

        val firstSplash = DottysPagerModel(
            "",
            R.mipmap.dottys_splash_first_image,
            context.getString(R.string.splash_first_text)
        )
        val secondSplash = DottysPagerModel(
            context.getString(R.string.title_second_top_splas),
            R.mipmap.dottys_splash_second_image,
            context.getString(R.string.splash_second_text)
        )
        val thirdSplash = DottysPagerModel(
            context.getString(R.string.title_third_splash_item),
            R.mipmap.dottys_splash_third_image,
            context.getString(R.string.subtitle_third_splash_item)
        )
        val fourSplash = DottysPagerModel(
            context.getString(R.string.title_four_splash_item),
            R.mipmap.dottys_splash_four_image,
            context.getString(
                R.string.subtitle_four_splash_item
            )
        )
        val listData = ArrayList<DottysPagerModel>()
        listData.add(firstSplash)
        listData.add(secondSplash)
        listData.add(thirdSplash)
        listData.add(fourSplash)
        return  listData
    }


    private fun buttonLisener(context: Activity) {
        loginButton.setOnClickListener {
            loginButton.isEnabled = false
            val intentLogin = Intent(context, DottysLoginActivity::class.java)
            context.startActivity(intentLogin)
        }
        sigupButton.setOnClickListener {
            sigupButton.isEnabled = false
            val intent = Intent(context, DottysRegisterActivity::class.java)
            context.startActivity(intent)
        }
    }


}

fun DottysBaseActivity.getVersionApp(context: AppCompatActivity): String {

    return try {
        val pInfo = context.packageManager.getPackageInfo(packageName, 0)
        val version = pInfo.versionName
        @Suppress("DEPRECATION") val verCode = pInfo.versionCode
        "V$version.$verCode"
    } catch (e: Error) {
        Log.d("ERROR VERSION", e.toString())
        ""
    }
}