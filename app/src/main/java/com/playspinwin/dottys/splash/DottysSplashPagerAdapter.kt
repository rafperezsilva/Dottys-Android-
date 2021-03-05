package com.playspinwin.dottys.splash


  import android.content.Context
  import android.util.DisplayMetrics
  import android.view.LayoutInflater
  import android.view.View
  import android.view.ViewGroup
  import android.widget.*
  import androidx.viewpager.widget.PagerAdapter
  import com.playspinwin.dottys.R
  import kotlin.math.roundToInt


class CustomPagerAdapter(
    private val mContext: Context,
    val list: ArrayList<DottysPagerModel>,
    var metrics: DisplayMetrics? = null
) : PagerAdapter() {


    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val modelObject = list[position]
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(R.layout.splash_pager_item, collection, false) as ViewGroup
        val image = layout.findViewById<ImageView>(R.id.image_pager)
        val imageTop = layout.findViewById<ImageView>(R.id.image_top_pager)
        val title = layout.findViewById<TextView>(R.id.title_pager)
        val subtitle = layout.findViewById<TextView>(R.id.subtitle_pager)
        val staticLabelSwipe: TextView = layout.findViewById<TextView>(R.id.splash_swipe_static_textview)
        var layoutParams = TableRow.LayoutParams(metrics!!.widthPixels, metrics!!.heightPixels / 2)
        staticLabelSwipe.alpha = 0f
        image.layoutParams = layoutParams
        var staticLabelParams = LinearLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        when (position){
            0 -> {
                title.height = 0
                layoutParams = TableRow.LayoutParams((metrics!!.widthPixels * 0.85).roundToInt(),
                    (metrics!!.heightPixels * 0.85).roundToInt())
                layoutParams.setMargins(25,
                    -(metrics!!.heightPixels * 0.2).roundToInt(),
                    25,
                    -(metrics!!.heightPixels * 0.2).roundToInt())
                 image.layoutParams = layoutParams
                staticLabelParams.setMargins(25,  55, 25, 0)
               staticLabelSwipe.layoutParams = staticLabelParams
                staticLabelSwipe.alpha = 1f
            }
            1, 2, 3 -> {
                val param = title.layoutParams as LinearLayout.LayoutParams
                param.setMargins(25, 80, 25, 130)
                title.layoutParams = param
                imageTop.layoutParams.height = 0
                when (position) {

                    1, 2 -> {
                        param.setMargins(25, 80, 25, 50)
                        title.layoutParams = param
                    }
                    3 -> {
                        param.setMargins(25, 80, 25, 75)
                        title.layoutParams = param
                        imageTop.layoutParams.height = 0
                        layoutParams =
                            TableRow.LayoutParams((metrics!!.widthPixels * 0.8).roundToInt(),
                                metrics!!.heightPixels / 2)
                        layoutParams.topMargin = 0
                        image.layoutParams = layoutParams

                    }
                }
            }
            else -> {
                imageTop.layoutParams.height = 0
            }
        }

        image.setImageResource(modelObject.image)
        title.text = list[position].title
        subtitle.text = list[position].subtitle
        collection.addView(layout)
        return layout
    }
    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }
    override fun getCount(): Int {
        return list.count()
    }
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

}