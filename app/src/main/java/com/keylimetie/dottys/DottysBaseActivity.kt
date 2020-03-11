package com.keylimetie.dottys

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import androidx.appcompat.app.ActionBar

import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.dottysrewards.dottys.service.VolleyService

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
        sharedPreferences =  this.getSharedPreferences(
            PreferenceTypeKey.USER_DATA.name,
            Context.MODE_PRIVATE
        )
        println(getUserPreference().token)

    }

    fun actionBarSetting(){
        this.supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true);
        supportActionBar?.setCustomView(R.layout.custom_action_bar)
        supportActionBar?.elevation = 1F
        actionBarView = supportActionBar?.customView
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
}