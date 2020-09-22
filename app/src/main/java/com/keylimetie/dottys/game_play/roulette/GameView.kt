package com.keylimetie.dottys.game_play.roulette

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.keylimetie.dottys.R
import java.util.*


class GameView(context: Context?, image: ImageView) : SurfaceView(context), Runnable {
    private val bmp: Bitmap
    var holderSurfgce: SurfaceHolder? =  null
    private var thread = Thread()
    var isPlaying: Boolean = false
    var imageAtGame = image
    var valueAnimator: ValueAnimator? = null
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.RED)
        canvas.drawBitmap(bmp, 10f, 10f, null)
    }

    init {
        holderSurfgce = holder

        holderSurfgce?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) {}
            override fun surfaceCreated(holder: SurfaceHolder) {
                val c: Canvas = holder.lockCanvas(null)
                draw(c)
                holder.unlockCanvasAndPost(c)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int,
                width: Int, height: Int
            ) {
            }
        })
        bmp = BitmapFactory.decodeResource(resources, R.drawable.coffee_rulette)
        val minX = 360.0f
        val maxX = 720.0f

        val rand = Random()

        val finalX: Float = rand.nextFloat() * (maxX - minX) + minX
        valueAnimator = ValueAnimator.ofFloat(0f, 360f)
     }
@SuppressLint("WrongConstant")
fun startAnimation(){
    if (!isPlaying) {
        valueAnimator?.addUpdateListener {
            val value = it.animatedValue as Float
            // 2
            imageAtGame.rotation = value
        }
        valueAnimator?.interpolator = LinearInterpolator()
        valueAnimator?.duration = (500).toLong()
        valueAnimator?.repeatCount = ObjectAnimator.INFINITE
       // valueAnimator?.repeatMode = RESTART / REVERSE
        isPlaying = true

        valueAnimator?.addUpdateListener(AnimatorUpdateListener { animation ->
            Log.d("UPDATE ANIM ", animation.animatedValue.toString())
            Log.d("DURATION ANIM ", valueAnimator?.duration.toString())

            if (animation.duration.plus(animation.duration ) > 0) {
                animation.duration =
                    animation.duration.plus(animation.duration )
            }
        })
        valueAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                 Log.d("END", "AINMATION ENDED")
                isPlaying = false
                valueAnimator?.duration =
                    valueAnimator?.duration?.plus(valueAnimator?.duration ?: 0L) ?: 0L
                if(valueAnimator?.duration ?: return < 10000) {
                    //valueAnimator?.start()
                } else {
                    valueAnimator?.pause()
                }
              //  valueAnimator.duration = (5000).toLong()
            }
        })
        valueAnimator?.start()
    }
}

    override fun run() {
        Log.d("Runnable", "Runnable has started")
        while (isPlaying){
            update()
            draw()
            sleep()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.run {
            Log.d("On Touch", "calling runneable")
            resumeGame()
        }
        return true
    }

     fun resumeGame(){
        Log.d("GAME STATE", "RUNNING RESUMED")
         startAnimation()
//        thread = Thread(this)
//        thread.start()
    }

    private fun  update() {
        Log.d("GAME STATE", "RUNNING UPDATE")

    }

    private fun  draw() {

    }

    private fun  sleep() {

    }
}
//
//class GameView(context: Context, private val size: Point, private val rectGame: RectF):  SurfaceView(context), Runnable {
//
//    private var thread = Thread()
//    private var isPlaying: Boolean = false
////    private var circleRoulette: Bitmap? = null
//
//
//     val paint: Paint = Paint()
//     var circleX : Float
//     var circleY : Float
//     var circleRadius: Float = ((size.x * 0.7) / 2).toFloat()
//    init {
//        paint.isFilterBitmap = true
//        paint.isAntiAlias = true
//        paint.color = Color.GREEN
//        circleX = (size.x / 2).toFloat()
//        circleY = (size.y / 2).toFloat()
//     //   circleRoulette = itemGamblinImage()
//        Log.d("SIZE", "${size.x} // ${size.y} ")
//    }
////region
////    override fun draw(canvas: Canvas?) {
////        super.draw(canvas)
////        canvas?.drawColor(Color.CYAN)
////
////        canvas?.drawBitmap(itemGamblinImage(),0f,0f,paint)
////        canvas?.drawCircle(circleX,circleY,circleRadius,paint)
////    }
////
////    override fun onTouchEvent(event: MotionEvent?): Boolean {
////        circleX = event!!.x
////        circleY = event!!.y
////        invalidate()
////        return true
////    }
////endregion
//
//
//
//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        event?.run {
//            when(action){
//                MotionEvent.ACTION_DOWN -> {
//                    if (!isPlaying) {
//                        resumeGame()
//                    } else {
//                        pauseGame()
//                    }
//                }
//            }
//        }
////        circleX =  event?.x ?: 0f
////        circleY = event?.y ?: 0f
//        return true
//    }
//
//    private fun itemGamblinImage():Bitmap{
//        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.base_circle)
//        var bitmapSize: Int = (size.x*0.7).toInt()
//        return  Bitmap.createScaledBitmap(bitmap,bitmapSize, bitmapSize, true)
//
//    }
//
//    override fun run() {
//        Log.d("GAME STATE", "RUNNING $isPlaying")
//        while (isPlaying){
//            update()
//            draw()
//            sleep()
//        }
//    }
//
//
//    private fun resumeGame(){
//        Log.d("GAME STATE", "RUNNING RESUMED")
//        isPlaying = true
//        thread = Thread(this)
//        thread.start()
//
//    }
//    private fun pauseGame(){
//
//        try {
//            Log.d("GAME STATE", "RUNNING PAUSED")
//            isPlaying = false
//            thread.join()
//        } catch (e:InterruptedException){
//            Log.d("thread error", e.toString())
//        }
//
//    }
//
//    private fun update(){
//        Log.d("GAME STATE", "RUNNING UPDATE")
//
//    }
//
//    private fun draw(){
//        Log.d("GAME STATE", "RUNNING DRAW")
//        if (holder.surface.isValid) {
//            val canvas = holder.lockCanvas()
//           canvas.drawColor(Color.CYAN)
//           canvas.drawBitmap(animTest() ?: return,-(circleX * 0.5).toFloat(),(circleY * 0.5).toFloat(),paint)
//           //canvas.drawRoundRect(rectGame,circleX,circleY,paint)
//
//            holder.unlockCanvasAndPost(canvas)
//        }
//
//    }
//
//    private fun sleep(){
//        Log.d("GAME STATE", "RUNNING SLEEP")
//
//    }
//
//
//    fun animTest(): Bitmap?{
//        val matrix = Matrix()
//
//        matrix.postRotate(circleRadius)
//
//        val scaledBitmap = Bitmap.createScaledBitmap(itemGamblinImage(), itemGamblinImage().width,
//            itemGamblinImage().height, true)
//
//        val rotatedBitmap = Bitmap.createBitmap(
//            scaledBitmap,
//            0,
//            0,
//            scaledBitmap?.width ?: 0,
//            scaledBitmap?.height ?: 0,
//            matrix,
//            true
//        )
//        circleRadius += 22
//         return  rotatedBitmap
//    }
//
//}