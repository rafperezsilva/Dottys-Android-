package com.keylimetie.dottys.splash

import android.app.Activity
import android.content.Intent
import android.util.DisplayMetrics
import android.widget.Button
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.ViewPager
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.login.DottysLoginActivity
import com.viewpagerindicator.CirclePageIndicator


class DottysSplashViewModel : ViewModel() {
    private lateinit var viewPager: ViewPager
    private lateinit var loginButton: Button
    private lateinit var sigupButton: Button
    private val displayMetrics = DisplayMetrics()

     fun initView(context: DottysSplashActivity){
        viewPager = context.findViewById(R.id.splashViewPager)
        loginButton = context.findViewById(R.id.login_splash_button)
        sigupButton = context.findViewById(R.id.sign_up_splash_button)
        context.windowManager.defaultDisplay.getMetrics(displayMetrics)

         val indicator =  context.findViewById(R.id.indicator) as CirclePageIndicator
         viewPager.adapter = CustomPagerAdapter(
             context,
             initSplashData(context),
             displayMetrics
         )
         indicator.setViewPager(viewPager)
         buttonLisener(context)

         if (context.getUserPreference().token != null) {
             val intent = Intent(context, DottysMainNavigationActivity::class.java)
             context.startActivity(intent)
         } else {
             //context.actionBarSetting()
         }
    }

    fun initSplashData(context:Activity):ArrayList<DottysPagerModel>{
        var firstSplash = DottysPagerModel(
            "",
            R.mipmap.dottys_splash_first_image,
            context.getString(R.string.splash_first_text)
        )
        var secondSplash = DottysPagerModel(
            context.getString(R.string.title_second_top_splas),
            R.mipmap.dottys_splash_second_image,
            context.getString(R.string.splash_second_text)
        )
        var thirdSplash = DottysPagerModel(
            context.getString(R.string.title_third_splash_item),
            R.mipmap.dottys_splash_third_image,
            context.getString(R.string.subtitle_third_splash_item)
        )
        var fourSplash = DottysPagerModel(
            context.getString(R.string.title_four_splash_item),
            R.mipmap.dottys_splash_four_image,
            context.getString(
                R.string.subtitle_four_splash_item
            )
        )
        var listData = ArrayList<DottysPagerModel>()
        listData.add(firstSplash)
        listData.add(secondSplash)
        listData.add(thirdSplash)
        listData.add(fourSplash)
        return  listData
    }


    fun buttonLisener(context:Activity){
        loginButton.setOnClickListener {
            val intentLogin = Intent(context, DottysLoginActivity::class.java)
            context.startActivity(intentLogin)
        }
        sigupButton.setOnClickListener {
        }
    }


}
