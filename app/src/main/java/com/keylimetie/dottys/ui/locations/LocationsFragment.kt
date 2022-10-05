package com.keylimetie.dottys.ui.locations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.CrashlyticsRegistrar
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.ui.dashboard.DashboardViewModel


class LocationsFragment : Fragment(), DottysLocationDelegates {

    private lateinit var locationViewModel: LocationsViewModel
    var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        //locationViewModel =
        //     ViewModelProviders.of(this).get(LocationsViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_locations, container, false)
        rootView = root
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity
        locationViewModel = activity?.let { LocationsViewModel(it) } ?: return root
        activity.let { locationViewModel.initLocationView(this, activity, root) }
        locationViewModel.screenDimensionManager(LocationViewType.MAP_FULL)
// = activity.getUserNearsLocations().locations?.filter { it.storeType.name }
//TODO
//        for (location in  activity.getUserNearsLocations().locations ?: return root ){
//            if (!stores.contains(location.storeType?.value)) {
//                location.storeType?.value?.let { stores.add(it) }
//            }
//        }

        return root
    }


    override fun onResume() {
        super.onResume()
        (activity as DottysMainNavigationActivity).mapFragmentBase = locationViewModel.fragmentMap
        locationViewModel.fragmentMap.updateMarker()


    }

    override fun onStop() {
        super.onStop()
        (activity as DottysMainNavigationActivity).mapFragmentBase = null
       // locationViewModel.locationDataObserver = null
    }

    override fun getStoresLocation(locations: DottysLocationsStoresModel) {
        val mContext = activity as DottysMainNavigationActivity
        locations.locations?.let {
                locationViewModel.initMapWHitMarker(it)
        }
        locationViewModel.screenDimensionManager(LocationViewType.COLLAPSE_TYPE)

        locations.locations?.let {
            locationViewModel.initExpandableList(mContext,
                it)
        }
        if (locations.locations.isNullOrEmpty()){
            locationViewModel.screenDimensionManager(LocationViewType.MAP_FULL)
      //    (mContext).showSnackBarMessage(mContext,"You have no stores near you at this time, please come  back later.")
        }
        try {
            DashboardViewModel(mContext).getBeaconList(
                mContext,
                locations.locations?.first()?.storeNumber.toString(),
                null
            )
        } catch (e:Exception){
            CrashlyticsReport.Session.Event.Log.builder().setContent("ERROR MAP FRAGMENT" + "${e.message}")
        }
    }


    override fun allItemsCollapse(isColappse: Boolean) {
        if (isColappse) {
            this.locationViewModel.screenDimensionManager(LocationViewType.COLLAPSE_TYPE)
        } else {
            this.locationViewModel.screenDimensionManager(LocationViewType.EXPANDED_TYPE)
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//            if (requestCode == 42) {
//                // If request is cancelled, the result arrays are empty.
//                if (resultCode == Activity.RESULT_OK) {
//                    // permission was granted, yay!
//                    callPhone()
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality
//                }
//                return
//            }
//
//    }
//
//    @SuppressLint("MissingPermission")
//    fun callPhone(){
//        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "1122334455"))
//       startActivity(intent)
//    }
}

//class DottysStoreListAdapter


fun DottysBaseActivity.showSnackBarMessage(activity: DottysBaseActivity, msg: String){
   // hideCustomKeyboard(activity)
    val container = activity.findViewById<View>(android.R.id.content)
    if (container != null) {
        Snackbar.make(container, msg, Snackbar.LENGTH_LONG).show()
    }

}