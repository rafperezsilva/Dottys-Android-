package com.playspinwin.dottys.game_play.roulette

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.playspinwin.dottys.R


public class RouletteWheelView(context: Context) {
    val x = Int
    val y = Int
    var wheelBitmap: Bitmap? = null

   init {
       wheelBitmap =  BitmapFactory.decodeResource(context.resources, R.mipmap.sandwich_win)
   }

}