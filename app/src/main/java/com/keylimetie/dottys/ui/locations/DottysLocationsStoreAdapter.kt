package com.keylimetie.dottys.ui.locations

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.keylimetie.dottys.R
import kotlin.properties.Delegates

enum class LocationViewType {
    SEARCH_TYPE, EXPANDED_TYPE, COLLAPSE_TYPE
}

class DottysLocationsStoreAdapter(
    private val activity: Context,
    private val dataSource: ArrayList<DottysStoresLocation>,
    val mapFragment: DottysLocationsMapFragment
) : BaseExpandableListAdapter() {
    //  private val inflater: LayoutInflater
    // = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var locationObserver: DottysStoreListObserver? = null
    var parent_textvew: TextView? = null
    override fun getGroupCount(): Int {
        return dataSource.size//childLists.size
    }

    override fun getChildrenCount(parent: Int): Int {
        return 1// parents.size
    }

    override fun getGroup(parent: Int): Any {

        return dataSource[parent]
    }

    override fun getChild(parent: Int, child: Int): Any {
        return dataSource[parent]
    }

    override fun getGroupId(parent: Int): Long {
        return parent.toLong()
    }

    override fun getChildId(parent: Int, child: Int): Long {
        return child.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        parent: Int,
        isExpanded: Boolean,
        convertView: View?,
        parentview: ViewGroup
    ): View {
        var convertView = convertView

        if (convertView == null) {
            val inflater =
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.locations_stores_item, parentview, false)

        }

        parent_textvew = convertView!!.findViewById(R.id.location_store_textview) as TextView
        val distanceTextView = convertView.findViewById(R.id.distance_textview) as TextView
        parent_textvew?.text =
            dataSource[parent].address1 + "\n" + dataSource[parent].city + ", " + dataSource[parent].state + " " + dataSource[parent].zip
        distanceTextView.text = dataSource[parent].distance.toString() + " mil"
        return convertView
    }


    override fun getChildView(
        parent: Int,
        child: Int,
        isLastChild: Boolean,
        convertView: View?,
        parentview: ViewGroup
    ): View {
        var convertView = convertView

        if (convertView == null) {
            val inflater =
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.locations_stores_child_item, parentview, false)


        }
        var paramsView = convertView?.layoutParams

        convertView?.layoutParams = paramsView
        val child_textvew = convertView!!.findViewById(R.id.child_textview) as TextView
        child_textvew.text =
            dataSource[parent].hours?.let { getDataHours(it.filter { it1 -> it1 != "" }) }
        paramsView?.height =
            (dataSource[parent].hours?.filter { it != "" }?.size?.times(50) ?: 0) + 50
        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }

    fun getDataHours(hours: List<String>): String {
        var hoursString = StringBuilder()
        for (hsItem in hours.indices) {
            if (hsItem == hours.size - 1 && hours[hsItem] != "") {
                hoursString.append(hours[hsItem])
            } else if (hours[hsItem] != "") {
                hoursString.append(hours[hsItem] + "\n")
            }

        }
        return hoursString.toString()
    }


    /*
     * DELEGATES
     */

    override fun onGroupExpanded(groupPosition: Int) {
        super.onGroupExpanded(groupPosition)
        print("GROUP EXPAMDEDE ->" + groupPosition)
        locationObserver = DottysStoreListObserver(mapFragment)
        locationObserver?.hasSelected = dataSource[groupPosition]
    }

    override fun onGroupCollapsed(groupPosition: Int) {

        print("GROUP COLAPSED ->" + groupPosition)

    }

}

interface DottysStoreListDelegates {
    fun onItemSelected(location: DottysStoresLocation)

}

class DottysStoreListObserver(lisener: DottysStoreListDelegates) {
    var hasSelected: DottysStoresLocation by Delegates.observable(
        initialValue = DottysStoresLocation(),
        onChange = { prop, old, new -> lisener.onItemSelected(new) })
}