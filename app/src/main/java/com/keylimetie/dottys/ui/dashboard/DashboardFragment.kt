package com.keylimetie.dottys.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings.Secure.*
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.keylimetie.dottys.*
import com.keylimetie.dottys.beacon_service.DottysBeaconActivity
import com.keylimetie.dottys.beacon_service.DottysBeaconActivityDelegate
import com.keylimetie.dottys.beacon_service.DottysBeaconActivityObserver
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.register.DottysRegisterActivity
import com.keylimetie.dottys.splash.getVersionApp
import com.keylimetie.dottys.ui.dashboard.models.*
import com.keylimetie.dottys.ui.drawing.DottysDrawingDelegates
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingUserModel
import com.keylimetie.dottys.ui.locations.*
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
    lateinit var mainHandler: Handler
    var dashboardViewModel = DashboardViewModel(activity as DottysMainNavigationActivity?)
    private var viewFragment: View? = null
    var maxChildFlipperView = 0
    var flipperViewDashboard: ViewFlipper? = null
    var mainActivity = activity as DottysMainNavigationActivity?
    private val updateTextTask = object : Runnable {
        override fun run() {
            gotToNextBanner()
            mainHandler.postDelayed(this, 5000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        viewFragment = root
        flipperViewDashboard = viewFragment?.findViewById<ViewFlipper>(R.id.flipper_view_dashboard)

        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        if (activity != null) {

            dashboardViewModel.initDashboardViewSetting(this, activity, viewFragment)
        }
        activity?.hideCustomKeyboard(activity)
        activity?.beaconService?.observer = DottysBeaconActivityObserver(this)
        mainHandler = Handler(Looper.getMainLooper())
        return root
    }



    override fun onStart() {
        super.onStart()
        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.dashboardFragment = this
        activity?.beaconService?.observer = DottysBeaconActivityObserver(this)
        getActivity()?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

    }
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onResume() {
        super.onResume()

        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.dashboardFragment = this
        activity?.beaconService?.observer = DottysBeaconActivityObserver(this)
        activity?.gpsTracker =
            this.context?.let { GpsTracker(it as DottysMainNavigationActivity) }!! /*GPS TRACKER*/

        activity?.requestLocation(activity.gpsTracker, activity)
        activity?.gpsTracker?.locationGps?.let { activity.gpsTracker?.onLocationChanged(it) }
        mainHandler.post(updateTextTask)
        dashboardViewModel.refreshItemsAtView()
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTextTask)
    }

    @SuppressLint("HardwareIds")
    private fun checkDataAtProfile(activityMain: DottysMainNavigationActivity?, isUpdateable: Boolean) {
        dashboardViewModel.initDashboardButtons()
        val location = activityMain?.getUserNearsLocations()?.locations.let { it?.first() }
        val androidId = getString(
            context?.contentResolver,
            ANDROID_ID)
        val appVersion = "Android_${activityMain?.getVersionApp(activityMain)}"
        val profileData = activityMain?.getUserPreference()
        if (isUpdateable){
            callUpdateProfileData(profileData, activityMain, isUpdateable)
            return
        }
        profileData?.deviceId = if (profileData?.deviceId != androidId ||
            profileData?.appVersion != appVersion
        ) ({
            profileData?.deviceId = androidId
            profileData?.appVersion = appVersion
        }).toString() else { androidId }
        if ((location?.distance ?: 0.0) <= 1.0 &&
                profileData?.address1 ?: "" != location?.address1 ?: ""){
            profileData?.address1 = location?.address1
            profileData?.zip  = location?.zip
        }
        callUpdateProfileData(profileData, activityMain, isUpdateable)
    }

    private fun callUpdateProfileData(profileData: DottysLoginResponseModel?, activityMain: DottysMainNavigationActivity?, isUpdateable: Boolean){
        if (profileData?.toJson()
                ?.isEquivalentToString(activityMain?.getUserPreference()?.toJson()) == false or isUpdateable )
        {
            Log.d("DASHBOARD", "PROFILE ITS BEGIN TO UPADATE")
            val profileViewModel = ProfileViewModel(null, profileData)
            profileData.let {
                it?.let { it1 -> profileViewModel.uploadProfile(it1, activityMain) }
                profileViewModel.profileUpdateObserver = DottysProfileObserver(this)
            }
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

    /**
     *      0
     * CURRENT USER
     * **/
    override fun getCurrentUser(currentUser: DottysLoginResponseModel) {
        //    var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        //    updateDottysLocations(activity as DottysMainNavigationActivity)
        when {
            currentUser.cell.isNullOrEmpty() -> {
             initVerificationCell()
            }
            currentUser.cellVerified == false -> {
                val intent = Intent(activity as DottysMainNavigationActivity, DottysRegisterActivity::class.java)
                intent.putExtra("IS_REGISTER_USER", true)
                (activity as DottysMainNavigationActivity)?.startActivity(intent)
            }
            else -> {
                  dashboardViewModel.initDashboardButtons()
                  dashboardViewModel.getBannerDashboard(activity as DottysMainNavigationActivity)
              }
          }


        //TODO dashboardViewModel.getDrawingSummary(activity as DottysMainNavigationActivity)
        //updateDataAtProfile(activity as DottysMainNavigationActivity)
    }

    @SuppressLint("InflateParams")
    private fun initVerificationCell() {
        val addPhoneView = layoutInflater.inflate(R.layout.view_phone_verification, null) as ViewGroup
        val dashboardLayout = viewFragment?.findViewById<ConstraintLayout>(R.id.dashboard_layout)
        val buttonDone = addPhoneView.findViewById<Button>(R.id.validation_done_button)
        val validationPhoneEditText = addPhoneView.findViewById<EditText>(R.id.validation_phone_edittext)
        dashboardLayout?.addView(addPhoneView)
        val phoneViewParams = (viewFragment?.height?: 0)*0.5
        addPhoneView.elevation = 25f
        buttonDone?.elevation = 35f

        addPhoneView.animate()
            .translationY(-(phoneViewParams).toFloat())
            .setDuration(0)
            .start()
        addPhoneView.animate()
            .translationY((((viewFragment?.height
                ?: 0) * 0.8 / 2) - (phoneViewParams / 2)).toFloat())
            .setDuration(700)
            .setStartDelay(750)
            .start()
       validationPhoneEditText.addTextChangedListener(object : PhoneNumberFormattingTextWatcher() {
           override fun onTextChanged(
               s: CharSequence?,
               start: Int,
               before: Int,
               count: Int,
           ) {
               if (!validationPhoneEditText.text.contains("+1") && !validationPhoneEditText.text.isEmpty()) {
                   validationPhoneEditText.text.insert(0, "+1")
               } else if (validationPhoneEditText.text.toString() == "+1") {
                   validationPhoneEditText.setText("")
               }
               super.onTextChanged(s, start, before, count)
           }
       })
        buttonDone?.setOnClickListener{

             if (validationPhoneEditText.text.isBlank()) {
                mainActivity?.hideCustomKeyboard(activity as DottysMainNavigationActivity)
                DottysBaseActivity().showSnackBarMessage(activity as DottysMainNavigationActivity,
                     "Phone number is invalid or Empty, please check phone number field.")

             } else {
                 val userData =  (activity as DottysMainNavigationActivity).getUserPreference()
                 userData?.cell = validationPhoneEditText.text.toString().replace(" ","").replace("-","")
                 (activity as DottysMainNavigationActivity)?.saveDataPreference(PreferenceTypeKey.USER_DATA, userData?.toJson() ?: return@setOnClickListener)
                 checkDataAtProfile(activity as DottysMainNavigationActivity, true)
                 addPhoneView.animate()
                     .translationY(-((viewFragment?.height ?: 0) * 0.7 / 2).toFloat())
                     .setDuration(350)
                     .start()
             }
        }

    }

    /**
     *          1
     *   BANNER AT DASHBOARD
     *   **/
    override fun onDashboardBanners(banners: DottysBannerModel) {
        val bannersList = banners.bannerList?.sortedBy { it.priority }
        addPagerDashboardImages(bannersList ?: return)
        dashboardViewModel.getUserRewards(activity as DottysMainNavigationActivity)
        (activity as DottysMainNavigationActivity).saveDataPreference(PreferenceTypeKey.BANNERS, banners.toJson())
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

        (activity as DottysMainNavigationActivity).gpsTracker?.locationGps.let {
            val locationViewModel = LocationsViewModel(activity as DottysMainNavigationActivity)
            locationViewModel.locationDataObserver = DottysLocationStoresObserver(this)
            locationViewModel.getLocationsDottysRequest((activity as DottysMainNavigationActivity),
                it?.latitude.toString(),
                it?.longitude.toString())
        }

        //TODO REMOVE INTENT
//        var intent = Intent(activity, DottysProfilePictureActivity::class.java)
//        startActivity(intent)
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
        //if (locations.locations?.first()?.distance ?: return < 0.5 || mainActivity?.getBeaconStatus()?.beaconArray.isNullOrEmpty()) {
            activity.let { dashboardViewModel.getBeaconList(it as DottysBaseActivity, locations.locations?.first()?.storeNumber.toString()) }
        //} else {

     //   }
    }

    /** 4
     *  **/
    override fun getBeaconList(beaconList: DottysBeaconsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.saveDataPreference(
            PreferenceTypeKey.BEACONS_LIST,
            beaconList.toJson().toString()
        )
        DottysBeaconActivity(activity ?: return)
        dashboardViewModel.initAnalitycsItems(activity.getBeaconStatus()?.beaconArray
            ?: activity.getBeaconStatus()?.beaconArray ?: return)
        activity.let { dashboardViewModel.getDrawingSummary(it as DottysMainNavigationActivity) }
        dashboardViewModel.initAnalitycsItems(
            (if (mainActivity?.getBeaconStatus()?.beaconArray.isNullOrEmpty()) {
                mainActivity?.getBeaconStatus()?.beaconArray
            } else {
                mainActivity?.getBeaconStatus()?.beaconArray
            })
        )

      //  activity.let { it?.let { it1 -> dashboardViewModel.getDrawingSummary(it1) } }

    }

    /** 5 **/
    override fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel) {
        checkDataAtProfile(activity as DottysMainNavigationActivity, false)
    }

    override fun onProfileUpdated(userProfile: DottysLoginResponseModel?) {
        if(userProfile == null){
            var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
            activity?.let { dashboardViewModel.getGlobalDataRequest(it) }
            return
        } //TODO REMOVE ON FIX BACKEND ANIVERSARY DATE
        when {
            userProfile.cell.isNullOrEmpty() -> {
             initVerificationCell()
            }
            userProfile.cellVerified == false -> {
                val intent = Intent(activity as DottysMainNavigationActivity, DottysRegisterActivity::class.java)
                intent.putExtra("IS_REGISTER_USER", true)
                (activity as DottysMainNavigationActivity)?.startActivity(intent)

            }
            else -> {
                var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
                activity?.let { dashboardViewModel.getGlobalDataRequest(it) }
            }
        }
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

    @SuppressLint("CommitPrefEdits")
    override fun getDottysUserLocation(locationData: DottysDrawingRewardsModel) {
        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.editor = activity?.sharedPreferences!!.edit()
        activity.saveDataPreference(PreferenceTypeKey.DOTTYS_USER_LOCATION, locationData.toJson())

        val beaconsArray = DottysBeaconArray(activity.getBeaconStatus()?.beaconArray)
        dashboardViewModel.initAnalitycsItems(beaconsArray.beaconArray
            ?: activity.getBeaconStatus()?.beaconArray ?: return)
        checkDataAtProfile(activity, false)

    }

    override fun getUserDrawings(drawing: DottysDrawingUserModel) {
        print(drawing)
    }

    override fun allItemsCollapse(isColappse: Boolean) {}

      fun addPagerDashboardImages(bannerList: List<DottysBanners>) {
        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity
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
        flipperViewDashboard?.addOnLayoutChangeListener(this)
        maxChildFlipperView = bannerList.count()


    }

    override fun onBeaconsServiceChange(beaconsData: DottysBeaconArray) {
        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        if (beaconsData != activity?.getBeaconStatus()) {
            activity?.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,beaconsData.toJson()
            )
        }


        dashboardViewModel.initAnalitycsItems(beaconsData.beaconArray)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.flipper_view_dashboard -> {
                mainHandler.removeCallbacks(updateTextTask)
                mainHandler.post(updateTextTask)
            }

        }
    }


    fun gotToNextBanner(){
        if (flipperViewDashboard?.displayedChild ?: 0 >= maxChildFlipperView - 1) {
            flipperViewDashboard?.displayedChild = 0
        } else {
            flipperViewDashboard?.showNext()
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
