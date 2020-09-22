package com.keylimetie.dottys.splash


  import android.content.Context
  import android.util.DisplayMetrics
  import android.view.LayoutInflater
  import android.view.View
  import android.view.ViewGroup
  import android.widget.ImageView
  import android.widget.LinearLayout
  import android.widget.TableRow
  import android.widget.TextView
   import androidx.viewpager.widget.PagerAdapter
  import com.keylimetie.dottys.R
  import kotlin.math.roundToInt

class CustomPagerAdapter(private val mContext: Context, val list: ArrayList<DottysPagerModel>, var metrics: DisplayMetrics? = null) : PagerAdapter() {


    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val modelObject = list[position]
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(R.layout.splash_pager_item, collection, false) as ViewGroup
        val image = layout.findViewById<ImageView>(R.id.image_pager)
        val imageTop = layout.findViewById<ImageView>(R.id.image_top_pager)
        val title = layout.findViewById<TextView>(R.id.title_pager)
        val subtitle = layout.findViewById<TextView>(R.id.subtitle_pager)
        var layoutParams = TableRow.LayoutParams(metrics!!.widthPixels, metrics!!.heightPixels / 2)
        image.layoutParams = layoutParams

        when (position){
            0 -> {
                title.height = 0
            }
            1,2,3 -> {
                val param = title.layoutParams as LinearLayout.LayoutParams
                param.setMargins(25,80,25,130)
                title.layoutParams = param
                imageTop.layoutParams.height = 0
                when (position){
                    1,2 -> {
                        param.setMargins(25, 80, 25, 50)
                        title.layoutParams = param
                    }
                    3 -> {
                        param.setMargins(25,80,25,75)
                        title.layoutParams = param
                        imageTop.layoutParams.height = 0
                        layoutParams = TableRow.LayoutParams((metrics!!.widthPixels * 0.8).roundToInt(), metrics!!.heightPixels / 2)
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