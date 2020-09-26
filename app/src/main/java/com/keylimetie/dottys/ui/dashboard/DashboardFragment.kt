package com.keylimetie.dottys.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.ViewFlipper
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.*
import com.keylimetie.dottys.beacon_service.DottysBeaconActivityDelegate
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.ui.dashboard.models.DottysBanners
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconsModel
import com.keylimetie.dottys.ui.dashboard.models.DottysDrawingSumaryModel
import com.keylimetie.dottys.ui.drawing.DottysDrawingDelegates
import com.keylimetie.dottys.ui.drawing.DottysDrawingObserver
import com.keylimetie.dottys.ui.drawing.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.drawing.DottysDrawingUserModel
import com.keylimetie.dottys.ui.locations.DottysLocationDelegates
import com.keylimetie.dottys.ui.locations.DottysLocationStoresObserver
import com.keylimetie.dottys.ui.locations.DottysLocationsStoresModel
import com.keylimetie.dottys.ui.locations.LocationsViewModel
import com.squareup.picasso.Picasso


class DashboardFragment : Fragment(), DottysDashboardDelegates, DottysDrawingDelegates,
    DottysLocationDelegates, DottysBeaconActivityDelegate , View.OnClickListener, View.OnLayoutChangeListener {
    private var staticImagesResouerce = arrayListOf<Int>(R.id.dashboard_image_pager0,R.id.dashboard_image_pager1,R.id.dashboard_image_pager2,
                                                         R.id.dashboard_image_pager3,R.id.dashboard_image_pager4,R.id.dashboard_image_pager5,
                                                         R.id.dashboard_image_pager6,R.id.dashboard_image_pager7,R.id.dashboard_image_pager8,
                                                         R.id.dashboard_image_pager9)
    private var homeViewModel = DashboardViewModel()
    private var viewFragment: View? = null
    var maxChildFlipperView = 0
    var flipperViewDashboard: ViewFlipper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        viewFragment = root
       flipperViewDashboard  = viewFragment?.findViewById<ViewFlipper>(R.id.flipper_view_dashboard)
        return root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onResume() {
        super.onResume()
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.gpsTracker =
            this.context?.let { GpsTracker(it as DottysMainNavigationActivity) }!! /*GPS TRACKER*/

        activity?.requestLocation(activity?.gpsTracker, activity)
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
        activity?.let { homeViewModel.getBannerDashboard(it) }
    }
    /*0*/
    override fun getCurrentUser(currentUser: DottysLoginResponseModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        var locationModel = LocationsViewModel()
        locationModel.locationDataObserver = DottysLocationStoresObserver(this)
        val location = this.activity?.let {
            activity?.gpsTracker?.let { it1 ->
                //activity.getLocation(
                    it1.getLocation()
                //)
            }
        }
        if(location != null){
            activity?.let { locationModel.getLocationsDottysRequest(
                it,
                location?.latitude.toString(),
                location?.longitude.toString(),
                this
            ) }
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
        activity?.saveDataPreference(PreferenceTypeKey.LOCATIONS, locations.toJson())
        activity?.let { homeViewModel.getBeaconList(it) }
    }

    /*3*/// -- /*02*/
    override fun getGlobalData(gloabalData: DottysGlobalDataModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity?.saveDataPreference(PreferenceTypeKey.GLOBAL_DATA, gloabalData.toJson().toString())
        activity?.let { homeViewModel.initDashboardPager(it, gloabalData) }
    }

    /*4*/
    override fun getBeaconList(beaconList: DottysBeaconsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity?.saveDataPreference(
            PreferenceTypeKey.BEACON_AT_LOCATION,
            beaconList.toJson().toString()
        )
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

    }

    /*6*/// -- /*04*/
    override fun getUserRewards(dawing: DottysDrawingRewardsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        homeViewModel.initDashboardItemsView(viewFragment ?: return, dawing, activity ?: return)

    }

    override fun getDottysUserLocation(locationData: DottysDrawingRewardsModel) {
        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity?.saveDataPreference(PreferenceTypeKey.DOTTYS_USER_LOCATION, locationData.toJson())

        var beaconsArray = DottysBeaconArray(activity?.getBeaconStatus()?.beaconArray)
        homeViewModel.initAnalitycsItems(beaconsArray)

    }

    override fun getUserDrawings(drawing: DottysDrawingUserModel) {
      print(drawing)
     }

    override fun allItemsCollapse(isColappse: Boolean) {}

      fun addPagerDashboardImages(bannerList: List<DottysBanners>){
          var activity: DottysMainNavigationActivity? = getActivity() as DottysMainNavigationActivity
          var limitOfFlipperView: Int = bannerList.size
           if (bannerList.size > staticImagesResouerce.size){
               limitOfFlipperView =  staticImagesResouerce.size
           }
          for (index  in 0 until limitOfFlipperView) {
              val imageView = viewFragment?.findViewById<ImageView>(staticImagesResouerce[index])

              Picasso.with(activity)
                  .load(bannerList[index].image)
                  .into(imageView)

       }
          flipperViewDashboard?.setOnClickListener(this)
          flipperViewDashboard?.isAutoStart = true

          flipperViewDashboard?.flipInterval = 4000
          flipperViewDashboard?.startFlipping()
          flipperViewDashboard?.addOnLayoutChangeListener(this)
          maxChildFlipperView = bannerList.count()

    }

    override fun onBeaconsServiceChange(beaconsData: DottysBeaconArray) {
        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        if (beaconsData != activity?.getBeaconStatus()) {
            activity?.saveDataPreference(
                PreferenceTypeKey.BEACON_AT_CONECTION,
                beaconsData.toJson()
            )
        }

        homeViewModel.initAnalitycsItems(beaconsData)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.flipper_view_dashboard -> {
              if (flipperViewDashboard?.displayedChild ?: 0 >= maxChildFlipperView - 1) {
                  flipperViewDashboard?.displayedChild = 0
                 } else {
                     flipperViewDashboard?.showNext()
                 }
            }
        }
    }


    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        if (flipperViewDashboard?.displayedChild ?: 0 >= maxChildFlipperView ) {
            flipperViewDashboard?.displayedChild = 0
        }
    }


}
