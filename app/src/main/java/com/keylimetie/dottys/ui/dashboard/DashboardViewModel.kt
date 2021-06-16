package com.keylimetie.dottys.ui.dashboard


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.icu.math.BigDecimal
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.ViewPager
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.keylimetie.dottys.*
import com.keylimetie.dottys.R.id
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.models.Monthly
import com.keylimetie.dottys.redeem.DottysRedeemRewardsActivity
import com.keylimetie.dottys.redeem.DottysRewardRedeemedActivity
import com.keylimetie.dottys.ui.dashboard.models.*
import com.keylimetie.dottys.ui.drawing.DottysDrawingDelegates
import com.keylimetie.dottys.ui.drawing.DottysDrawingObserver
import com.keylimetie.dottys.ui.drawing.DrawingViewModel
import com.keylimetie.dottys.ui.drawing.RewardsSegment
import com.keylimetie.dottys.ui.drawing.models.DottysDrawing
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingUserModel
import com.keylimetie.dottys.ui.locations.showSnackBarMessage
import com.keylimetie.dottys.utils.castQRCodeBitmap
import com.keylimetie.dottys.utils.md5
import com.keylimetie.dottys.utils.stringGetYear
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.Throws
import kotlin.properties.Delegates


class DashboardViewModel(private val mainActivity: DottysMainNavigationActivity?) : ViewModel(),
    View.OnClickListener, DottysDrawingDelegates {
    private var pager: ViewPager? = null

    /*DASHBOARD ITEMS */
    private var nameDashboard: TextView? = null
    private var locationDashboard: TextView? = null
    private var pointsEarned: TextView? = null
    private var cashRewards: TextView? = null
    private var profilePhantonButton: Button? = null

    /*LIST BUTTONS*/
    private var leftRollPagerButton: Button? = null
    private var rightRollPagerButton: Button? = null

    /*BASDGE BUTTON ITEMS*/
    private var backgroundLabelBadge: TextView? = null
    private var counterLabelBadge: TextView? = null
    var userCurrentUserDataObserver: DottysCurrentUserObserver? = null
    var dashboardView: View? = null

    private var floatingAnalicsView: ConstraintLayout? = null
    private var mainFragmentActivity: DottysMainNavigationActivity? = mainActivity
    var fragmentDashBoard: DashboardFragment? = null
    var drawingBadgeCounter: Int? = 0
    var adapter: DashboardPagerAdapter? = null


    private fun setItemsDashBoard(
        rootView: View,
        activity: DottysMainNavigationActivity,
    ) {
        mainFragmentActivity = activity
        nameDashboard = rootView.findViewById<TextView>(id.profile_name_dashboard)
        pointsEarned = rootView.findViewById<TextView>(id.points_earned_textview)
        cashRewards = rootView.findViewById<TextView>(id.cash_rewards_textview)
        profilePhantonButton = rootView.findViewById<Button>(id.phanton_profile_button)


        backgroundLabelBadge = rootView.findViewById<TextView>(id.background_badge)
        counterLabelBadge = rootView.findViewById<TextView>(id.badge_counter)
        floatingAnalicsView = rootView.findViewById<ConstraintLayout>(id.analitycs_floating_view)
        floatingAnalicsView?.setOnClickListener(this)
        profilePhantonButton?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            id.phanton_profile_button -> {
                initQRCodeView()
            }
            id.analitycs_floating_view, id.close_analytics_buttom -> {
                mainFragmentActivity?.let { hideAnalitycsView(it) }
            }

            id.send_to_support_button -> {
                mainActivity?.sendMailToSupport(mainActivity)
            }
            id.redeem_rewards_button -> {
                val intent = Intent(mainFragmentActivity, DottysRedeemRewardsActivity::class.java)
                intent.putExtra(
                    "REDEEM_REWARDS",
                    userCurrentUserDataObserver?.rewardsAtSession?.toJson().toString()
                )
                mainFragmentActivity?.startActivity(intent)
            }
            id.convert_points_dashboard_button -> {
//                mainFragmentActivity?.segmentSelect = RewardsSegment.DRAWING_ENTRIES
//                mainFragmentActivity?.intent?.putExtra("IS_DASHBOARD_BUTTON", true)
//                mainFragmentActivity?.controller?.navigate(
//                    R.id.nav_rewards,
//                    mainFragmentActivity?.intent?.extras
//                )
                mainFragmentActivity?.segmentSelect = RewardsSegment.DRAWING_ENTRIES
                mainFragmentActivity?.intent?.putExtra("IS_DASHBOARD_BUTTON", true)
                mainFragmentActivity?.controller?.navigate(
                    R.id.nav_drawing,
                    mainFragmentActivity?.intent?.extras
                )
            }
            else -> {
                return
            }
        }
    }

    fun initDashboardViewSetting(
        fragment: DashboardFragment,
        mContext: DottysMainNavigationActivity,
        dashboardView: View?,
    ) {
        this.dashboardView = dashboardView
        userCurrentUserDataObserver = DottysCurrentUserObserver(fragment)
        setItemsDashBoard(dashboardView ?: return, mContext)
        //  mContext.beaconsStatusObserver = DottysBeaconStatusObserver(fragment)
        mContext.hideLoader()
        fragmentDashBoard = fragment
        getCurrentUserRequest(mContext)
        initDashboardItemsView()
        addProfileImage(mContext, dashboardView,  dashboardView.findViewById<CircleImageView>(id.profile_dashboard_image))

    }

    fun initDashboardItemsView() {
        initDashboardButtons()
        fillItemsAtDashboards(mainFragmentActivity?.getDrawings()) //TODO --> REMOVED ON LAST VERSION
    }

    fun refreshItemsAtView(){
        nameDashboard?.text = mainFragmentActivity?.getUserPreference()?.fullName
        cashRewards?.text = getCashForDrawing()
        pointsEarned?.text = NumberFormat.getIntegerInstance()
            .format(mainFragmentActivity?.getUserPreference()?.points ?: 0.0)
        locationDashboard?.text = addressLocationFotmatted( mainFragmentActivity?.getDrawings() ?: return)
        badgeCounterDrawingManager(drawingBadgeCounter ?: 0)
        hideAnalitycsView(mainFragmentActivity ?: return)
        fragmentDashBoard?.addPagerDashboardImages(mainActivity?.getBannersStored()?.bannerList?.sortedBy { it.priority } ?: return)
        dashboardView?.findViewById<CircleImageView>(id.profile_dashboard_image)?.let {
            addProfileImage(mainFragmentActivity ?: return, dashboardView  ?: return,
                it
            )
        }
    }

    // FILL DATA NAME AND BUTTON DASHBOARD
    fun initDashboardButtons() {
        val redeemButton =
            dashboardView?.findViewById<Button>(id.redeem_rewards_button)
        val convertPointButton =
            dashboardView?.findViewById<Button>(id.convert_points_dashboard_button)
        redeemButton?.setOnClickListener(this)
        convertPointButton?.setOnClickListener(this)
        refreshItemsAtView()
    }

    // FILL DATA POINTS AND BUTTON DASHBOARD
    fun fillItemsAtDashboards(rewardsLoaction: DottysDrawingRewardsModel?) {
        refreshItemsAtView()

        //  TODO INIT VIEW ITEMS


    }


    private fun initQRCodeView(){
        val user = mainFragmentActivity?.getUserPreference() ?: return

        mainFragmentActivity?.findViewById<TextView>(R.id.member_name_TV)?.text = user.fullName
        mainFragmentActivity?.findViewById<TextView>(R.id.member_since_TV)?.text =  "Member Since ${user.createdAt?.stringGetYear()}"
        val qrView = mainFragmentActivity?.findViewById<View>(R.id.qr_view)
        qrView?.animate()?.translationY(mainFragmentActivity?.screenHeigth?.toFloat() ?: 0f)?.setDuration(0)?.start()
        qrView?.animate()?.translationY(0f)?.setDuration(450)?.start()
        qrView?.visibility = View.VISIBLE
        val userId = user.id ?: return
        Picasso.get().load(mainFragmentActivity?.getUserPreference()?.profilePicture).into(mainFragmentActivity?.findViewById<CircleImageView>(id.qr_profile_dashboard_image))
        mainFragmentActivity?.findViewById<ImageView>(id.qr_image_IV)?.setImageBitmap(userId.castQRCodeBitmap())
        mainFragmentActivity?.findViewById<View>(id.close_qr_view_button)?.setOnClickListener {
            qrView?.animate()?.translationY(mainFragmentActivity?.screenHeigth?.toFloat() ?: 0f)?.setDuration(350)?.withEndAction {
                qrView.visibility = View.GONE
            }
        }
    }

    fun badgeCounterDrawingManager(badgeCounter: Int) {
        if (badgeCounter == 0) {
            backgroundLabelBadge?.visibility = View.INVISIBLE
            counterLabelBadge?.visibility = View.INVISIBLE
        } else {
            backgroundLabelBadge?.visibility = View.VISIBLE
            counterLabelBadge?.visibility = View.VISIBLE
            counterLabelBadge?.text = badgeCounter.toString()
        }
    }

    private fun getCashForDrawing(): String {
        val baseActivity = mainFragmentActivity as DottysBaseActivity
        val drawingUser =

            baseActivity.getRewardsAtSession().rewards?.filter { it.redeemed == false }
        var cash = 0
        if (drawingUser != null) {
            for (drawing in drawingUser) {
                if (drawing.value != null) {
                    cash = (cash + drawing.value).toInt()
                }
            }
        }
        return NumberFormat.getIntegerInstance().format(cash)

    }

    private fun addressLocationFotmatted(rewardsLoaction: DottysDrawingRewardsModel): String {
        val staticFirtsText = "Your drawing entries are entered at "
        if (rewardsLoaction.address1?.isEmpty() != false) {
            return ""
        }
        return staticFirtsText + rewardsLoaction.address1 + ", " + rewardsLoaction.city + ", " + rewardsLoaction.state + " " + rewardsLoaction.zip
    }

    private fun addProfileImage(
        mContext: DottysMainNavigationActivity,
        rootView: View,
        imageView: CircleImageView
    ) {
       // val imageView = rootView.findViewById<CircleImageView>(id.profile_dashboard_image)
        if (mainFragmentActivity?.userPictureBM != null){
            imageView.setImageBitmap(mainFragmentActivity?.userPictureBM)

        } else {

            imageView.setImageResource(R.mipmap.default_profile_image)
            if (mainFragmentActivity?.getUserPreference()?.profilePicture?.isNotBlank() == true) {
                Picasso.get()//with(mContext)
                    .load(mainFragmentActivity?.getUserPreference()?.profilePicture)
                    .transform(CircleTransform()).into(imageView)
            }
        }
    }


    /*REQUEST DASHBOARD IMAGE */
    private fun requestDashboardProfileImage(
        mContext: DottysMainNavigationActivity,
        rootView: View, fragment: Fragment,
    ) {
        val imageView = rootView.findViewById<CircleImageView>(id.profile_dashboard_image)
        val email = userCurrentUserDataObserver?.currentUserModel?.email//"mrirenita@gmail.com"
        var url = "https://www.gravatar.com/avatar/" + email?.md5() + "?s=400&r=pg&d=404"
        if (userCurrentUserDataObserver?.currentUserModel?.profilePicture != null) {
            url = userCurrentUserDataObserver?.currentUserModel?.profilePicture ?: ""
        }
        val mQueue = Volley.newRequestQueue(mContext)
        val request = ImageRequest(url,
            Response.Listener { bitmap ->
                imageView.setImageBitmap(bitmap)
                //getLocationDrawing(fragment)
            }, 0, 0, null,
            Response.ErrorListener {
                Log.d("IMAGE PROFILE REQUEST", "Error on urlImage }")
            })
        mQueue.add(request)
    }


    fun getDrawingSummary(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        //mContext.showLoader()
        val jsonArrayRequest =
            object : JsonArrayRequest(Method.GET, mContext.baseUrl + "drawings/summary", null,
                Response.Listener<JSONArray> { response ->
                    mContext.hideLoader()
                    Log.d("DRAWING SUMMARY", response.toString())
                    val drawingSummary: DottysDrawingSumaryModel =
                        DottysDrawingSumaryModel.fromJson(
                            response.toString()
                        )

                    userCurrentUserDataObserver?.dawingSummaryModel = drawingSummary
                }, object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError) {
                        mContext.hideLoader()
                        //mContext.finishSession(mContext)
                        if (error.networkResponse == null) {
                            return
                        }
                        val errorRes =
                            DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                        if (errorRes.error?.messages?.size ?: 0 > 0) {
                            DottysBaseActivity().showSnackBarMessage(mContext,
                                errorRes.error?.messages?.first() ?: "")
                        }
                    }
                }) { //no semicolon or coma

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Authorization"] = mContext.getCurrentToken() ?: ""
                    return params
                }

            }
        mQueue.add(jsonArrayRequest)
    }

    fun getCurrentUserRequest(mContext: DottysBaseActivity) {
        val mQueue = Volley.newRequestQueue(mContext)

        mContext.showLoader()

        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "users/currentUser/",
            null,
            Response.Listener<JSONObject> { response ->
                mContext.hideLoader()
                Log.d("CURRENT USER", response.toString())
                var user: DottysLoginResponseModel =
                    DottysLoginResponseModel.fromJson(
                        response.toString()
                    )
                //getDrawingSummary(mContext) TODO GET GRAWIN SUMARY ** LOST REQUEST √√
                if (user.token.isNullOrBlank()) {
                    user.token = mContext.getUserPreference().token
                }
                userCurrentUserDataObserver?.currentUserModel = user
                mContext.saveDataPreference(PreferenceTypeKey.USER_DATA, user.toJson())
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext.hideLoader()

                    if (error.networkResponse == null) {
                        return
                    }
                    if (error.networkResponse.statusCode == 401) {
                        mContext.finishSession(mContext)
                    }
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        DottysBaseActivity().showSnackBarMessage(mContext,
                            errorRes.error?.messages?.first() ?: "")
                    }
                    Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getCurrentToken() ?: ""
                return params
            }

        }
        Log.d("CURRENT USER", jsonObjectRequest.url)
        mQueue.add(jsonObjectRequest)

    }

    fun getGlobalDataRequest(mContext: DottysBaseActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader()

        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "global",
            null,
            Response.Listener<JSONObject> { response ->
                mContext.hideLoader()
                Log.d("GLOBAL DATA", response.toString())
                val user: DottysGlobalDataModel =
                    DottysGlobalDataModel.fromJson(
                        response.toString()
                    )
                userCurrentUserDataObserver?.currentGlobalData = user
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext.hideLoader()
                    mContext.finishSession(mContext)
                    if (error.networkResponse == null) {
                        return
                    }
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        DottysBaseActivity().showSnackBarMessage(mContext,
                            errorRes.error?.messages?.first() ?: "")
                    }
                    Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getCurrentToken() ?: ""
                return params
            }

        }
        mQueue.add(jsonObjectRequest)

    }

    /**/
    fun getNearsDottysLocations(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)

        //mContext.showLoader()

        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "locations/" + mContext.getUserPreference().homeLocationID,
            null,
            Response.Listener<JSONObject> { response ->
                mContext.hideLoader()

                var dottysLocation: DottysDrawingRewardsModel =
                    DottysDrawingRewardsModel.fromJson(
                        response.toString()
                    )
                userCurrentUserDataObserver?.dottysLocation = dottysLocation
                // getDrawingSummary(mContext)
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    println(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (error.networkResponse == null) {
                        return
                    }
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        DottysBaseActivity().showSnackBarMessage(mContext,
                            errorRes.error?.messages?.first() ?: "")
                    }
                    Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getCurrentToken() ?: ""
                return params
            }


        }
        mQueue.add(jsonObjectRequest)

    }
    /**/

    fun getUserRewards(mContext: DottysBaseActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        // mContext.showLoader()
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "rewards/currentUser/?redeemed=true",
            null,
            Response.Listener<JSONObject> { response ->
                mContext.hideLoader()

                var rewards: DottysRewardsModel =
                    DottysRewardsModel.fromJson(
                        response.toString()
                    )
                userCurrentUserDataObserver?.rewardsAtSession = rewards
                mContext.saveDataPreference(PreferenceTypeKey.REWARDS, rewards.toJson())
                fillItemsAtDashboards(null)
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext.hideLoader()
                    //mContext.finishSession(mContext)
                    if (error.networkResponse == null) {
                        return
                    }
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
//                        DottysBaseActivity().showSnackBarMessage(this,
//                            mContext,
//                            errorRes.error?.messages?.first() ?: "",
//                            Toast.LENGTH_LONG
//                        ).show()
                    }
                    Log.e("TAG", error.message, error)
                    userCurrentUserDataObserver?.rewardsAtSession = DottysRewardsModel()
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getCurrentToken() ?: ""
                return params
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                Log.d("USERS REWARDS", response.toString())
                return super.parseNetworkResponse(response)
            }


        }
        mQueue.add(jsonObjectRequest)

    }

    /* BANNER DASHBOARD */
    fun getBannerDashboard(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "banners/",
            null,
            Response.Listener<JSONObject> { response ->
                mContext.hideLoader()
                Log.d("BANNER LIST -->", response.toString())
                var banners: DottysBannerModel =
                    DottysBannerModel.fromJson(
                        response.toString()
                    )
                userCurrentUserDataObserver?.dashboardBanners = banners


            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext.hideLoader()
                    if (error.networkResponse == null) {
                        return
                    }
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        DottysBaseActivity().showSnackBarMessage(mContext,
                            errorRes.error?.messages?.first() ?: "")
                    }
                    Log.e("TAG", error.message, error)
                    userCurrentUserDataObserver?.dashboardBanners = DottysBannerModel()
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getCurrentToken() ?: ""
                return params
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                Log.d("BANNERS AT DASHBOARD", response.toString())
                return super.parseNetworkResponse(response)
            }
        }
        mQueue.add(jsonObjectRequest)

    }

    /*BEACON LIST REQUEST */
    fun getBeaconList(mContext: DottysBaseActivity, storeNumber: String) {
        print("TOKEN 🚩: ${mContext.getCurrentToken() ?: ""}")
        if(mContext.getDottysBeaconsList()?.isNotEmpty() == true || "${mContext.getDottysBeaconsList()?.first()?.location?.storeNumber}" == storeNumber){
            mContext.initEstimoteBeaconManager()
            return
        }

        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader()
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "beacons/?major=$storeNumber",
            null,
            Response.Listener<JSONObject> { response ->
                mContext.hideLoader()
                Log.d("BEACON LIST -->", response.toString())
                var user: DottysBeaconsModel =
                    DottysBeaconsModel.fromJson(
                        response.toString()
                    )
                userCurrentUserDataObserver?.dottysBeaconList = user
                mContext.saveDataPreference(PreferenceTypeKey.BEACONS_LIST, user.toJson())
                mContext.saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION, user.toJson())
                mContext.initEstimoteBeaconManager()
                if(mContext is DottysMainNavigationActivity){
                    (mContext as? DottysMainNavigationActivity)?.initAnalyticsItems(user.beacons as ArrayList<DottysBeacon>?)
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext.hideLoader()
                    if (error.networkResponse == null) {
                        return
                    }
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        DottysBaseActivity().showSnackBarMessage(mContext,
                            errorRes.error?.messages?.first() ?: "")
                    }
                    Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getCurrentToken() ?: ""
                return params
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                Log.d("BEACON LIST", response.toString())
                return super.parseNetworkResponse(response)
            }
        }
        mQueue.add(jsonObjectRequest)

    }

    fun getLocationDrawing(fragment: Fragment) {
        val drawingViewModel = DrawingViewModel()
        drawingViewModel.initViewSetting(
            fragment as DashboardFragment,
            userCurrentUserDataObserver?.currentUserModel?.homeLocationID, null, null
        )
    }

    fun initDashboardPager(
        view: DottysMainNavigationActivity,
        drawingSummary: DottysGlobalDataModel,
    ) {
        DottysDrawing()
        var drawingList = ArrayList<Monthly?>()
        for (drawing in drawingSummary.drawingTemplates?.values!!) {
            drawingList.add(drawing)
        }

        // leftRollPagerButton = dashboardView?.findViewById<Button>(id.left_roll_pager_button)
        // rightRollPagerButton =  dashboardView?.findViewById<Button>(id.rigth_roll_pager_button)
        leftRollPagerButton?.setOnClickListener(this)
        rightRollPagerButton?.setOnClickListener(this)
        view.windowManager.defaultDisplay.getMetrics(view.displayMetrics)
        // pager = view.findViewById<ViewPager>(id.pager_dashboard)
        pager?.adapter = DashboardPagerAdapter(view, drawingList)
        adapter = DashboardPagerAdapter(view, drawingList)
        pager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
            }

            override fun onPageSelected(position: Int) {
                adapter?.currentPages = position

                Log.d("selected", position.toString())
            }

        })

        val drawingViewModel = DrawingViewModel()
        drawingViewModel.drawingObserver = DottysDrawingObserver(this)
        pager?.setOnClickListener {
            drawingViewModel.getUserDrawings(
                mainFragmentActivity ?: DottysMainNavigationActivity()
            )
        }
        // TODO getDrawingSummary(activity as DottysMainNavigationActivity)
    }


    private fun hideAnalitycsView(activity: DottysMainNavigationActivity) {
        var screenHeigth = activity.resources.displayMetrics?.heightPixels ?: 0
        floatingAnalicsView?.animate()?.translationY(screenHeigth.toFloat())?.setDuration(250)
            ?.start()
    }

    private fun showAnalitycsView() {
        floatingAnalicsView?.visibility = View.VISIBLE
        floatingAnalicsView?.animate()?.translationY(0.0f)?.setDuration(450)?.start()

    }

    @SuppressLint("SetTextI18n")
    fun initAnalitycsItems(beaconList: ArrayList<DottysBeacon>?) {
        val storeLocation =
            mainFragmentActivity?.findViewById<TextView>(R.id.location_analitycs_store) //?: return
        val closeAnalyticButton =
            mainFragmentActivity?.findViewById<Button>(R.id.close_analytics_buttom) //?: return
        val userHostIdTextView =
            mainFragmentActivity?.findViewById<TextView>(R.id.user_host_id) //?: return
        val locationEnableTextView =
            mainFragmentActivity?.findViewById<TextView>(R.id.location_enable_textview) //?: return
        val locationDeviceTextView =
            mainFragmentActivity?.findViewById<TextView>(R.id.location_device_analytic_textview) //?: return
        val sendToSupportButton =
            mainFragmentActivity?.findViewById<Button>(R.id.send_to_support_button) //?: return
        val isPushEnable =
            mainFragmentActivity?.findViewById<TextView>(R.id.is_push_enable_textview) //?: return
        isPushEnable?.text = if(mainFragmentActivity?.isPushNotificationEnable() == true) "Enable" else "Disable"
        sendToSupportButton?.setOnClickListener(this)
        closeAnalyticButton?.setOnClickListener(this)
        closeAnalyticButton?.setOnClickListener {
            mainFragmentActivity?.let { it1 -> hideAnalitycsView(it1) }
        }
        userHostIdTextView?.text = mainFragmentActivity?.getUserPreference()?.id ?: ""
        val trackerLocation = mainFragmentActivity?.gpsTracker
        var isEnableLocation = "Disable"
        if (trackerLocation?.isGPSEnabled == true) {
            isEnableLocation = "Enable"
        }
        val lat = trackerLocation?.locationGps?.latitude ?: mainFragmentActivity?.gpsTracker?.getLatitude()
        val long = trackerLocation?.locationGps?.longitude ?: mainFragmentActivity?.gpsTracker?.getLongitude()

        val latitude = BigDecimal(lat.toString()).setScale(3, 1)
        val longitude = BigDecimal(long.toString()).setScale(3, 1)

        locationDeviceTextView?.text = "Lat: $latitude | Long: $longitude"
        locationEnableTextView?.text = isEnableLocation
        mainActivity?.getUserNearsLocations()?.locations?.first()?.let { location -> storeLocation?.text = "${location.storeNumber ?: 0}" }

        val listViewRewards =
            mainFragmentActivity?.findViewById<ListView>(R.id.beacons_analytics_listview)
        if (mainFragmentActivity == null) {
            return
        }
        listViewRewards?.adapter = AnalyticBeacoonsAdapter(
            mainFragmentActivity ?: DottysBaseActivity(),
            beaconList ?: mainFragmentActivity?.getBeaconStatus()?.beaconArray ?: return
        )
    }

    override fun getUserRewards(rewards: DottysDrawingRewardsModel) {
        mainFragmentActivity?.saveDataPreference(PreferenceTypeKey.DRAWINGS, rewards.toJson())
        initDashboardItemsView()
    }

    override fun getUserDrawings(drawing: DottysDrawingUserModel) {
        val drawingSelected = adapter?.getCurrentPage()?.let { drawing.drawings?.get(it) }
        val intent = Intent(mainFragmentActivity, DottysRewardRedeemedActivity::class.java)
        intent.putExtra("DRAWING_DATA", drawingSelected?.toJson().toString())
        mainFragmentActivity?.startActivity(intent)
    }

}

/* CURRENT USER PROTOCOL */
//region
interface DottysDashboardDelegates {
    fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel)
    fun getCurrentUser(currentUser: DottysLoginResponseModel)
    fun getUserRewards(rewards: DottysRewardsModel)
    fun getGlobalData(gloabalData: DottysGlobalDataModel)
    fun getDottysUserLocation(locationData: DottysDrawingRewardsModel)
    fun getBeaconList(beaconList: DottysBeaconsModel)
    fun onDashboardBanners(banners: DottysBannerModel)
}

class DottysCurrentUserObserver(lisener: DottysDashboardDelegates) {
    private val element = ArrayList<DottysDrawingSumaryModelElement>()
    var dawingSummaryModel: DottysDrawingSumaryModel by Delegates.observable(
        initialValue = DottysDrawingSumaryModel(
            element
        ),
        onChange = { _, _, new -> lisener.getDrawingSummary(new) })

    var currentUserModel: DottysLoginResponseModel by Delegates.observable(
        initialValue = DottysLoginResponseModel(),
        onChange = { _, _, new -> lisener.getCurrentUser(new) })

    var rewardsAtSession: DottysRewardsModel by Delegates.observable(
        initialValue = DottysRewardsModel(),
        onChange = { _, _, new -> lisener.getUserRewards(new) })

    var currentGlobalData: DottysGlobalDataModel by Delegates.observable(
        initialValue = DottysGlobalDataModel(),
        onChange = { _, _, new -> lisener.getGlobalData(new) })
    var dottysLocation: DottysDrawingRewardsModel by Delegates.observable(
        initialValue = DottysDrawingRewardsModel(),
        onChange = { _, _, new -> lisener.getDottysUserLocation(new) })
    var dottysBeaconList: DottysBeaconsModel by Delegates.observable(
        initialValue = DottysBeaconsModel(),
        onChange = { _, _, new -> lisener.getBeaconList(new) })
    var dashboardBanners: DottysBannerModel by Delegates.observable(
        initialValue = DottysBannerModel(),
        onChange = { _, _, new -> lisener.onDashboardBanners(new) })

}