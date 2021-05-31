package com.keylimetie.dottys.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.keylimetie.dottys.R
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon

class AnalyticBeacoonsAdapter(
    private val mContext: Context,
    val list: ArrayList<DottysBeacon>,
) : BaseAdapter() {

    private var idBeacon: TextView? = null
    private var typeBeacon: TextView? = null
    private var statusColorBeacon: ImageView? = null


    private val inflater: LayoutInflater =
        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    //1
    override fun getCount(): Int {
        return list.size
    }

    //2
    override fun getItem(position: Int): Any {
        return list[position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //4
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.beacon_analytics_item, parent, false)
        val params = rowView.layoutParams

        val rewards = list[position]
        idBeacon = rowView.findViewById<TextView>(R.id.id_beacon_label)
        statusColorBeacon =
            rowView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.status_beacon_image)
        typeBeacon = rowView.findViewById<TextView>(R.id.type_beacon_label)

        fillItemsInView(rewards)
        return rowView
    }


    private fun fillItemsInView(beacon: DottysBeacon) {
        idBeacon?.text = beacon.beaconIdentifier
        typeBeacon?.text =
            "${beacon.beaconType?.value} | Major: ${beacon.major.toString()} - Minor:${beacon.minor.toString()}"
        var conectionStatusColor: Drawable? = when (beacon.isConected) {
            true -> {
                mContext.resources.getDrawable(R.drawable.shape_circular_conected)
            }
            else -> {
                mContext.resources.getDrawable(R.drawable.shape_circular_disconected)

            }
        }
        statusColorBeacon?.background = conectionStatusColor
    }


}
