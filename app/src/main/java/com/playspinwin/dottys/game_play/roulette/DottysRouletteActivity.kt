package com.playspinwin.dottys.game_play.roulette

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.playspinwin.dottys.DottysBaseActivity
import com.playspinwin.dottys.R


class DottysRouletteActivity : DottysBaseActivity(), View.OnClickListener {
    /** Called when the activity is first created.  */

     var gameView: GameView?  = null
    var endGameButton   : Button? = null
    var startGameButton : Button? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
      ///  setContentView(GameView(this))

        val game = FrameLayout(this)

        val gameWidgets = LinearLayout(this)

         val image   = ImageView(this)
        image.setImageResource(R.mipmap.sandwich_win)
         endGameButton   = Button(this)
         startGameButton = Button(this)
       // val myText = TextView(this)

      //  val params  = gameWidgets.layoutParams
       // params.width = displayMetrics.widthPixels
        gameWidgets.setOrientation(LinearLayout.VERTICAL);

        endGameButton?.setText("Pause")
        startGameButton?.setText("Start")
        //myText.text = "rIZ..i"

        //gameWidgets.addView(myText)
        gameWidgets.addView(endGameButton)
        gameWidgets.addView(startGameButton)
        gameWidgets.addView(image)
        game.setBackgroundResource(R.color.black)
        gameView = GameView(this, image)
        game.addView(gameView)
        game.addView(gameWidgets)
        setContentView(game)
        endGameButton?.id = 998
        startGameButton?.id = 999
        endGameButton?.setOnClickListener(this)
        startGameButton?.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        Log.d("VIEW ON LICK", v?.id.toString())
        when(v?.id){
            startGameButton?.id -> {
                gameView?.resumeGame()
            }
            endGameButton?.id -> {
                gameView?.isPlaying = false
            }
        }

    }
}

/*

class GameView(context: Context?) : SurfaceView(context) {
    private val bmp: Bitmap
    private val holder: SurfaceHolder
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(bmp, 10, 10, null)
    }

    init {
        holder = getHolder()
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) {}
            override fun surfaceCreated(holder: SurfaceHolder) {
                val c: Canvas = holder.lockCanvas(null)
                onDraw(c)
                holder.unlockCanvasAndPost(c)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int,
                width: Int, height: Int
            ) {
            }
        })
        bmp = BitmapFactory.decodeResource(resources, R.drawable.icon)
    }
}

*/
    /*
class DottysRouletteActivity:
AppCompatActivity(), View.OnTouchListener  {
    private var itemRoulette0: ImageView? = null
    private var itemRoulette1: ImageView? = null
    private var itemRoulette2: ImageView? = null
    private var itemRoulette3: ImageView? = null
    private var itemRoulette4: ImageView? = null
    private var itemRoulette5: ImageView? = null
    private var itemRoulette6: ImageView? = null
    private var itemRoulette7: ImageView? = null
    var mainHandler: Handler? = null
    private val RANDOM = Random()
    var arrayRouletteItems:  ArrayList<ImageView>? = null

    private var isPlaying: Boolean = false
    var selectorLayout: RelativeLayout? = null


    private var rouletteContainer: ConstraintLayout? = null
    private var markerImage: ImageView? = null
    private  var timeUpdate:Long = 20
    private var markerPosition = 0
    private var degreeOld: Int = 0
    private var degree: Int = 0
    private var rollCounter = 0


    private val updateTask = object : Runnable {
        override fun run() {
           minusOneSecond()
            // markerChekcer()
            mainHandler?.postDelayed(this, timeUpdate)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_dottys_roulette)
          initViewSetting()
        selectorLayout = RelativeLayout(this)

     }

    fun markerChekcer(){
        for (img in arrayRouletteItems ?: return) {
            val rectB = Rect()
            val rectA = Rect()
            markerImage?.getLocalVisibleRect(rectB)

            img?.getLocalVisibleRect(rectA)
            if(rectA.intersect(rectB)){
                img.scaleX = 2.0f
                img.scaleY = 2.0f
            } else {
                img.scaleX = 1f
                img.scaleY = 1f
            }
        }
    }


    fun minusOneSecond() {

        for (img in arrayRouletteItems ?: return){
            img.alpha = 1f
            rouletteContainer?.removeView(selectorLayout)


        }

        if (markerPosition >= (arrayRouletteItems?.count() ?: 0) - 1) {
            markerPosition = 0
            rollCounter += 1
        } else {
            markerPosition += 1
        }
        Log.d("SECOND", markerPosition.toString())
        //arrayRouletteItems?.get(markerPosition)?.alpha = 0f
        arrayRouletteItems?.get(markerPosition)?.z = 0.2f
        if (rollCounter%2 == 0) {
            if(timeUpdate > 100) {
                timeUpdate += 20
            }else {
                timeUpdate += 5
            }

        }
        if (timeUpdate >= 300){
            pauseGame()
        }



        selectorLayout?.layoutParams =  arrayRouletteItems?.get(markerPosition)?.layoutParams
        selectorLayout?.setBackgroundResource(R.drawable.base_circle)
        selectorLayout?.z = 0.1f
        selectorLayout?.alpha = 0.5f
        rouletteContainer?.addView(selectorLayout)



    }




    fun initViewSetting(){
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        rouletteContainer= findViewById<ConstraintLayout>(R.id.roulette_container) as ConstraintLayout
        markerImage = findViewById<ImageView>(R.id.marker_image) as ImageView
        markerImage?.z = 0.2f
        rouletteContainer?.setOnTouchListener(this)
//        var params = rouletteContainer?.layoutParams
//        params?.width = (size.x * 0.7).roundToInt()
//        params?.height = (size.x * 0.7).roundToInt()
       // rouletteContainer?.layoutParams =  params
        addItemsAtRoulette()
    }


    fun addItemsAtRoulette(){
//        roueletteContainer = findViewById<ConstraintLayout>(R.id.roulette_container)
//        containerMain = findViewById<ConstraintLayout>(R.id.parent_roulette_view)

        itemRoulette0 = findViewById<ImageView>(R.id.itemRoulette0)
        itemRoulette1 = findViewById<ImageView>(R.id.itemRoulette1)
        itemRoulette2 = findViewById<ImageView>(R.id.itemRoulette2)
        itemRoulette3 = findViewById<ImageView>(R.id.itemRoulette3)
        itemRoulette4 = findViewById<ImageView>(R.id.itemRoulette4)
        itemRoulette5 = findViewById<ImageView>(R.id.itemRoulette5)
        itemRoulette6 = findViewById<ImageView>(R.id.itemRoulette6)
        itemRoulette7 = findViewById<ImageView>(R.id.itemRoulette7)

          arrayRouletteItems  = arrayListOf( itemRoulette0!!,
                                             itemRoulette1!!,
                                             itemRoulette2!!,
                                             itemRoulette3!!,
                                             itemRoulette4!!,
                                             itemRoulette5!!,
                                             itemRoulette6!!,
                                             itemRoulette7!!)

        getItemInRoulette()



    }

    fun  getItemInRoulette() {
        val drawblesGambling = arrayListOf(R.drawable.no_win_rulette,R.drawable.coffee_rulette,R.drawable.sandwich_rulette,R.drawable.soda_win)
        val imagasAtGambling = ArrayList<Int>()

      // var rectArray = ArrayList<Rect>()
        for (image in arrayRouletteItems ?: return) {

            var ram = Random().nextInt(drawblesGambling.size)
//            while (imagasAtGambling.filter { it == imagasAtGambling[ram]}.count() <= 3) {
//
//                imagasAtGambling.add(drawblesGambling[ram])
//
//            }
            image.setImageDrawable(resources.getDrawable(drawblesGambling[ram]))
            image.scaleX = 1f
            image.scaleY = 1f
        }
       // return rectArray
    }




    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.run {
            when (action){
                MotionEvent.ACTION_MOVE -> {
                    Log.d("MOTION", "Motion MOVE in $x | $y")
                }
                MotionEvent.ACTION_DOWN -> {
                    Log.d("MOTION", "Motion DOWN  in $x | $y")
                    if (!isPlaying) {
                        resumeGame()
                    }
                    false
//                    else {
//                        pauseGame()
//
//                    }
                   // spin()
//                    mainHandler = Handler(Looper.getMainLooper())
//                    mainHandler?.post(updateTextTask)
                }
                else -> {false}
            }
        }
        return false
    }





    private fun resumeGame(){
        Log.d("GAME STATE", "RUNNING RESUMED")
        isPlaying = true
        timeUpdate = 0
        mainHandler = Handler(Looper.getMainLooper())
        mainHandler?.post(updateTask)
         getItemInRoulette()
     //   rouletteContainer?.startAnimation(spin())


    }
    private fun pauseGame(){

        try {
            Log.d("GAME STATE", "RUNNING PAUSED")
            isPlaying = false
            mainHandler?.removeCallbacks(updateTask)
            mainHandler = null
            timeUpdate = 0



            for (img in arrayRouletteItems ?: ArrayList()){
                val rectf = Rect()
                val rectA = Rect()
                selectorLayout?.getGlobalVisibleRect(rectf)
                img.getGlobalVisibleRect(rectA)
                 if (rectf.intersect(rectA)){
                     img.scaleX = 5f
                     img.scaleY = 5f
                 //    img.alpha = 1f
                 }
//                if (img.alpha == 0f){
//                    img.scaleX = 5f
//                    img.scaleY = 5f
//                    img.alpha = 1f
//                    rouletteContainer?.removeView(selectorLayout)
//                }
            }


        } catch (e:InterruptedException){
            Log.d("thread error", e.toString())
        }

    }
    fun spin(): RotateAnimation {
        degreeOld = (degree % 360)
        // we calculate random angle for rotation of our wheel
        degree = RANDOM.nextInt(360) + 720
        // rotation effect on the center of the wheel
        val rotateAnim = RotateAnimation(
            degreeOld.toFloat(), degree.toFloat(),
            RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnim.duration = 5600
        rotateAnim.fillAfter = true
        rotateAnim.interpolator = DecelerateInterpolator()
        rotateAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                // we empty the result text view when the animation start
                Log.d("ANIMATION", "STRAT")
            }

            override fun onAnimationEnd(animation: Animation) {
                // we display the correct sector pointed by the triangle at the end of the rotate animation
                Log.d("ANIMATION", "END")
                pauseGame()
                // val marker: Rect? = markerImage.let {  Rect(it?.left ?: 0,it?.top ?: 0,it?.right ?: 0,it?.bottom ?: 0)
                //  }
                // for ((index,imageBound) in getItemInRoulette().withIndex()) if (marker!!.intersect(convertRect(imageBound,containerMain?.rootView!!,markerImage?.rootView!!))){
                //    arrayRouletteItems?.get(index)?.alpha  = 0.0f
                // }
            }

            override fun onAnimationRepeat(animation: Animation) {
                Log.d("ANIMATION", "REPEAT")
            }
        })

        // we start the animation
        // contentRoulette?.startAnimation(rotateAnim)
        return rotateAnim
    }
}
*/