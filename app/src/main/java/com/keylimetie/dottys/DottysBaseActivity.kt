package com.keylimetie.dottysimport AltBeaconHandlerimport android.Manifestimport android.annotation.SuppressLintimport android.app.ActionBarimport android.app.AlertDialogimport android.app.NotificationChannelimport android.app.NotificationManagerimport android.bluetooth.BluetoothAdapterimport android.bluetooth.BluetoothManagerimport android.bluetooth.le.ScanCallbackimport android.bluetooth.le.ScanResultimport android.content.ActivityNotFoundExceptionimport android.content.Contextimport android.content.Intentimport android.content.SharedPreferencesimport android.content.pm.ActivityInfoimport android.content.pm.PackageManager.PERMISSION_GRANTEDimport android.content.res.Configurationimport android.content.res.Resourcesimport android.graphics.Bitmapimport android.graphics.Pointimport android.graphics.drawable.ColorDrawableimport android.location.Locationimport android.os.Bundleimport android.util.DisplayMetricsimport android.util.Logimport android.view.Viewimport android.view.WindowManagerimport android.view.inputmethod.InputMethodManagerimport android.widget.ImageButtonimport android.widget.ProgressBarimport androidx.appcompat.app.AppCompatActivityimport androidx.core.app.ActivityCompatimport androidx.core.app.NotificationManagerCompatimport androidx.core.content.PermissionCheckerimport androidx.lifecycle.LifecycleObserverimport com.dottysrewards.dottys.service.VolleyServiceimport com.estimote.coresdk.common.requirements.SystemRequirementsCheckerimport com.keylimetie.dottys.beacon_service.*import com.keylimetie.dottys.models.DottysGlobalDataModelimport com.keylimetie.dottys.models.DottysRewardsModelimport com.keylimetie.dottys.splash.DottysSplashActivityimport com.keylimetie.dottys.ui.dashboard.DashboardFragmentimport com.keylimetie.dottys.ui.dashboard.DashboardViewModelimport com.keylimetie.dottys.ui.dashboard.models.*import com.keylimetie.dottys.ui.drawing.models.DottysDrawingRewardsModelimport com.keylimetie.dottys.ui.locations.*import com.keylimetie.dottys.utils.geofence.DottysGeofenceimport com.keylimetie.dottys.utils.getLocationimport com.keylimetie.dottys.utils.isProbablyAnEmulatorimport com.onesignal.*import org.altbeacon.beacon.*import java.text.SimpleDateFormatimport java.util.*import kotlin.collections.ArrayListimport kotlin.properties.Delegatesenum class PreferenceTypeKey {    USER_DATA, REWARDS, GLOBAL_DATA, LOCATIONS, DOTTYS_USER_LOCATION, TOKEN, BEACONS_LIST, BEACON_AT_CONECTION, DRAWINGS, PUSH_NOTIFICATION, BANNERS, PREFS_DATA, LOGS_EVENT}object EstimoteCredentials {    const val APP_ID =        "appsupport-icsdottys-com-s-00i"//dottys-rewards-aa2"// "appsupport-icsdottys-com-s-00i"    const val APP_TOKEN = "3279b08f8557362e2d6bb7901b83ed17" //"d32ab3c4fb178ee04defe33d53ac9932"}@Suppress("DEPRECATION")open class DottysBaseActivity : AppCompatActivity(), DottysBeaconDelegates, OSPermissionObserver,    OSSubscriptionObserver, LifecycleObserver, DottysLocationChangeDelegates,    com.keylimetie.dottys.ui.locations.DottysLocationDelegates, BeaconEventDelegate,    BeaconHandlerDelegate    //,BeaconConsumer, MonitorNotifier, RangeNotifier{    /**     * BASE VARIABLES     */    //region    var userPictureBM: Bitmap? = null    private var geofenceActivity: DottysGeofence? = null    var backButton: ImageButton? = null    var sharedPreferences: SharedPreferences? = null    var editor: SharedPreferences.Editor? = null    var actionBarView: View? = null    var isUpdatingLocation: Boolean? = null    var baseUrl: String? = null    var progressBar: ProgressBar? = null    val displayMetrics = DisplayMetrics()    var gpsTracker: GpsTracker? = null    var beaconsStatusObserver: DottysBeaconStatusObserver? = null    var mainNavigationActivity: DottysMainNavigationActivity? = null    var dashboardFragment: DashboardFragment? = null    var mapFragmentBase: DottysLocationsMapFragment? = null    var beaconService: BeaconsHandler? = null     var locationsBaseViewModel: LocationsViewModel? = null    var lastKnownLatitudeGps = 0.0    var lastKnownLongitudeGps = 0.0    val screenSize = Point()    val screenWidth = Resources.getSystem().displayMetrics.widthPixels    val screenHeigth = Resources.getSystem().displayMetrics.heightPixels    //endregion    /**     * START LIFECICLE BASE ACTIVITY     */    override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)        VolleyService.initialize(this)        OneSignal.addPermissionObserver(this)        OneSignal.addSubscriptionObserver(this)        // baseUrl = this.resources.getString(R.string.url_base_development)        baseUrl = this.resources.getString(R.string.url_base_production)        progressBar = findViewById(R.id.progress_loader)        //hideLoader(this)        sharedPreferences = this.getSharedPreferences(            PreferenceTypeKey.USER_DATA.name,            Context.MODE_PRIVATE        )        //firebaseAnalytics = Firebase.analytics        println(getUserPreference().token)        // Logging set to help debug issues, remove before releasing your app.        //   OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);        // OneSignal Initialization        OneSignal.startInit(this)            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)            .unsubscribeWhenNotificationsAreDisabled(true)            .init()        /* TRANSITION ANIM */        overridePendingTransition(            R.anim.slide_in_left, R.anim.slide_out_left        )        initGeaofences()        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {            display?.getSize(screenSize)        }    }    override fun onStart() {        super.onStart()        if (gpsTracker == null) {            gpsTracker = GpsTracker(this)            gpsTracker?.getLocation(gpsTracker!!)            gpsTracker?.locationObserver = DottysLocationObserver(this)        }        //EstimoteSDK.initialize(this, EstimoteCredentials.APP_ID, EstimoteCredentials.APP_TOKEN)    }    override fun onResume() {        super.onResume()        if (getUserPreference() != null || progressBar?.visibility != 0) {            if (!(getUserNearsLocations().locations.isNullOrEmpty()) &&                getUserNearsLocations().locations?.first()?.distance ?: return < 0.5            ) {                initEstimoteBeaconManager()            }            else {                beaconService?.let { it -> it.beaconManager?.let { it.disconnect() } }            }        }    }    override fun onWindowFocusChanged(hasFocus: Boolean) {        super.onWindowFocusChanged(hasFocus)        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT        this.window.setFlags(            WindowManager.LayoutParams.FLAG_FULLSCREEN,            WindowManager.LayoutParams.FLAG_FULLSCREEN        )    }    private fun initGeaofences() {        if (getUserNearsLocations().locations.isNullOrEmpty()) {            return        }        val distance = getUserNearsLocations().locations?.first()?.distance ?: 0.0        if (geofenceActivity == null && distance < 20000) {            Log.e("GEOFENCE", "GEAONCES TREATH INIT")            geofenceActivity = DottysGeofence(this)        }    }    private val leScanCallback = object : ScanCallback() {        override fun onScanResult(callbackType: Int, result: ScanResult) {            super.onScanResult(callbackType, result)            Log.i("DeviceListActivity","onScanResult: ${result.device.address} - ${result.device.uuids}")            Log.i("device ", "kskd   " + result.getRssi())        }        override fun onBatchScanResults(results: MutableList<ScanResult>?) {            super.onBatchScanResults(results)            Log.e("DeviceListActivity","onBatchScanResults:${results.toString()}")        }        override fun onScanFailed(errorCode: Int) {            super.onScanFailed(errorCode)            Log.i("DeviceListActivity", "onScanFailed: $errorCode")        }    }    @SuppressLint("RestrictedApi")    internal fun initEstimoteBeaconManager() {        if (isProbablyAnEmulator() || getCurrentToken().isNullOrEmpty()) {            return        }        if (SystemRequirementsChecker.checkWithDefaultDialogs(this) && this is DottysMainNavigationActivity ) { // && beaconService == null                beaconService = BeaconsHandler( this,BeaconHandlerObserver(this))            try {                beaconService?.handlerData?.removeCallbacks(beaconService?.runnable!!)                beaconService?.handlerData?.removeCallbacksAndMessages(null)            } catch (e:Exception) {Log.e("REMOVE HANDLER ERROR", "${e.message}2")}                beaconService?.initBeaconManager()/*            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager            val bluetoothAdapter = bluetoothManager.adapter            val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)                startActivityForResult(enableBtIntent, 1001001)            }//            when (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {//                else -> requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)//            }            bluetoothLeScanner.startScan(leScanCallback)*/        }    }/*    protected val TAG = "MonitoringActivity"    private var beaconManager: BeaconManager? = null    override fun onBeaconServiceConnect() {       // beaconManager?.removeAllMonitorNotifiers()        beaconManager?.addMonitorNotifier(this)        beaconManager?.addRangeNotifier(this)        val region = Region(            "5f8db83f95f3cefed47e5df7",            Identifier.parse("3D085F5D-072E-D57D-62FE-50DADE7F2CB6"),            Identifier.fromInt(215),            Identifier.fromInt(1)        )        beaconManager?.startMonitoringBeaconsInRegion(region)    }    fun  initAltBeacon() {        beaconManager = BeaconManager.getInstanceForApplication(this)        // To detect proprietary beacons, you must add a line like below corresponding to your beacon        // type.  Do a web search for "setBeaconLayout" to get the proper expression.        // beaconManager.getBeaconParsers().add(new BeaconParser().        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));        beaconManager?.foregroundBetweenScanPeriod = 5000        beaconManager?.backgroundBetweenScanPeriod = 5000        beaconManager?.bind(this)    }    override fun didEnterRegion(p0: Region?) {        Log.e("ALTBEACON", "⚪ ENTER")    }    override fun didExitRegion(p0: Region?) {        Log.e("ALTBEACON", "⚪️  EXIT")    }    override fun didDetermineStateForRegion(p0: Int, p1: Region?) {        Log.e("ALTBEACON", "⚪ STATE ${p1?.uniqueId}️")    }    override fun didRangeBeaconsInRegion(p0: MutableCollection<Beacon>?, p1: Region?) {        Log.e("ALTBEACON", "⚪️ RANGE")    } */    override fun finish() {        super.finish()        overridePendingTransition(            R.anim.slide_out_left, R.anim.slide_out_right        )    }    override fun onOSPermissionChanged(stateChanges: OSPermissionStateChanges) {        if (stateChanges.from.enabled &&            !stateChanges.to.enabled        ) {            AlertDialog.Builder(this).setMessage("Notification Disable").show()        }        Log.i("Debug", "onOSPermissionChanged: $stateChanges")    }    override fun onOSSubscriptionChanged(stateChanges: OSSubscriptionStateChanges?) {        if (!(stateChanges?.from?.subscribed ?: return) &&            stateChanges.to?.subscribed ?: return        ) {            AlertDialog.Builder(this)                .setMessage("You've successfully subscribed to push notifications!")                .show()            stateChanges.to.userId        }        Log.i("Debug", "onOSSubscriptionChanged: " + stateChanges)    }    override fun onConfigurationChanged(newConfig: Configuration) {        super.onConfigurationChanged(newConfig)        // Checks whether a hardware keyboard is available//        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {//            DottysBaseActivity().showSnackBarMessage(this, "keyboard visible")//        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {//            DottysBaseActivity().showSnackBarMessage(this, "keyboard hidden")//        }    }    /**     * BEACON RECORDED     */    override fun getBeaconRecorded(beaconRecorded: DottysBeacon) {        responseBEacoManagaer(beaconRecorded)    }    private fun responseBEacoManagaer(beaconRecorded: DottysBeacon) {        var beaconAux = beaconRecorded        var beaconsTemp = ArrayList<DottysBeacon>()        if (getBeaconStatus()?.beaconArray?.size ?: 0 > 0) {            beaconsTemp = (getBeaconStatus()!!).beaconArray ?: ArrayList<DottysBeacon>()        }        var beaconsTemp2 = beaconsTemp        for (x in 0 until beaconsTemp.size) {            //  beaconsTemp2[x].id = beaconAux.beaconIdentifier            if (beaconsTemp[x].id == beaconRecorded.beaconIdentifier ||                beaconsTemp[x].beaconIdentifier == beaconRecorded.beaconIdentifier            ) {                beaconAux.isConected = beaconRecorded.eventType?.equals("ENTER") == true                beaconsTemp2[x] = beaconAux            } else {                beaconsTemp2[x] = beaconsTemp[x]            }        }        saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,            DottysBeaconArray(beaconsTemp2).toJson())        if (dashboardFragment == null){            try {                if  ((this as DottysBaseActivity).supportFragmentManager.fragments.first() is DashboardFragment) {                    dashboardFragment = (this as DottysBaseActivity).supportFragmentManager.fragments.first() as DashboardFragment                }            } catch (e: Exception) {                Log.e("CAST TO DASHBOARD", e.message ?: "")            }        }        dashboardFragment?.dashboardViewModel?.initAnalitycsItems(DottysBeaconArray(beaconsTemp2).beaconArray)        DottysBaseActivity().showSnackBarMessage(this,            "Has ${beaconRecorded.eventType} to ${beaconRecorded.beaconType} Beacon")    }    /**     * BACKGROUND AND FOREGROEUND LOCATION LISENER     */    override fun onLocationChangeHandler(locationGps: Location?) {        Log.d("LOCATION CHANGE",            "**  BASE ACTIVITY ${locationGps?.latitude} //  ${locationGps?.longitude}")        if (locationsBaseViewModel == null) {            locationsBaseViewModel = LocationsViewModel(this)        }        locationsBaseViewModel?.getLocationsDottysRequest(this,            locationGps?.latitude.toString(),            locationGps?.longitude.toString())        locationsBaseViewModel?.locationDataObserver = DottysLocationStoresObserver(this)        if (mapFragmentBase != null) {            mapFragmentBase?.updateMarker()            val fragmentActivity =                locationsBaseViewModel?.locationFragment?.activity as DottysMainNavigationActivity            locationsBaseViewModel?.initLocationView(locationsBaseViewModel?.locationFragment                ?: return, fragmentActivity, locationsBaseViewModel?.rootView ?: return)        }    }    /**     * DOTTYS LOCATION LISENER     */    override fun getStoresLocation(locations: DottysLocationsStoresModel) {        editor = sharedPreferences!!.edit()        if (locations.locations.isNullOrEmpty()) {            return        }        val distance = locations.locations?.first()?.distance ?: return        Log.e("STORES HAS UPDATED", "Distance to near store $distance")        try{        DashboardViewModel(this as DottysMainNavigationActivity).getBeaconList(this , locations.locations?.first()?.storeNumber.toString()) }        catch (e:Exception) { Log.e("ERROR", "AT GET BEACONS ON BASE ${e.message}")}        //  dashboardFragment?.dashboardViewModel?.initAnalitycsItems(getBeaconStatus()?.beaconArray)//        if (geofenceActivity == null && distance < 20000) {//            geofenceActivity = DottysGeofence(this)//        }    }    /**     * COLLAPSED ITEMS - UNUSEFUL     */    override fun allItemsCollapse(isColappse: Boolean) {}    /**     * ACTIONBAR SETTING     */    @SuppressLint("WrongConstant")    fun actionBarSetting(actionBar: androidx.appcompat.app.ActionBar, coloBackground: ColorDrawable) {        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM        actionBar.setDisplayShowCustomEnabled(true)        actionBar.setCustomView(R.layout.custom_action_bar)        actionBar.elevation = 1F        actionBarView = actionBar.customView        actionBar.setBackgroundDrawable(coloBackground)        backButton = actionBarView?.findViewById<ImageButton>(R.id.back_image_button)        backButton?.setOnClickListener {            finish()        }    }    fun enablePushNotification(isEnable: Boolean){        editor = sharedPreferences!!.edit()        editor!!.putBoolean(PreferenceTypeKey.PUSH_NOTIFICATION.name, isEnable)        editor!!.commit()        if (isEnable) {            NotificationManagerCompat.from(this).cancelAll()        } else {            val notificationChannel = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {                NotificationChannel("channel_01",                    this.resources.getString(R.string.app_name),                    NotificationManager.IMPORTANCE_DEFAULT)            } else {                return            }            NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel)        }    }    fun sendMailToSupport(context: DottysBaseActivity) {        val i = Intent(Intent.ACTION_SEND)        i.type = "message/rfc822"        i.putExtra(Intent.EXTRA_EMAIL, arrayOf("appsupport@playspinwinbrands.com"))        try {            context?.startActivity(Intent.createChooser(i, "Send mail..."))        } catch (ex: ActivityNotFoundException) {            DottysBaseActivity().showSnackBarMessage(this,                "There are no email clients installed.")        }    }//    fun isPushNotificationEnabled():Boolean {//        if (sharedPreferences == null) {//            sharedPreferences = this.getSharedPreferences(//                PreferenceTypeKey.USER_DATA.name,//                Context.MODE_PRIVATE//            )//        }////          return   NotificationManagerCompat.from(this).areNotificationsEnabled()//sharedPreferences!!.getBoolean(PreferenceTypeKey.PUSH_NOTIFICATION.name, false)//    }    /**     * REQUEST LOCATION PERMOSION     * */    open fun requestLocation(gpsTracker: GpsTracker?, activity: AppCompatActivity?) {        try {            if (checkSelfPermission(                    Manifest.permission.ACCESS_FINE_LOCATION                ) !== PERMISSION_GRANTED            ) {                ActivityCompat.requestPermissions(                    activity!!,                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),                    101                )            }        } catch (e: java.lang.Exception) {            e.printStackTrace()        }//        gpsTracker?.let { activity?.let { it1 -> getLocation(it, it1) } }    }    /**     * GET BEACON FOR CURRENT STORE     * */    fun getDottysBeaconsList(): ArrayList<DottysBeacon>? {        sharedPreferences =  getSharedPreferences(PreferenceTypeKey.PREFS_DATA.name,            Context.MODE_PRIVATE)        val textoDate = sharedPreferences!!.getString(PreferenceTypeKey.BEACONS_LIST.name, "")        return try {            var person: DottysBeaconsModel =                DottysBeaconsModel.fromJson(                    textoDate!!                )            try {                var beaconAtStore =                    person.beacons//.beacons?.filter { it.location?.storeNumber ?: 0 == getUserNearsLocations().locations?.first()?.storeNumber ?: 0 }                            as ArrayList                //saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,                //    DottysBeaconArray(beaconAtStore).toJson())                beaconAtStore            } catch (e: Exception) {                println(e)                null            }        } catch (e: Exception) {            println(e)            null        }    }    /**     * GET CURRENT TOKEN     * */    fun getCurrentToken(): String? {         sharedPreferences =  getSharedPreferences(PreferenceTypeKey.PREFS_DATA.name,            Context.MODE_PRIVATE)        return try {             sharedPreferences!!.getString(PreferenceTypeKey.TOKEN.name, null)        } catch (e: Exception) {            println(e)            null        }    }    /**     * GET BEAON STATUS     * */    fun getBeaconStatus(): DottysBeaconArray? {        if (sharedPreferences == null) {            sharedPreferences =  getSharedPreferences(                PreferenceTypeKey.PREFS_DATA.name,                0            )        }        val textoDate =            sharedPreferences!!.getString(PreferenceTypeKey.BEACON_AT_CONECTION.name, "")        return try {            var person: DottysBeaconArray? =                textoDate?.let { DottysBeaconArray.fromJson(it) }            person        } catch (e: Exception) {            println(e)            null        }    }    /**     * GET CURRENT USER     */    fun getUserPreference(): DottysLoginResponseModel {        if (sharedPreferences == null) {            sharedPreferences =  getSharedPreferences(                PreferenceTypeKey.PREFS_DATA.name,                0            )        }        val userData = sharedPreferences!!.getString(PreferenceTypeKey.USER_DATA.name, "")        return try {            var currentUserData: DottysLoginResponseModel =                DottysLoginResponseModel.fromJson(                    userData!!                )            currentUserData        } catch (e: Exception) {            println(e)            DottysLoginResponseModel()        }    }    /**     * GET GLOBAL DATA     */    fun getGlobalData(): DottysGlobalDataModel {        sharedPreferences =  getSharedPreferences(            PreferenceTypeKey.PREFS_DATA.name,            0        )        val globalInfo = sharedPreferences!!.getString(PreferenceTypeKey.GLOBAL_DATA.name, "")        return try {            var globalData: DottysGlobalDataModel =                DottysGlobalDataModel.fromJson(                    globalInfo!!                )            globalData        } catch (e: Exception) {            println(e)            DottysGlobalDataModel()        }    }    /**     * GET DRAWINGS     */    fun getDrawings(): DottysDrawingRewardsModel {        sharedPreferences =  getSharedPreferences(            PreferenceTypeKey.PREFS_DATA.name,            0        )        val drawingData = sharedPreferences!!.getString(PreferenceTypeKey.DRAWINGS.name, "")        return try {            var drawingLocations: DottysDrawingRewardsModel =                DottysDrawingRewardsModel.fromJson(                    drawingData!!                )            drawingLocations        } catch (e: Exception) {            println(e)            DottysDrawingRewardsModel()        }    }    /**     * GET ALL DOTTYS LOCATIONS     */    fun getUserNearsLocations(): DottysLocationsStoresModel {        sharedPreferences =  getSharedPreferences(            PreferenceTypeKey.PREFS_DATA.name,            0        )        val nearStoreData = sharedPreferences?.getString(PreferenceTypeKey.LOCATIONS.name, "")        return try {            var nearStoreLocation: DottysLocationsStoresModel =                DottysLocationsStoresModel.fromLocationJson(                    nearStoreData!!                )            nearStoreLocation        } catch (e: Exception) {            println(e)            DottysLocationsStoresModel()        }    }    /**     * GET REWARDS AT SESSION     */    fun getRewardsAtSession(): DottysRewardsModel {            sharedPreferences =  getSharedPreferences(                PreferenceTypeKey.PREFS_DATA.name,                0            )        val rewardsDAta = sharedPreferences!!.getString(PreferenceTypeKey.REWARDS.name, "")        return try {            var rewards: DottysRewardsModel =                DottysRewardsModel.fromJson(                    rewardsDAta!!                )            rewards        } catch (e: Exception) {            println(e)            DottysRewardsModel()        }    }    /**     * GET STORED BANNERS     */    fun getBannersStored(): DottysBannerModel {        if (sharedPreferences == null) {            sharedPreferences =  getSharedPreferences(                PreferenceTypeKey.PREFS_DATA.name,                0            )        }        val bannerData = sharedPreferences!!.getString(PreferenceTypeKey.BANNERS.name, "")        return try {            var banners: DottysBannerModel =                DottysBannerModel.fromJson(                    bannerData!!                )            banners        } catch (e: Exception) {            println(e)            DottysBannerModel()        }    }    /**     * GET LOGS EVENTS     */    fun getLogsEvent(): BeaconConnectionsUpdatesModel? {        sharedPreferences = getSharedPreferences(            PreferenceTypeKey.PREFS_DATA.name,            Context.MODE_PRIVATE        )        val dataJsonAsString =            sharedPreferences!!.getString(PreferenceTypeKey.LOGS_EVENT.name, "")        return try {            val stores: BeaconConnectionsUpdatesModel? =                dataJsonAsString?.let { BeaconConnectionsUpdatesModel.fromJson(it) }            stores        } catch (e: Exception) {            println(e)            null        }    }    fun updateBeaconRegisters(        update: String?,        beacon: DottysBeaconResponseModel?    ): ArrayList<String> {        val updates = getLogsEvent()?.updates        if (update.isNullOrEmpty()) {            return if (updates.isNullOrEmpty()) {                ArrayList<String>()            } else {                updates            }        }        return if (updates.isNullOrEmpty()) {            saveDataPreference(                PreferenceTypeKey.LOGS_EVENT,                BeaconConnectionsUpdatesModel(beacon, arrayListOf(update)).toJson()            )            arrayListOf(update)        } else {            updates.add(update)            saveDataPreference(                PreferenceTypeKey.LOGS_EVENT,                BeaconConnectionsUpdatesModel(beacon, updates).toJson()            )            updates        }    }    /**     * BASE PUBLIC FUNTIONS     */    fun finishSession(mContext: DottysBaseActivity) {        // DottysBaseActivity().showSnackBarMessage(this,mContext, "User Logout")        val context = mContext as DottysMainNavigationActivity        val editPref = context.sharedPreferences?.edit()        mContext.gpsTracker?.stopUsingGPS()        /**mContext.beaconService?.beaconManager?.disconnect()        mContext.beaconService?.removeBeaconsLisener()*/        context.removeReferenceData(PreferenceTypeKey.USER_DATA)        context.removeReferenceData(PreferenceTypeKey.BEACON_AT_CONECTION)        context.removeReferenceData(PreferenceTypeKey.LOGS_EVENT)        context.removeReferenceData(PreferenceTypeKey.BEACONS_LIST)        editPref?.clear()        editPref?.apply()        editPref?.commit()        mContext.sharedPreferences = null        val intent = Intent(context, DottysSplashActivity::class.java)        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)        mContext.startActivity(intent)        context.finish()    }    fun setBackButton() {        backButton = actionBarView?.findViewById<ImageButton>(R.id.back_image_button)        backButton?.setImageResource(R.drawable.back_icon_black)        backButton?.scaleY = 0.5f        backButton?.scaleX = 0.5f    }    fun hideCustomKeyboard(context: AppCompatActivity) {        val imm: InputMethodManager =            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)    }    fun showLoader() {        if (progressBar?.visibility == View.VISIBLE) {            return        }        progressBar = this.findViewById<ProgressBar>(R.id.progress_loader)        progressBar?.visibility = View.VISIBLE        window.setFlags(            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE        )    }    fun hideLoader() {        progressBar = this.findViewById<ProgressBar>(R.id.progress_loader)        progressBar?.visibility = View.INVISIBLE        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)    }    override fun onBeaconAtNearLocationRetrieved(beaconData: DottysBeaconsModel) {        TODO("Not yet implemented")    }    override fun onBeaconRecorded(beaconEvent: DottysBeaconResponseModel) {        //saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION, beaconEvent.toJson())        Log.e(            "BEACON RECORD EVENT",            "\n*******************\n      ${beaconEvent.eventType} at ${beaconEvent.minor}\n*******************"        )       val beaconRecorded = beaconEvent.castToBeaconEvent()                beaconRecorded.isConected =  beaconEvent.eventType == BeaconEventType.ENTER                beaconRecorded.isRegistered =  beaconEvent.eventType == BeaconEventType.ENTER                beaconRecorded.expiration =  0               try {                   beaconService?.beaconsConnected()?.indexOf(beaconService?.beaconsConnected()?.first { it.minor == beaconEvent.minor })                       ?.let {                           var beaconsToRecord = beaconService?.beaconsConnected()                           beaconsToRecord?.set(beaconsToRecord.indexOf(beaconsToRecord.first { it.minor == beaconEvent.minor }),                               beaconRecorded                           )                           val beaconsAtConection = DottysBeaconArray()                           beaconsAtConection.beaconArray = beaconsToRecord                           saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION, beaconsAtConection.toJson())                         //  beaconService?.beaconsConnected()                       //                           beaconService?.beaconsConnected()?.set(//                               it,//                               beaconRecorded//                           )                       }               } catch (e:Exception) { Log.e("EVENT BEACON RECORDED", "${e.message}")}        val sdf = SimpleDateFormat("dd/MM hh:mm:ss")        val currentDate = sdf.format(Date())        updateBeaconRegisters(            "$currentDate: Last Update ${beaconEvent.eventType} at minor ${beaconEvent.minor}",            if (beaconEvent.eventType == BeaconEventType.ENTER) {                beaconEvent            } else {                null            }        )        if (this is DottysMainNavigationActivity) {            this.reloadLogsList()           this.initAnalyticsItems(beaconService?.beaconsConnected())        }    }    override fun onBeaconViewChange(beaconsData: ArrayList<DottysBeacon>) {//        if (this is DottysMainNavigationActivity) {//            this.initAnalyticsItems(getBeaconsFilled(beaconsData))//        }        val beaconsAtConection = DottysBeaconArray()        beaconsAtConection.beaconArray =            getBeaconsFilled(beaconsData)//beaconsData.filter { it.isConected == true } as ArrayList<DottysBeacon>        saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION, beaconsAtConection.toJson())        if (this is DottysMainNavigationActivity) {            this.initAnalyticsItems(getBeaconsFilled(beaconsData))        }        beaconService?.taskCounter = 0        Log.i("BEACON 🔵 CHANGE",            "🟣VIEW HAS BEEN UPDATED 🔵 TASK COUNTER ♈️ ${beaconService?.taskCounter}"        )    }    fun getBeaconsFilled(conectedBeacons: ArrayList<DottysBeacon>): ArrayList<DottysBeacon> {        val beaconConnected = ArrayList<DottysBeacon>()        for (beacon in this.getDottysBeaconsList() ?: return ArrayList<DottysBeacon>()) {            if (conectedBeacons.none { it.minor == beacon.minor }) {                beaconConnected.add(beacon)            } else {                beaconConnected.add(conectedBeacons.first { it.minor == beacon.minor })            }        }        return beaconConnected    }}/* BASE DELEGATES*/interface DottysBeaconStatusDelegate {   // fun onBeaconsChange(beaconsData: ArrayList<DottysBeacon>)    fun onDistanceToStoreChange(distance: Double)}class DottysBeaconStatusObserver(lisener: DottysBeaconStatusDelegate) {//  //  var listOfBeacons: ArrayList<DottysBeacon> by Delegates.observable(//        initialValue = ArrayList<DottysBeacon>(),//        onChange = { _, _, new -> lisener.onBeaconsChange(new) })    var distanceToNearStore: Double by Delegates.observable(        initialValue = (999999.0).toDouble(),        onChange = { _, _, new -> lisener.onDistanceToStoreChange(new) })}fun DottysBaseActivity.isPushNotificationEnable():Boolean{    if (sharedPreferences == null) {        sharedPreferences = this.getSharedPreferences(            PreferenceTypeKey.PUSH_NOTIFICATION.name,            Context.MODE_PRIVATE        )    }    return sharedPreferences!!.getBoolean(PreferenceTypeKey.PUSH_NOTIFICATION.name, true)}/** * SAVE PREFERENCE DATA * */fun DottysBaseActivity.saveDataPreference(keyPreference: PreferenceTypeKey, jsonData: String?) {    if(keyPreference != PreferenceTypeKey.PUSH_NOTIFICATION) {        sharedPreferences = getSharedPreferences(            PreferenceTypeKey.PREFS_DATA.name,            0        )        editor = sharedPreferences!!.edit()        editor!!.putString(keyPreference.name, jsonData)        editor!!.commit()        editor!!.apply()    }    when (keyPreference) {//        PreferenceTypeKey.BEACONS_LIST -> {//            if (getBeaconStatus()?.beaconArray.isNullOrEmpty() ||//                getBeaconStatus()?.beaconArray?.first()?.major != getDottysBeaconsList()?.first()?.major//            ) {//                saveDataPreference(PreferenceTypeKey.BEACONS_LIST,//                    DottysBeaconArray(getDottysBeaconsList()).toJson())//            }//        }        PreferenceTypeKey.PUSH_NOTIFICATION -> {            editor = sharedPreferences!!.edit()            if (jsonData != null) {                editor!!.putBoolean(keyPreference.name, jsonData.toBoolean())            }            editor!!.commit()            editor!!.apply()        }        else -> {            return        }    }}/** * REMOVE PREFERENCE DATA */fun DottysBaseActivity.removeReferenceData(keyPreference: PreferenceTypeKey) {    sharedPreferences =  getSharedPreferences(        PreferenceTypeKey.PREFS_DATA.name,        0    )    editor = sharedPreferences!!.edit()    editor!!.remove(keyPreference.name)    editor!!.commit()    editor!!.apply()}