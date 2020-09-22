package com.keylimetie.dottys.game_play

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R
import pl.droidsonroids.gif.GifImageView


class DottysBubbleGameActivity : DottysBaseActivity(), View.OnClickListener {
    var bubble1: GifImageView? = null
    var bubble2: GifImageView? = null
    var bubble3: GifImageView? = null
    var bubble4: GifImageView? = null
    var bubble5: GifImageView? = null
    var imagesAtGame = ArrayList<Int>()
    var imagesGifArray = ArrayList<GifImageView?>()
    var popedBallons =  0
    private  var mp: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_bubble_game)
        this.supportActionBar?.let {actionBarSetting(it, ColorDrawable(resources.getColor(R.color.colorPrimary)))}
        val titleBar = actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = "Bubble Game"
        initGifImages()
        initGame()
    }

    private fun initGifImages(){
        bubble1 = findViewById<GifImageView>(R.id.bubble1)
        bubble2 = findViewById<GifImageView>(R.id.bubble2)
        bubble3 = findViewById<GifImageView>(R.id.bubble3)
        bubble4 = findViewById<GifImageView>(R.id.bubble4)
        bubble5 = findViewById<GifImageView>(R.id.bubble5)

        bubble1?.setOnClickListener(this)
        bubble2?.setOnClickListener(this)
        bubble3?.setOnClickListener(this)
        bubble4?.setOnClickListener(this)
        bubble5?.setOnClickListener(this)

       imagesGifArray.add(bubble1)
       imagesGifArray.add(bubble2)
       imagesGifArray.add(bubble3)
       imagesGifArray.add(bubble4)
       imagesGifArray.add(bubble5)


    }

    private  fun initGame(){
        initGifImages()
        popedBallons = 0
        val scratchActivity = DottysScratchAndWinActivity()
        imagesAtGame = scratchActivity.imagesAtGame(4)
        for (gif in imagesGifArray){
            gif?.setImageResource(R.mipmap.balloon)
            gif?.isClickable = true
            gif?.scaleX = 1f
            gif?.scaleY = 1f
        }

    }

    override fun onClick(v: View?) {
        mp  = MediaPlayer.create(this, R.raw.pop_balloon_audio)
        mp?.start()

        when(v?.id){
            R.id.bubble1 -> {
                bubble1?.setImageResource(R.mipmap.pop_balloon)
                v?.setOnClickListener(null)
              }
            R.id.bubble2 -> {
                bubble2?.setImageResource(R.mipmap.pop_balloon)
                v?.setOnClickListener(null)
             }
            R.id.bubble3 -> {
                bubble3?.setImageResource(R.mipmap.pop_balloon)
                v?.setOnClickListener(null)
             }
            R.id.bubble4 -> {
                bubble4?.setImageResource(R.mipmap.pop_balloon)
                v?.setOnClickListener(null)
             }
            R.id.bubble5 -> {
                bubble5?.setImageResource(R.mipmap.pop_balloon)
                v?.setOnClickListener(null)
             }
        }

        replaceImage(v as GifImageView)
    }

    @SuppressLint("ResourceAsColor")
    fun replaceImage(gif: GifImageView){

        Handler().postDelayed(
            {

                gif?.scaleX = 0.5f
                gif?.scaleY = 0.5f
                gif.scaleType = ImageView.ScaleType.CENTER_CROP
                gif?.setImageResource(imagesAtGame[popedBallons])
                popedBallons += 1
                 if (popedBallons == 5) {
                     Toast.makeText(this,getWinner(),Toast.LENGTH_LONG).show()
                     Handler().postDelayed(
                         {
                             initGame()
                         },
                         2000)
                 }

            },
            700 // value in milliseconds
        )
    }

    private fun getWinner():String{
        when {
            imagesAtGame.filter { it == R.mipmap.coffee_win}.size == 3 -> {
                return  "Coffee Win"
            }
            imagesAtGame.filter { it == R.mipmap.sandwich_win}.size == 3 -> {
                return   "Sandwich Win"
            }
            imagesAtGame.filter { it == R.mipmap.soda_win}.size == 3 -> {
                return   "Soda Win"
            }
            else -> {
                return   "Loser"
            }
        }

    }
}
