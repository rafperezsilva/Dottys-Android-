package com.keylimetie.dottys.game_play

 import android.graphics.drawable.ColorDrawable
 import android.os.Bundle
 import android.widget.TextView
 import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R

class DottysScratchAndWinActivity : DottysBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_scratch_and_win)
        this.supportActionBar?.let {actionBarSetting(it, ColorDrawable(resources.getColor(R.color.colorPrimary)))}
        val titleBar = actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = "Scratch Game"
    }
}
