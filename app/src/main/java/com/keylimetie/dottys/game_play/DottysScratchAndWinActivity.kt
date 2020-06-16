package com.keylimetie.dottys.game_play

  import android.content.DialogInterface
  import android.graphics.drawable.ColorDrawable
  import android.os.Bundle
  import android.os.Handler
  import android.view.View
  import android.view.WindowManager
  import android.widget.ImageView
  import android.widget.TextView
  import android.widget.Toast
  import com.clock.scratch.ScratchView
  import com.keylimetie.dottys.DottysBaseActivity
  import com.keylimetie.dottys.R


class DottysScratchAndWinActivity : DottysBaseActivity(), ScratchView.EraseStatusListener {
    var scratchImageView: ScratchView? = null
    val imageArray = ArrayList<ImageView>()
    var imageAtGameArray = ArrayList<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_scratch_and_win)
        this.supportActionBar?.let {actionBarSetting(it, ColorDrawable(resources.getColor(R.color.colorPrimary)))}
        val titleBar = actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = "Scratch Game"
        initGamblingImages()
        //restoreGame()
    }

    fun initScratchView(){
        scratchImageView = null
        scratchImageView = findViewById<ScratchView>(R.id.scratch_view)
        scratchImageView?.setEraserSize(125.0f)
        scratchImageView?.setMaxPercent(70)
        scratchImageView?.setWatermark(R.mipmap.dottys_splash_first_image)
        scratchImageView?.setEraseStatusListener(this)
    }

    fun initGamblingImages(){
        val image1 = findViewById<ImageView>(R.id.scratch_image_01)
        val image2 = findViewById<ImageView>(R.id.scratch_image_02)
        val image3 = findViewById<ImageView>(R.id.scratch_image_03)
        val image4 = findViewById<ImageView>(R.id.scratch_image_04)
        val image5 = findViewById<ImageView>(R.id.scratch_image_05)
        val image6 = findViewById<ImageView>(R.id.scratch_image_06)
        val image7 = findViewById<ImageView>(R.id.scratch_image_07)
        val image8 = findViewById<ImageView>(R.id.scratch_image_08)
        val image9 = findViewById<ImageView>(R.id.scratch_image_09)

        imageArray.add(image1)
        imageArray.add(image2)
        imageArray.add(image3)
        imageArray.add(image4)
        imageArray.add(image5)
        imageArray.add(image6)
        imageArray.add(image7)
        imageArray.add(image8)
        imageArray.add(image9)
        restoreGame()

    }

    private fun restoreGame(){
        initScratchView()

        imageAtGameArray.clear()
        imageAtGameArray = imagesAtGame()
        fillImages(imageAtGameArray)
    }
   private fun fillImages(imageAtGame: ArrayList<Int>){

        for ((index,image) in imageArray.withIndex()) {
            image.cropToPadding = true
            image.adjustViewBounds = true
             image.setImageResource(imageAtGame[index])
        }

    }

    private fun imagesAtGame():  ArrayList<Int>{
        val imagesAtGame: ArrayList<Int> = arrayListOf(R.mipmap.coffee_win,R.mipmap.sandwich_win,R.mipmap.no_win)
        var imagesForGame = ArrayList<Int>()
        imagesForGame.clear()
        for(image in imageArray){
            if (imagesForGame.filter { it == R.mipmap.coffee_win}.size >= 3 ||
                imagesForGame.filter { it == R.mipmap.sandwich_win}.size >= 3) {
                imagesForGame.add(R.mipmap.no_win)
            } else {
                imagesForGame.add(imagesAtGame[(0 until imagesAtGame.size).random()])
            }
        }


        return imagesForGame
    }

    fun getWinner():String?{
        if(imageAtGameArray.filter { it == R.mipmap.coffee_win }.size == 3){
            Toast.makeText(this,"COFFEE WINNER",Toast.LENGTH_LONG).show()
            "COFFEE WINNER"
        } else if (imageAtGameArray.filter { it == R.mipmap.sandwich_win }.size == 3){
            Toast.makeText(this,"SANDWICH WINNER",Toast.LENGTH_LONG).show()
            "SANDWICH WINNER"
        }else {
            Toast.makeText(this,"LOSER",Toast.LENGTH_LONG).show()
            "LOSER"
        }
        return  null
    }

    override fun onProgress(percent: Int) {
           if (percent >= 70){
               if (percent > 71) {return}


               scratchImageView?.clear()
//               scratchImageView?.reset()
//               val builder1: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
//               builder1.setMessage(getWinner())
//               builder1.setCancelable(true)
//
//               builder1.setPositiveButton(
//                   "Try Again",
//                   DialogInterface.OnClickListener { dialog, id ->
//                       dialog.cancel()
//
//                   })
//               builder1.setNegativeButton(
//                   "Exit"
//               ) { dialog, id ->
//                   dialog.cancel()
//               }
//               builder1.create()?.show()
           }
    }

    override fun onCompleted(view: View?) {

        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val viewS = view as ScratchView

        getWinner()
        Handler().postDelayed({
            viewS.reset()

            initScratchView()
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            restoreGame()
        }, 3000)
    }
}
