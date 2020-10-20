package com.keylimetie.dottysimport android.Manifestimport android.app.NotificationChannelimport android.app.NotificationManagerimport android.content.Contextimport android.content.Intentimport android.content.SharedPreferencesimport android.content.pm.PackageManager.PERMISSION_GRANTEDimport android.content.res.Configurationimport android.graphics.drawable.ColorDrawableimport android.location.Locationimport android.os.Bundleimport android.os.Handlerimport android.util.DisplayMetricsimport android.util.Logimport android.view.Viewimport android.view.WindowManagerimport android.view.inputmethod.InputMethodManagerimport android.widget.ImageButtonimport android.widget.ProgressBarimport android.widget.Toastimport androidx.appcompat.app.ActionBarimport androidx.appcompat.app.AlertDialogimport androidx.appcompat.app.AppCompatActivityimport androidx.core.app.ActivityCompatimport androidx.core.app.NotificationManagerCompatimport androidx.core.content.ContextCompat.checkSelfPermissionimport androidx.lifecycle.LifecycleObserverimport com.dottysrewards.dottys.service.VolleyServiceimport com.estimote.coresdk.common.config.EstimoteSDKimport com.keylimetie.dottys.beacon_service.DottysBeaconActivityimport com.keylimetie.dottys.beacon_service.DottysBeaconDelegatesimport com.keylimetie.dottys.models.DottysGlobalDataModelimport com.keylimetie.dottys.models.DottysRewardsModelimport com.keylimetie.dottys.splash.DottysSplashActivityimport com.keylimetie.dottys.ui.dashboard.DashboardFragmentimport com.keylimetie.dottys.ui.dashboard.models.DottysBeaconimport com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArrayimport com.keylimetie.dottys.ui.dashboard.models.DottysBeaconsModelimport com.keylimetie.dottys.ui.drawing.models.DottysDrawingRewardsModelimport com.keylimetie.dottys.ui.locations.DottysLocationStoresObserverimport com.keylimetie.dottys.ui.locations.DottysLocationsMapFragmentimport com.keylimetie.dottys.ui.locations.DottysLocationsStoresModelimport com.keylimetie.dottys.ui.locations.LocationsViewModelimport com.keylimetie.dottys.utils.ForegroundCheckTaskimport com.keylimetie.dottys.utils.geofence.DottysGeofenceimport com.keylimetie.dottys.utils.isProbablyAnEmulatorimport com.onesignal.*import kotlin.properties.Delegatesenum class PreferenceTypeKey {    USER_DATA, REWARDS, GLOBAL_DATA, LOCATIONS, DOTTYS_USER_LOCATION, BEACONS_LIST, BEACON_AT_CONECTION, DRAWINGS, PUSH_NOTIFICATION}object EstimoteCredentials {    const val APP_ID =        "appsupport-icsdottys-com-s-00i"//dottys-rewards-aa2"// "appsupport-icsdottys-com-s-00i"    const val APP_TOKEN = "3279b08f8557362e2d6bb7901b83ed17" //"d32ab3c4fb178ee04defe33d53ac9932"}open class DottysBaseActivity : AppCompatActivity(), DottysBeaconDelegates, OSPermissionObserver,    OSSubscriptionObserver, LifecycleObserver, DottysLocationChangeDelegates,    com.keylimetie.dottys.ui.locations.DottysLocationDelegates {    /**     * BASE VARIABLES     */    //region    private var geofenceActivity: DottysGeofence? = null    var backButton: ImageButton? = null    var sharedPreferences: SharedPreferences? = null    var editor: SharedPreferences.Editor? = null    var actionBarView: View? = null    var isUpdatingLocation: Boolean? = null    var baseUrl: String? = null    var progressBar: ProgressBar? = null    val displayMetrics = DisplayMetrics()    var gpsTracker: GpsTracker? = null    var beaconsStatusObserver: DottysBeaconStatusObserver? = null    var mainNavigationActivity: DottysMainNavigationActivity? = null    var dashboardFragment: DashboardFragment? = null    var mapFragmentBase: DottysLocationsMapFragment? = null    var beaconService: DottysBeaconActivity? = null    var locationsBaseViewModel: LocationsViewModel? = null    var lastKnownLatitudeGps = 0.0    var lastKnownLongitudeGps = 0.0     //endregion    /**     * START LIFECICLE BASE ACTIVITY     */    override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)        VolleyService.initialize(this)        OneSignal.addPermissionObserver(this)        OneSignal.addSubscriptionObserver(this)        // baseUrl = this.resources.getString(R.string.url_base_development)        baseUrl = this.resources.getString(R.string.url_base_production)        progressBar = findViewById(R.id.progress_loader)        //hideLoader(this)        sharedPreferences = this.getSharedPreferences(            PreferenceTypeKey.USER_DATA.name,            Context.MODE_PRIVATE        )        //firebaseAnalytics = Firebase.analytics        println(getUserPreference().token)        // Logging set to help debug issues, remove before releasing your app.        //   OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);        // OneSignal Initialization        OneSignal.startInit(this)            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)            .unsubscribeWhenNotificationsAreDisabled(true)            .init()        /* TRANSITION ANIM */        overridePendingTransition(            R.anim.slide_in_left, R.anim.slide_out_left        )    }    private fun initEstimoteBeaconManager(context: DottysBaseActivity) {        if (isProbablyAnEmulator()) {            return        }        beaconService = DottysBeaconActivity(this)        beaconService.let { it?.initBeaconManager() }    }    override fun onStart() {        super.onStart()        if (gpsTracker == null) {            gpsTracker = GpsTracker(this)            gpsTracker?.getLocation()            gpsTracker?.locationObserver = DottysLocationObserver(this)        }        EstimoteSDK.initialize(this, EstimoteCredentials.APP_ID, EstimoteCredentials.APP_TOKEN)        if (getUserPreference() != null || progressBar?.visibility != 0) {            if (!(getUserNearsLocations().locations.isNullOrEmpty()) &&                getUserNearsLocations().locations?.first()?.distance ?: return < 0.2            ) {               // Handler().postDelayed({                    initEstimoteBeaconManager(this)              //  }, 1000 * 60 * 5) // 5 minutes            } else {                beaconService?.let { it -> it.beaconManager?.let { it.disconnect() } }            }        }    }    override fun finish() {        super.finish()        overridePendingTransition(            R.anim.slide_out_left, R.anim.slide_out_right        )    }    override fun onOSPermissionChanged(stateChanges: OSPermissionStateChanges) {        if (stateChanges.from.enabled &&            !stateChanges.to.enabled        ) {            AlertDialog.Builder(this).setMessage("Notification Disable").show()        }        Log.i("Debug", "onOSPermissionChanged: $stateChanges")    }    override fun onOSSubscriptionChanged(stateChanges: OSSubscriptionStateChanges?) {        if (!(stateChanges?.from?.subscribed ?: return) &&            stateChanges.to?.subscribed ?: return        ) {            AlertDialog.Builder(this)                .setMessage("You've successfully subscribed to push notifications!")                .show()            stateChanges.to.userId        }        Log.i("Debug", "onOSSubscriptionChanged: " + stateChanges)    }    override fun onConfigurationChanged(newConfig: Configuration) {        super.onConfigurationChanged(newConfig)        // Checks whether a hardware keyboard is available        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show()        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show()        }    }    /**     * BEACON RECORDED     */    override fun getBeaconRecorded(beaconRecorded: DottysBeacon) {        var beaconAux = beaconRecorded        var beaconsTemp = ArrayList<DottysBeacon>()        if (getBeaconStatus()?.beaconArray?.size ?: 0 > 0) {            beaconsTemp = (getBeaconStatus()!!).beaconArray ?: ArrayList<DottysBeacon>()        }        var beaconsTemp2 = beaconsTemp        for (x in 0 until beaconsTemp.size) {           //  beaconsTemp2[x].id = beaconAux.beaconIdentifier            if (beaconsTemp[x].id == beaconRecorded.beaconIdentifier ||                beaconsTemp[x].beaconIdentifier == beaconRecorded.beaconIdentifier            ) {                beaconAux.isConected = beaconRecorded.eventType?.equals("ENTER") == true                beaconsTemp2[x] = beaconAux            } else {                beaconsTemp2[x] = beaconsTemp[x]            }        }        saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,            DottysBeaconArray(beaconsTemp2).toJson())        if (dashboardFragment == null){            try {               if  ((this as DottysBaseActivity).supportFragmentManager.fragments.first() is DashboardFragment) {                   dashboardFragment = (this as DottysBaseActivity).supportFragmentManager.fragments.first() as DashboardFragment                }            } catch (e: Error) {                Log.e("CAST TO DASHBOARD", e.toString())            }        }        dashboardFragment?.dashboardViewModel?.initAnalitycsItems(DottysBeaconArray(beaconsTemp2).beaconArray)        Toast.makeText(            this,            "Has ${beaconRecorded.eventType} to ${beaconRecorded.beaconType} Beacon",            Toast.LENGTH_LONG        ).show()    }    /**     * BACKGROUND AND FOREGROEUND LOCATION LISENER     */    override fun onLocationChangeHandler(locationGps: Location?) {        Log.d("LOCATION CHANGE",            "**  BASE ACTIVITY ${locationGps?.latitude} //  ${locationGps?.longitude}")        if (locationsBaseViewModel == null) {            locationsBaseViewModel = LocationsViewModel(this)        }        locationsBaseViewModel?.getLocationsDottysRequest(this,            locationGps?.latitude.toString(),            locationGps?.longitude.toString())        locationsBaseViewModel?.locationDataObserver = DottysLocationStoresObserver(this)        if (mapFragmentBase != null) {            mapFragmentBase?.updateMarker()            val fragmentActivity =                locationsBaseViewModel?.locationFragment?.activity as DottysMainNavigationActivity            locationsBaseViewModel?.initLocationView(locationsBaseViewModel?.locationFragment                ?: return, fragmentActivity, locationsBaseViewModel?.rootView ?: return)        }    }    /**     * DOTTYS LOCATION LISENER     */    override fun getStoresLocation(locations: DottysLocationsStoresModel) {        editor = sharedPreferences!!.edit()        if (locations.locations.isNullOrEmpty()) {            return        }        val distance = locations.locations?.first()?.distance ?: return        Log.e("STORES HAS UPDATED", "Distance to near store $distance")        dashboardFragment?.dashboardViewModel?.initAnalitycsItems(getBeaconStatus()?.beaconArray)        if (geofenceActivity == null && distance < 20000) {            geofenceActivity = DottysGeofence(this)        }    }    /**     * COLLAPSED ITEMS - UNUSEFUL     */    override fun allItemsCollapse(isColappse: Boolean) {}    /**     * ACTIONBAR SETTING     */    fun actionBarSetting(actionBar: ActionBar, coloBackground: ColorDrawable) {        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM        actionBar.setDisplayShowCustomEnabled(true)        actionBar.setCustomView(R.layout.custom_action_bar)        actionBar.elevation = 1F        actionBarView = actionBar.customView        actionBar.setBackgroundDrawable(coloBackground)        backButton = actionBarView?.findViewById<ImageButton>(R.id.back_image_button)        backButton?.setOnClickListener {            finish()        }    }    /**     * SAVE PREFERENCE DATA     * */    fun saveDataPreference(keyPreference: PreferenceTypeKey, jsonData: String) {        editor = sharedPreferences!!.edit()        editor!!.putString(keyPreference.name, jsonData)        editor!!.commit()        when (keyPreference) {            PreferenceTypeKey.BEACONS_LIST -> {               if(getBeaconStatus()?.beaconArray.isNullOrEmpty() ||                   getBeaconStatus()?.beaconArray?.first()?.major != getDottysBeaconsList()?.first()?.major){                   saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,                        DottysBeaconArray(getDottysBeaconsList()).toJson())               }            }//            PreferenceTypeKey.LOCATIONS -> {//                var beaconData = getDottysBeaconsList() ?: return//                val locationData = DottysLocationsStoresModel.fromLocationJson(jsonData)//                if (locationData.locations?.first()?.distance ?: 1.0 > 1.5) {//                    saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,//                        DottysBeaconArray(ArrayList<DottysBeacon>()).toJson())//                    return//                }//                var beaconAtStore =//                    beaconData.filter { it.location?.storeNumber ?: 0 == locationData.locations?.first()?.storeNumber ?: 0 }//                            as ArrayList//                saveDataPreference(PreferenceTypeKey.BEACONS_LIST,//                    DottysBeaconArray(beaconAtStore).toJson())//            }            else -> {                return            }        }    }    fun enablePushNotification(isEnable:Boolean){        editor = sharedPreferences!!.edit()        editor!!.putBoolean(PreferenceTypeKey.PUSH_NOTIFICATION.name, isEnable)        editor!!.commit()        if (isEnable) {            NotificationManagerCompat.from(this).cancelAll()        } else {            val notificationChannel = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {                NotificationChannel("channel_01", this.resources.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT)            } else {               return            }            NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel)        }    }    fun isPushNotificationEnabled():Boolean {        if (sharedPreferences == null) {            sharedPreferences = this.getSharedPreferences(                PreferenceTypeKey.USER_DATA.name,                Context.MODE_PRIVATE            )        }          return   NotificationManagerCompat.from(this).areNotificationsEnabled()//sharedPreferences!!.getBoolean(PreferenceTypeKey.PUSH_NOTIFICATION.name, false)    }    /**     * REMOVE PREFERENCE DATA     */    private fun removeReferenceData(keyPreference: PreferenceTypeKey) {        editor = sharedPreferences!!.edit()        editor!!.remove(keyPreference.name)        editor!!.commit()    }    /**     * REQUEST LOCATION PERMOSION     * */    open fun requestLocation(gpsTracker: GpsTracker?, activity: AppCompatActivity?) {        try {            if (checkSelfPermission(                    applicationContext,                    Manifest.permission.ACCESS_FINE_LOCATION                ) !== PERMISSION_GRANTED            ) {                ActivityCompat.requestPermissions(                    activity!!,                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),                    101                )            }        } catch (e: java.lang.Exception) {            e.printStackTrace()        }//        gpsTracker?.let { activity?.let { it1 -> getLocation(it, it1) } }    }    /**     * GET BEACON FOR CURRENT STORE     * */    fun getDottysBeaconsList(): ArrayList<DottysBeacon>? {        val textoDate = sharedPreferences!!.getString(PreferenceTypeKey.BEACONS_LIST.name, "")        return try {            var person: DottysBeaconsModel =                DottysBeaconsModel.fromJson(                    textoDate!!                )            try {                var beaconAtStore =                    person.beacons//.beacons?.filter { it.location?.storeNumber ?: 0 == getUserNearsLocations().locations?.first()?.storeNumber ?: 0 }                            as ArrayList                //saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,                //    DottysBeaconArray(beaconAtStore).toJson())                beaconAtStore            } catch (e: Exception) {                println(e)                null            }        } catch (e: Exception) {            println(e)            null        }    }    /**     * GET BEAON STATUS     * */    fun getBeaconStatus(): DottysBeaconArray? {        if (sharedPreferences == null) {            sharedPreferences = this.getSharedPreferences(                PreferenceTypeKey.BEACON_AT_CONECTION.name,                Context.MODE_PRIVATE            )        }        val textoDate =            sharedPreferences!!.getString(PreferenceTypeKey.BEACON_AT_CONECTION.name, "")        return try {            var person: DottysBeaconArray? =                textoDate?.let { DottysBeaconArray.fromJson(it) }            person        } catch (e: Exception) {            println(e)            null        }    }    /**     * GET CURRENT USER     */    fun getUserPreference(): DottysLoginResponseModel {        if (sharedPreferences == null) {            sharedPreferences = this.getSharedPreferences(                PreferenceTypeKey.USER_DATA.name,                Context.MODE_PRIVATE            )        }        val userData = sharedPreferences!!.getString(PreferenceTypeKey.USER_DATA.name, "")        return try {            var currentUserData: DottysLoginResponseModel =                DottysLoginResponseModel.fromJson(                    userData!!                )            currentUserData        } catch (e: Exception) {            println(e)            DottysLoginResponseModel()        }    }    /**     * GET GLOBAL DATA     */    fun getGlobalData(): DottysGlobalDataModel {        val globalInfo = sharedPreferences!!.getString(PreferenceTypeKey.GLOBAL_DATA.name, "")        return try {            var globalData: DottysGlobalDataModel =                DottysGlobalDataModel.fromJson(                    globalInfo!!                )            globalData        } catch (e: Exception) {            println(e)            DottysGlobalDataModel()        }    }    /**     * GET DRAWINGS     */    fun getDrawings(): DottysDrawingRewardsModel {        val drawingData = sharedPreferences!!.getString(PreferenceTypeKey.DRAWINGS.name, "")        return try {            var drawingLocations: DottysDrawingRewardsModel =                DottysDrawingRewardsModel.fromJson(                    drawingData!!                )            drawingLocations        } catch (e: Exception) {            println(e)            DottysDrawingRewardsModel()        }    }    /**     * GET ALL DOTTYS LOCATIONS     */    fun getUserNearsLocations(): DottysLocationsStoresModel {        if (sharedPreferences == null) {            sharedPreferences = this.getSharedPreferences(                PreferenceTypeKey.USER_DATA.name,                Context.MODE_PRIVATE            )        }        val nearStoreData = sharedPreferences?.getString(PreferenceTypeKey.LOCATIONS.name, "")        return try {            var nearStoreLocation: DottysLocationsStoresModel =                DottysLocationsStoresModel.fromLocationJson(                    nearStoreData!!                )            nearStoreLocation        } catch (e: Exception) {            println(e)            DottysLocationsStoresModel()        }    }    /**     * GET REWARDS AT SESSION     */    fun getRewardsAtSession(): DottysRewardsModel {        if (sharedPreferences == null) {            sharedPreferences = this.getSharedPreferences(                PreferenceTypeKey.REWARDS.name,                Context.MODE_PRIVATE            )        }        val rewardsDAta = sharedPreferences!!.getString(PreferenceTypeKey.REWARDS.name, "")        return try {            var rewards: DottysRewardsModel =                DottysRewardsModel.fromJson(                    rewardsDAta!!                )            rewards        } catch (e: Exception) {            println(e)            DottysRewardsModel()        }    }    /**     * BASE PUBLIC FUNTIONS      */    fun finishSession(mContext: DottysBaseActivity) {        // Toast.makeText(mContext, "User Logout", Toast.LENGTH_LONG).show()        val editPref = mContext.sharedPreferences?.edit()        editPref?.clear()        editPref?.apply()        mContext.removeReferenceData(PreferenceTypeKey.USER_DATA)        editPref?.commit()        mContext.sharedPreferences = null        val intent = Intent(mContext, DottysSplashActivity::class.java)        ///**/ intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)        mContext.startActivity(intent)        // mContext.finish()    }    fun setBackButton() {        backButton = actionBarView?.findViewById<ImageButton>(R.id.back_image_button)        backButton?.setImageResource(R.drawable.back_icon_black)        backButton?.scaleY = 0.5f        backButton?.scaleX = 0.5f    }    fun hideCustomKeyboard() {        val imm: InputMethodManager =            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)    }    fun showLoader() {        if (progressBar?.visibility == View.VISIBLE) {            return        }        progressBar = this.findViewById<ProgressBar>(R.id.progress_loader)        progressBar?.visibility = View.VISIBLE        window.setFlags(            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE        )    }    fun hideLoader() {        progressBar = this.findViewById<ProgressBar>(R.id.progress_loader)        progressBar?.visibility = View.INVISIBLE        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)    }}/* BASE DELEGATES*/interface DottysBeaconStatusDelegate {    fun onBeaconsChange(beaconsData: ArrayList<DottysBeacon>)    fun onDistanceToStoreChange(distance: Double)}class DottysBeaconStatusObserver(lisener: DottysBeaconStatusDelegate) {    var listOfBeacons: ArrayList<DottysBeacon> by Delegates.observable(        initialValue = ArrayList<DottysBeacon>(),        onChange = { _, _, new -> lisener.onBeaconsChange(new) })    var distanceToNearStore: Double by Delegates.observable(        initialValue = (999999.0).toDouble(),        onChange = { _, _, new -> lisener.onDistanceToStoreChange(new) })}