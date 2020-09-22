package com.keylimetie.dottys.ui.locations

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
 import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.GpsTracker

import com.keylimetie.dottys.R
import kotlin.properties.Delegates


enum class LocationViewType {
    SEARCH_TYPE, EXPANDED_TYPE, COLLAPSE_TYPE
}

class DottysLocationsStoreAdapter(
    private val activity: Context,
    private val dataSource: ArrayList<DottysStoresLocation>,
    private val mapFragment: DottysLocationsMapFragment
) : BaseExpandableListAdapter(), View.OnClickListener {
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
        val childTextvew = convertView!!.findViewById(R.id.child_textview) as TextView
        val getDirectionsButton = convertView!!.findViewById(R.id.get_directions_button) as Button
        val callLocationButton = convertView!!.findViewById(R.id.call_location_button) as Button
        getDirectionsButton.setOnClickListener(this)
        callLocationButton.setOnClickListener(this)
        childTextvew.text =
            dataSource[parent].hours?.let { getDataHours(it.filter { it1 -> it1 != "" }) }
        paramsView?.height =
            (dataSource[parent].hours?.filter { it != "" }?.size?.times(50) ?: 0) + 250
        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }

    private fun getDataHours(hours: List<String>): String {
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

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.get_directions_button -> {
                openMap()
            }
            R.id.call_location_button -> {
                checkPermission()
            }
        }
    }

    private fun checkPermission() {

        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity as Activity,
                    Manifest.permission.CALL_PHONE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    42)
            }
        } else {
            // Permission has already been granted
            callPhone()
        }
    }






    @SuppressLint("MissingPermission")
    fun callPhone(){
        val phone = locationObserver?.hasSelected?.phone ?: ""
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))
        activity.startActivity(intent)
    }

    fun openMap(){
        if (locationObserver?.hasSelected?.latitude.toString().isNotEmpty()){
         val lat =  locationObserver?.hasSelected?.latitude.toString()
         val long =  locationObserver?.hasSelected?.longitude.toString()
        val currentGps = GpsTracker(activity as DottysMainNavigationActivity)
        val uri =
            "http://maps.google.com/maps?saddr=${currentGps.getLatitude()},${currentGps.getLongitude()}&daddr=$lat,$long"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        activity.startActivity(intent)
        }
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