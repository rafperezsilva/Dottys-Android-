package com.playspinwin.dottys.game_play

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.playspinwin.dottys.DottysBaseActivity
import com.playspinwin.dottys.R
import com.playspinwin.dottys.game_play.roulette.DottysRouletteActivity

class DottysMainGamePlayActivity : DottysBaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_main_game_play)
        this.supportActionBar?.let {actionBarSetting(it, ColorDrawable(resources.getColor(R.color.colorPrimary)))}
        val titleBar = actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = "GAME PLAY"
        initItemsAtView()
    }

    override fun onClick(v: View?) {
        when(v?.id){
           R.id.scratch_game_button -> {
               val intent = Intent(this, DottysScratchAndWinActivity::class.java)
               startActivity(intent)
           }
           R.id.bubble_game_button -> {
               val intent = Intent(this,  DottysBubbleGameActivity::class.java)
               startActivity(intent)
           }
           R.id.slot_machine_button -> {
               val intent = Intent(this,  DottysSlotMachineActivity::class.java)
               startActivity(intent)


           }
           R.id.roulette_button -> {
               val intent = Intent(this,  DottysRouletteActivity::class.java)
               startActivity(intent)
           }
        }
    }

   private fun initItemsAtView(){
        val scratchButton = findViewById<Button>(R.id.scratch_game_button)
        val bubbleGameButton = findViewById<Button>(R.id.bubble_game_button)
        val slotMachineGameButton = findViewById<Button>(R.id.slot_machine_button)
        val rouletteGameButton = findViewById<Button>(R.id.roulette_button)
        scratchButton.setOnClickListener(this)
        bubbleGameButton.setOnClickListener(this)
        slotMachineGameButton.setOnClickListener(this)
        rouletteGameButton.setOnClickListener(this)


    }
}
