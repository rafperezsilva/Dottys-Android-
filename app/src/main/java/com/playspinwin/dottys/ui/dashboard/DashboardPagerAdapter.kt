package com.playspinwin.dottys.ui.dashboard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.playspinwin.dottys.DottysMainNavigationActivity
import com.playspinwin.dottys.R
import com.playspinwin.dottys.models.Monthly
import kotlin.properties.Delegates


class DashboardPagerAdapter(
    private val mContext: Context,
    val list: List<Monthly?>?,
) : PagerAdapter() {

    var currentPages = 0

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        var modelObject: Monthly?
        val pagerObserver = DottysPagerObserver(mContext as DottysMainNavigationActivity)
//        when (position) {
//            0 -> {
        modelObject = list?.get(position)
//            }
//            1 -> {
//                modelObject = list.drawingTemplates?.monthly
//            }
//            else -> {
//                modelObject = list.drawingTemplates?.quarterly
//            }

        //  }

        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(R.layout.dashboard_pager_item, collection, false) as ViewGroup
        val image = layout.findViewById<ImageView>(R.id.image_dashboard_pager)
        val title = layout.findViewById<TextView>(R.id.title_dashboard_pager)
        val subtitle = layout.findViewById<TextView>(R.id.subtitle_dashboard_pager)

        layout.setOnClickListener {
            Log.e("TAG", position.toString())
            pagerObserver.itemSelected = position
        }
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

    fun getCurrentPage(): Int {
        return currentPages
    }


}

interface DottysPagerDelegates {
    fun getDrawingSelected(position: Int)

}

class DottysPagerObserver(lisener: DottysPagerDelegates) {
    // val itemSelected = 0
    var itemSelected: Int by Delegates.observable(
        initialValue = -1,
        onChange = { prop, old, new -> lisener.getDrawingSelected(new) })

}