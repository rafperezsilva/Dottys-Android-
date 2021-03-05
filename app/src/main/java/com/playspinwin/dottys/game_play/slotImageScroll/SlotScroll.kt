package com.playspinwin.dottys.game_play.slotImageScroll

import android.animation.Animator
import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.playspinwin.dottys.R
import kotlinx.android.synthetic.main.slot_image_scroll.view.*

class SlotScroll: FrameLayout {

    internal lateinit var eventEnd: EventEnd
    internal var lastResult = 0
    internal var oldValue = 0
    private  var mp: MediaPlayer? = null
    companion object {
        private const val ANIMATION_DURATION = 150
    }

    val value: Int
        get() = Integer.parseInt(nextImage.tag.toString())

    fun setEventEnd(eventEnd: EventEnd){
        this.eventEnd = eventEnd
    }

    constructor(context: Context) : super(context){
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        init(context)
    }

    private fun init(context: Context){
        LayoutInflater.from(context).inflate(R.layout.slot_image_scroll, this)
        nextImage.translationY = height.toFloat()
        mp = MediaPlayer.create(context,R.raw.roll_machine_sound)
        //mp?.start()
    }

    fun setRandomValue(image:Int, numRoll:Int){
        currentImage.animate()
            .translationY(-height.toFloat())
            .setDuration(ANIMATION_DURATION.toLong()).start()

        nextImage.translationY = nextImage.height.toFloat()
        nextImage.animate()
            .translationY(0f).setDuration(ANIMATION_DURATION.toLong())
            .setListener(object: Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    setImage(currentImage, oldValue%6)
                    currentImage.translationY = 0f
                    if(oldValue != numRoll) {
                        setRandomValue(image,numRoll)
                        oldValue++
                        if (mp?.isPlaying == false){
                            mp = MediaPlayer.create(context,R.raw.roll_machine_sound)
                            mp?.start()
                        }
                    }
                    else {
                        mp?.stop()
                        mp = MediaPlayer.create(context,R.raw.clack_machine_sound)
                        mp?.start()
                        lastResult = 0
                        oldValue = 0
                        setImage(nextImage, image)
                        eventEnd.eventEnd(image, numRoll)

                    }

                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }

            }).start()

    }

    //!! symbol is for asserting non-null to variables
    private fun setImage(currentImage: ImageView?, value: Int){
        when (value) {
            Utils.coffee -> currentImage!!.setImageResource(R.mipmap.coffee_win)
            Utils.sandwinch -> currentImage!!.setImageResource(R.mipmap.sandwich_win)
            Utils.soda -> currentImage!!.setImageResource(R.mipmap.soda_win)
//            Utils.seven -> currentImage!!.setImageResource(R.mipmap.coffee_win)
//            Utils.triple -> currentImage!!.setImageResource(R.mipmap.sandwich_win)
//            Utils.watermelon -> currentImage!!.setImageResource(R.mipmap.soda_win)
        }

        currentImage!!.tag = value
        lastResult = value
    }



}