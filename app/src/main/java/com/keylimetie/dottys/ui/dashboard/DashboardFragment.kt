package com.keylimetie.dottys.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ViewFlipper
import androidx.fragment.app.Fragment
import com.keylimetie.dottys.*
import com.keylimetie.dottys.beacon_service.DottysBeaconActivityDelegate
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.splash.getVersionApp
import com.keylimetie.dottys.ui.dashboard.models.DottysBanners
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconsModel
import com.keylimetie.dottys.ui.dashboard.models.DottysDrawingSumaryModel
import com.keylimetie.dottys.ui.drawing.DottysDrawingDelegates
import com.keylimetie.dottys.ui.drawing.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.drawing.DottysDrawingUserModel
import com.keylimetie.dottys.ui.locations.DottysLocationDelegates
import com.keylimetie.dottys.ui.locations.DottysLocationStoresObserver
import com.keylimetie.dottys.ui.locations.DottysLocationsStoresModel
import com.keylimetie.dottys.ui.locations.LocationsViewModel
import com.keylimetie.dottys.ui.profile.DottysProfileDelegates
import com.keylimetie.dottys.ui.profile.DottysProfileObserver
import com.keylimetie.dottys.ui.profile.ProfileViewModel
import com.keylimetie.dottys.utils.isEquivalentToString
import com.squareup.picasso.Picasso


class DashboardFragment : Fragment(), DottysDashboardDelegates, DottysDrawingDelegates,
    DottysLocationDelegates, View.OnClickListener, View.OnLayoutChangeListener,
    DottysProfileDelegates, DottysBeaconActivityDelegate {
    private var staticImagesResouerce = arrayListOf<Int>(
        R.id.dashboard_image_pager0, R.id.dashboard_image_pager1, R.id.dashboard_image_pager2,
        R.id.dashboard_image_pager3, R.id.dashboard_image_pager4, R.id.dashboard_image_pager5,
        R.id.dashboard_image_pager6, R.id.dashboard_image_pager7, R.id.dashboard_image_pager8,
        R.id.dashboard_image_pager9
    )
    var dashboardViewModel = DashboardViewModel(activity as DottysMainNavigationActivity?)
    private var viewFragment: View? = null
    var maxChildFlipperView = 0
    var flipperViewDashboard: ViewFlipper? = null
    var mainActivity = activity as DottysMainNavigationActivity?
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
//        homeViewModel =
//            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        viewFragment = root
        flipperViewDashboard = viewFragment?.findViewById<ViewFlipper>(R.id.flipper_view_dashboard)

        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        if (activity != null) {

            dashboardViewModel.initDashboardViewSetting(this, activity, viewFragment)
        }
        activity?.hideCustomKeyboard()

        //activity?.let { homeViewModel.getBannerDashboard(it) }
        return root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onResume() {
        super.onResume()
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.dashboardFragment = this
        activity?.gpsTracker =
            this.context?.let { GpsTracker(it as DottysMainNavigationActivity) }!! /*GPS TRACKER*/

        activity?.requestLocation(activity.gpsTracker, activity)
        activity?.gpsTracker?.locationGps?.let { activity.gpsTracker?.onLocationChanged(it) }


    }

    private fun updateDataAtProfile(activityMain: DottysMainNavigationActivity?) {
        dashboardViewModel.initDashboardButtons()
        val androidId = Secure.getString(
            context?.contentResolver,
            Secure.ANDROID_ID)
        val appVersion = "Android_${activityMain?.getVersionApp(activityMain)}"
        var profileData = activityMain?.getUserPreference()
        profileData?.deviceId = androidId
        profileData?.deviceId = if (profileData?.deviceId != androidId ||
            profileData?.appVersion != appVersion
        ) ({
            profileData?.deviceId = androidId
            profileData?.appVersion = appVersion
            if (profileData?.toJson()
                    ?.isEquivalentToString(activityMain?.getUserPreference()?.toJson()) == false
            ) {
                Log.d("DASHBOAR", "PROFILE ITS BEGIN TO UPADATE")
                val profileViewModel = ProfileViewModel(null, profileData)
                profileData.let {
                    profileViewModel.uploadProfile(it, activityMain)
                    profileViewModel.profileUpdateObserver = DottysProfileObserver(this)
                }
            }
        }).toString() else {
            return
        }


    }


    /* GET DOTTYS STORE LOCATION  */
    fun updateDottysLocations(activity: DottysBaseActivity) {
        var locationModel = LocationsViewModel(activity)
        locationModel.locationDataObserver = DottysLocationStoresObserver(this)
        val location = this.activity?.let {
            activity.gpsTracker?.let { it1 -> it1.getLocation() }
        }
        if (location != null) {
            activity.let {
                locationModel.getLocationsDottysRequest(
                    it,
                    location.latitude.toString(),
                    location.longitude.toString()
                )
            }
        }
    }

    /** 0 **/
    override fun getCurrentUser(currentUser: DottysLoginResponseModel) {
        //    var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        //    updateDottysLocations(activity as DottysMainNavigationActivity)
        dashboardViewModel.initDashboardButtons()
        dashboardViewModel.getBannerDashboard(activity as DottysMainNavigationActivity)
        //TODO dashboardViewModel.getDrawingSummary(activity as DottysMainNavigationActivity)
        //updateDataAtProfile(activity as DottysMainNavigationActivity)
    }

    /** 1 **/
    override fun onDashboardBanners(banners: ArrayList<DottysBanners>) {
        addPagerDashboardImages(banners)
        dashboardViewModel.getUserRewards(activity as DottysMainNavigationActivity)
    }

    /** 2 **/
    override fun getUserRewards(rewards: DottysRewardsModel) {
//        val activity = dashboardViewModel.mainFragmentActivity
//       if (activity?.getBeaconAtStoreLocation()?.size ?: 0 <= 0 && activity?.getUserNearsLocations()?.locations?.size ?: 0 > 0){
//            activity.let { dashboardViewModel?.getBeaconList(it as DottysBaseActivity)  }
//       }
        dashboardViewModel.drawingBadgeCounter =
            rewards.rewards?.filter { it.redeemed == false }?.size ?: 0
        dashboardViewModel.badgeCounterDrawingManager(dashboardViewModel.drawingBadgeCounter ?: 0)
        val mainActivity = activity as DottysMainNavigationActivity
        mainActivity.gpsTracker?.locationGps.let {
            val locationViewModel = LocationsViewModel(activity as DottysMainNavigationActivity)
            locationViewModel.locationDataObserver = DottysLocationStoresObserver(this)
            locationViewModel.getLocationsDottysRequest(mainActivity,
                it?.latitude.toString(),
                it?.longitude.toString())
        }

        //viewFragment?.let { activity?.let { it1 -> dashboardViewModel.addProfileImage(it1, it) } }
        //   dashboardViewModel.drawingViewModel.drawingObserver = DottysDrawingObserver(this)

//    TODO    val locationId =  activity?.getUserPreference()?.homeLocationID
//         dashboardViewModel.drawingViewModel.getCurrentDrawingLocation(activity ?: return, locationId ?: return)

    }

    /** 3 **/
    override fun getStoresLocation(locations: DottysLocationsStoresModel) {

        // mainActivity?.saveDataPreference(PreferenceTypeKey.LOCATIONS, locations.toJson())
        mainActivity?.beaconsStatusObserver?.distanceToNearStore =
            (locations.locations?.first()?.distance ?: return)
        if(locations.locations.isNullOrEmpty()){return}
        if (locations.locations?.first()?.distance ?: return < 0.5 || mainActivity?.getBeaconStatus()?.beaconArray.isNullOrEmpty()) {
            activity.let { dashboardViewModel.getBeaconList(it as DottysBaseActivity) }
        } else {
            activity?.let { dashboardViewModel.getDrawingSummary(it as DottysMainNavigationActivity) }
            dashboardViewModel.initAnalitycsItems(
                (if (mainActivity?.getBeaconStatus()?.beaconArray.isNullOrEmpty()) {
                    mainActivity?.getBeaconStatus()?.beaconArray
                } else {
                    mainActivity?.getBeaconStatus()?.beaconArray
                })
            )
        }
    }

    /** 4 // PASS **/
    override fun getBeaconList(beaconList: DottysBeaconsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.saveDataPreference(
            PreferenceTypeKey.BEACONS_LIST,
            beaconList.toJson().toString()
        )
        dashboardViewModel.initAnalitycsItems(activity?.getBeaconStatus()?.beaconArray
            ?: activity?.getBeaconStatus()?.beaconArray ?: return)
        activity.let { it?.let { it1 -> dashboardViewModel.getDrawingSummary(it1) } }

    }

    /** 4 **/
    override fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel) {
        updateDataAtProfile(activity as DottysMainNavigationActivity)
    }

    override fun onProfileUpdated(userProfile: DottysLoginResponseModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.let { dashboardViewModel.getGlobalDataRequest(it) }
    }

    //*02*/
    override fun getGlobalData(gloabalData: DottysGlobalDataModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity.saveDataPreference(PreferenceTypeKey.GLOBAL_DATA, gloabalData.toJson().toString())
        activity.let { dashboardViewModel.initDashboardPager(it, gloabalData) }
    }


    /*6*/// -- /*04*/
    override fun getUserRewards(dawing: DottysDrawingRewardsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        dashboardViewModel.initDashboardItemsView()

    }

    override fun getDottysUserLocation(locationData: DottysDrawingRewardsModel) {
        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity.saveDataPreference(PreferenceTypeKey.DOTTYS_USER_LOCATION, locationData.toJson())

        var beaconsArray = DottysBeaconArray(activity.getBeaconStatus()?.beaconArray)
        dashboardViewModel.initAnalitycsItems(beaconsArray.beaconArray
            ?: activity.getBeaconStatus()?.beaconArray ?: return)
        updateDataAtProfile(activity)

    }

    override fun getUserDrawings(drawing: DottysDrawingUserModel) {
        print(drawing)
    }

    override fun allItemsCollapse(isColappse: Boolean) {}

    private fun addPagerDashboardImages(bannerList: List<DottysBanners>) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity
        var limitOfFlipperView: Int = bannerList.size
        if (bannerList.size > staticImagesResouerce.size) {
            limitOfFlipperView = staticImagesResouerce.size
        }
        for (index in 0 until limitOfFlipperView) {
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

        dashboardViewModel.initAnalitycsItems(beaconsData.beaconArray)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
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
        oldBottom: Int,
    ) {
        if (flipperViewDashboard?.displayedChild ?: 0 >= maxChildFlipperView) {
            flipperViewDashboard?.displayedChild = 0
        }
    }


}
