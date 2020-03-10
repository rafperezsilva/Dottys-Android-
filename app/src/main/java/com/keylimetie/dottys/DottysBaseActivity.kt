package com.keylimetie.dottys

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import androidx.appcompat.app.ActionBar

import android.view.View
import android.widget.ImageButton
import android.widget.TextView


open class DottysBaseActivity: AppCompatActivity() {

    var actionBarView:  View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}