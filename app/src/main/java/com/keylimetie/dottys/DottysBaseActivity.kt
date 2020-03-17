package com.keylimetie.dottys

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import androidx.appcompat.app.ActionBar

import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.dottysrewards.dottys.service.VolleyService
import com.keylimetie.dottys.splash.DottysSplashActivity

enum class PreferenceTypeKey {
    USER_DATA, TOKEN
}

open class DottysBaseActivity: AppCompatActivity() {
    var sharedPreferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var actionBarView:  View? = null
    var baseUrl:  String? = null
    var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VolleyService.initialize(this)
        baseUrl = this.resources.getString(R.string.url_base_production)
        progressBar = findViewById(R.id.progress_loader)
        //hideLoader(this)
        sharedPreferences =  this.getSharedPreferences(
            PreferenceTypeKey.USER_DATA.name,
            Context.MODE_PRIVATE
        )
        println(getUserPreference().token)

    }

     fun actionBarSetting(actionBar: ActionBar, coloBackground: ColorDrawable){
        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar.setDisplayShowCustomEnabled(true)
        actionBar.setCustomView(R.layout.custom_action_bar)
        actionBar.elevation = 1F
        actionBarView = actionBar.customView
         actionBar.setBackgroundDrawable(coloBackground)
        val backButton = actionBarView?.findViewById<ImageButton>(R.id.back_image_button)
        backButton?.setOnClickListener {
            finish()
        }
    }

    fun saveDataPreference(keyPreference: PreferenceTypeKey, jsonData: String) {
        editor!!.putString(keyPreference.name, jsonData)
        editor!!.commit()
    }

    fun removeReferenceData(keyPreference: PreferenceTypeKey) {
        editor =  sharedPreferences!!.edit()
        editor!!.remove(keyPreference.name)
        editor!!.commit()
    }

    fun getUserPreference(): DottysLoginResponseModel {
        val textoDate = sharedPreferences!!.getString(PreferenceTypeKey.USER_DATA.name, "")

        return try {
            var person: DottysLoginResponseModel =
                DottysLoginResponseModel.fromJson(
                    textoDate!!
                )
            person

        } catch (e: Exception) {
            println(e)
            DottysLoginResponseModel()
        }
    }

    fun showLoader(context: AppCompatActivity) {
        progressBar = context.findViewById<ProgressBar>(R.id.progress_loader)
        progressBar!!.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun hideLoader(context: AppCompatActivity) {
        progressBar = context.findViewById<ProgressBar>(R.id.progress_loader)
        progressBar?.visibility = View.INVISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }


    fun  finishSession(mContext: DottysBaseActivity){
        Toast.makeText(mContext, "User Logout", Toast.LENGTH_LONG).show()
        val editPref = mContext.sharedPreferences?.edit()
        editPref?.clear()
        editPref?.apply()
        mContext.removeReferenceData(PreferenceTypeKey.USER_DATA)
        val intent = Intent(mContext, DottysSplashActivity::class.java)
        ///**/ intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext.startActivity(intent)
        // mContext.finish()

    }
}