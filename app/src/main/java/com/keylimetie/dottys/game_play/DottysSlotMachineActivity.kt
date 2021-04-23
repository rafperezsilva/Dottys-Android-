package com.keylimetie.dottys.game_play

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.game_play.slotImageScroll.EventEnd
import com.keylimetie.dottys.game_play.slotImageScroll.Utils
import com.keylimetie.dottys.ui.locations.showSnackBarMessage
import kotlinx.android.synthetic.main.activity_dottys_slot_machine.*
import kotlin.random.Random

class DottysSlotMachineActivity : DottysBaseActivity(), EventEnd {

    private var countDown = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_slot_machine)
        this.supportActionBar?.let {actionBarSetting(it, ColorDrawable(resources.getColor(R.color.colorPrimary)))}
        val titleBar = actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = "Slot Machine"
       // requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        image1.setEventEnd(this@DottysSlotMachineActivity)
        image2.setEventEnd(this@DottysSlotMachineActivity)
        image3.setEventEnd(this@DottysSlotMachineActivity)

        leverUp.setOnClickListener{
          //  if(Utils.score >= 50){
                leverUp.visibility = View.INVISIBLE
                leverDown.visibility = View.VISIBLE
                image1.setRandomValue(Random.nextInt(3), Random.nextInt(15-5+1)+5)
                image2.setRandomValue(Random.nextInt(3), Random.nextInt(15-5+1)+5)
                image3.setRandomValue(Random.nextInt(3), Random.nextInt(15-5+1)+5)
                Utils.score -= 50
              //  score_tv.text = Utils.score.toString()
//            }else{
//                DottysBaseActivity().showSnackBarMessage(this,"You dont have enough money")
//            }
        }
    }

    override fun eventEnd(result: Int, count: Int) {
        if(countDown < 2){
            countDown++
        }
        else{
            leverDown.visibility = View.GONE
            leverUp.visibility = View.VISIBLE
            countDown = 0

            if(image1.value == image2.value && image2.value == image3.value){
                DottysBaseActivity().showSnackBarMessage(this,"YOU WON!!!!")
                Utils.score +=300
            //    score_tv.text = Utils.score.toString()
            }
//            else if(image1.value == image2.value || image2.value == image3.value || image1.value == image3.value){
//                DottysBaseActivity().showSnackBarMessage(this,"You did good.")
//                Utils.score +=100
//             //   score_tv.text = Utils.score.toString()
//            }
            else{
                DottysBaseActivity().showSnackBarMessage(this,"You lost. Better luck next time.")
                Utils.score +=0
              //  score_tv.text = Utils.score.toString()
            }
        }
    }
}
