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
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.redeem.DottysRedeemRewardsActivity
import com.keylimetie.dottys.ui.drawing.DottysDrawingDelegates
import com.keylimetie.dottys.ui.drawing.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.drawing.DottysDrawingUserModel
import com.keylimetie.dottys.ui.locations.DottysLocationDelegates
import com.keylimetie.dottys.ui.locations.DottysLocationStoresObserver
import com.keylimetie.dottys.ui.locations.DottysLocationsStoresModel
import com.keylimetie.dottys.ui.locations.LocationsViewModel


class DashboardFragment : Fragment(), DottysDashboardDelegates, DottysDrawingDelegates,
    DottysLocationDelegates {

    var homeViewModel = DashboardViewModel()
    var viewFragment: View? = null
    var gsp: GpsTracker? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        viewFragment = root
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        gsp = this.context?.let { GpsTracker(it) }

        activity?.requestLocation(gsp, activity)

        return root
    }

    override fun onStart() {
        super.onStart()
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        if (activity != null) {
            homeViewModel.initDashboardViewSetting(this, activity)
        }
        activity?.hideCustomKeyboard()

    }

    override fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.let { homeViewModel.getGlobalDataRequest(it) }
    }

    override fun getCurrentUser(currentUser: DottysLoginResponseModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
       var locationModel = LocationsViewModel()
        locationModel.locationDataObserver = DottysLocationStoresObserver(this)
        val location = this.activity?.let { gsp?.let { it1 -> activity?.getLocation(it1, it) } }
        if(location != null){
        activity?.let { locationModel.getLocationsDottysRequest(it,location?.latitude.toString(), location?.longitude.toString(),this) }
            }
    }

    override fun getUserRewards(rewards: DottysRewardsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        viewFragment?.let { activity?.let { it1 -> homeViewModel.addProfileImage(it1, it, this) } }
    }

    override fun getGlobalData(gloabalData: DottysGlobalDataModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.let { homeViewModel.initDashboardPager(it, gloabalData) }
    }

    override fun getDottysUserLocation(locationData: DottysDrawingRewardsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity?.saveDataPreference(PreferenceTypeKey.DOTTYS_USER_LOCATION,locationData.toJson())
    }

    override fun getBeaconList(beaconList:DottysBeaconsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity?.saveDataPreference(PreferenceTypeKey.BEACON_AT_LOCATION,beaconList.toJson().toString())
    }

    override fun getUserRewards(dawing: DottysDrawingRewardsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?

        homeViewModel.initDashboardItemsView(viewFragment!!, dawing,activity!!)

        val redeemButton =
            viewFragment?.findViewById<Button>(com.keylimetie.dottys.R.id.redeem_rewards_button)
        redeemButton?.setOnClickListener {

            val intent = Intent(context, DottysRedeemRewardsActivity::class.java)
            intent.putExtra("REDEEM_REWARDS", homeViewModel.userCurrentUserDataObserver?.currentUserRewards?.toJson().toString())
            startActivity(intent)
         }


        }

    override fun getUserDrawings(drawing: DottysDrawingUserModel) {
      print(drawing)
     }

    override fun getStoresLocation(locations: DottysLocationsStoresModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity?.saveDataPreference(PreferenceTypeKey.LOCATIONS,locations.toJson())
//        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
//
       // activity?.let { homeViewModel.getNearsDottysLocations(it) }
        activity?.let { homeViewModel.getBeaconList(it) }
    }

    override fun allItemsCollapse(isColappse: Boolean) {  }
}
