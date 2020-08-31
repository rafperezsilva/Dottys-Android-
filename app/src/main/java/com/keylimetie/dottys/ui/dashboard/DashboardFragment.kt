package com.keylimetie.dottys.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.*
import com.keylimetie.dottys.beacon_service.DottysBeaconActivityDelegate
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.redeem.DottysRedeemRewardsActivity
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconsModel
import com.keylimetie.dottys.ui.dashboard.models.DottysDrawingSumaryModel
import com.keylimetie.dottys.ui.drawing.*
import com.keylimetie.dottys.ui.locations.DottysLocationDelegates
import com.keylimetie.dottys.ui.locations.DottysLocationStoresObserver
import com.keylimetie.dottys.ui.locations.DottysLocationsStoresModel
import com.keylimetie.dottys.ui.locations.LocationsViewModel


class DashboardFragment : Fragment(), DottysDashboardDelegates, DottysDrawingDelegates,
    DottysLocationDelegates, DottysBeaconActivityDelegate  {

    private var homeViewModel = DashboardViewModel()
    private var viewFragment: View? = null
    //var serviceBeacon = DottysBeaconService()
   // var gsp: GpsTracker? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        viewFragment = root

        return root
    }

    override fun onResume() {
        super.onResume()
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.gpsTracker =
            this.context?.let { GpsTracker(it as DottysMainNavigationActivity) }!! /*GPS TRACKER*/

        activity?.requestLocation( activity?.gpsTracker, activity)
        activity?.gpsTracker?.locationGps?.let { activity?.gpsTracker?.onLocationChanged(it) }
        if (activity?.getUserPreference() != null || activity?.progressBar?.visibility != 0) {
            activity?.initEstimoteBeaconManager(this)
        }


    }

    override fun onStart() {
        super.onStart()
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        if (activity != null) {
            homeViewModel.initDashboardViewSetting(this, activity, viewFragment)
        }
        activity?.hideCustomKeyboard()

    }
    /*0*/
    override fun getCurrentUser(currentUser: DottysLoginResponseModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        var locationModel = LocationsViewModel()
        locationModel.locationDataObserver = DottysLocationStoresObserver(this)
        val location = this.activity?.let {
            activity?.gpsTracker?.let { it1 ->
                activity.getLocation(
                    it1
                )
            }
        }
        if(location != null){
            activity?.let { locationModel.getLocationsDottysRequest(it,location?.latitude.toString(), location?.longitude.toString(),this) }
        }
    }

    /*1*/
    override fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.let { homeViewModel.getGlobalDataRequest(it) }
    }

    /*2*/
    override fun getStoresLocation(locations: DottysLocationsStoresModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity?.saveDataPreference(PreferenceTypeKey.LOCATIONS,locations.toJson())
        activity?.let { homeViewModel.getBeaconList(it) }
    }

    /*3*/// -- /*02*/
    override fun getGlobalData(gloabalData: DottysGlobalDataModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity?.saveDataPreference(PreferenceTypeKey.GLOBAL_DATA,gloabalData.toJson().toString())
        activity?.let { homeViewModel.initDashboardPager(it, gloabalData) }
    }

    /*4*/
    override fun getBeaconList(beaconList: DottysBeaconsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity?.saveDataPreference(PreferenceTypeKey.BEACON_AT_LOCATION,beaconList.toJson().toString())
    }

    /*5*/// -- /*03*/
    override fun getUserRewards(rewards: DottysRewardsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        homeViewModel.drawingBadgeCounter = rewards.rewards?.filter { it.redeemed == false }?.size ?: 0
        homeViewModel.badgeCounterDrawingManager(homeViewModel.drawingBadgeCounter ?: 0)
        viewFragment?.let { activity?.let { it1 -> homeViewModel.addProfileImage(it1, it, this) } }
        homeViewModel.drawingViewModel.drawingObserver = DottysDrawingObserver(this)
        val locationId =  activity?.getUserPreference()?.homeLocationID
        homeViewModel.mainFragmentActivity = activity
        homeViewModel.drawingViewModel.getDrawingSummary(activity ?: return, locationId ?: return)
//
//
//        if (locationId != null) {
//            drawingViewModel.getUserDrawings()
//        }
       // homeViewModel.initDashboardItemsView(viewFragment!!, dawing,activity!!)
    }

    /*6*/// -- /*04*/
    override fun getUserRewards(dawing: DottysDrawingRewardsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        homeViewModel.initDashboardItemsView(viewFragment!!, dawing,activity!!)
        val redeemButton =
            viewFragment?.findViewById<Button>(R.id.redeem_rewards_button)
        val convertPointButton =
            viewFragment?.findViewById<Button>(R.id.convert_points_dashboard_button)
        redeemButton?.setOnClickListener {

            val intent = Intent(context, DottysRedeemRewardsActivity::class.java)
            intent.putExtra("REDEEM_REWARDS", homeViewModel.userCurrentUserDataObserver?.currentUserRewards?.toJson().toString())
            startActivity(intent)
        }
        convertPointButton?.setOnClickListener {
            activity.controller.navigate(R.id.nav_rewards, activity.intent.extras)
        }

        if (activity.getBeaconAtStoreLocation()?.size ?: 0 <= 0 && activity.getUserNearsLocations().locations?.size ?: 0 > 0){
             activity?.let { homeViewModel.getBeaconList(it) }
        }
//         gsp = GpsTracker(this)
//        gsp?.stopUsingGPS()
//        gsp.onLocationChanged(gpsTracker.locationGps!!)
    }

    override fun getDottysUserLocation(locationData: DottysDrawingRewardsModel) {
        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity?.saveDataPreference(PreferenceTypeKey.DOTTYS_USER_LOCATION,locationData.toJson())

        var beaconsArray = DottysBeaconArray(activity?.getBeaconStatus()?.beaconArray)
        homeViewModel.initAnalitycsItems(beaconsArray)

    }

    override fun getUserDrawings(drawing: DottysDrawingUserModel) {
      print(drawing)
     }

    override fun allItemsCollapse(isColappse: Boolean) {}

//    override fun onBeaconsChange(beaconsData: ArrayList<DottysBeacon>) {
//        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
//        homeViewModel.initAnalitycsItems(DottysBeaconArray(beaconsData),activity)
//    }

//    override fun onBeaconsServiceChange(beaconsData: java.util.ArrayList<DottysBeacon>) {
//
//    }

    override fun onBeaconsServiceChange(beaconsData: DottysBeaconArray) {
        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        if (beaconsData != activity?.getBeaconStatus()) {
            activity?.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,beaconsData.toJson())
        }

        homeViewModel.initAnalitycsItems(beaconsData)
    }


}
