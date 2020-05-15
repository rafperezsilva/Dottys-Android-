package com.keylimetie.dottys.ui.dashboard

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.Monthly


class DashboardPagerAdapter(
    private val mContext: Context,
    val list: DottysGlobalDataModel) : PagerAdapter() {


    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        var modelObject: Monthly?
        when (position) {
            0 -> {
                modelObject = list.drawingTemplates?.weekly
            }
            1 -> {
                modelObject = list.drawingTemplates?.monthly
            }
            else -> {
                modelObject = list.drawingTemplates?.quarterly
            }

        }

        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(R.layout.dashboard_pager_item, collection, false) as ViewGroup
        val image = layout.findViewById<ImageView>(R.id.image_dashboard_pager)
        val title = layout.findViewById<TextView>(R.id.title_dashboard_pager)
        val subtitle = layout.findViewById<TextView>(R.id.subtitle_dashboard_pager)


        title.text = modelObject?.title
        subtitle.text = "Redeem " + modelObject?.priceInPoints.toString() + " Points for " +
                modelObject?.quantity + " Entries to Win!"
        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return 3
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}