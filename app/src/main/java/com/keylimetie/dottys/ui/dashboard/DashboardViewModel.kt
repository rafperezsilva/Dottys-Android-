package com.keylimetie.dottys.ui.dashboard

import android.R
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.ViewPager
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R.id
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.ui.drawing.DrawingViewModel
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set
import kotlin.properties.Delegates


class DashboardViewModel : ViewModel() {

    var userCurrentUserDataObserver: DottysCurrentUserObserver? = null
    private val displayMetrics = DisplayMetrics()

    fun initDashboardViewSetting(
        fragment: DashboardFragment,
        mContext: DottysMainNavigationActivity
    ) {
        userCurrentUserDataObserver = DottysCurrentUserObserver(fragment)
        mContext.hideLoader(mContext)
        // if (userCurrentUserDataObserver?.currentUserModel == null) {
        getCurrentUserRequest(mContext)
//        } else {
//            initDashboardItemsView(fragment.viewFragment!!)
//            getUserRewards(mContext)
//        }

    }

    fun     initDashboardItemsView(rootView: View, rewardsLoaction: com.keylimetie.dottys.ui.drawing.DottysDrawingRewardsModel, activity:DottysMainNavigationActivity) {
        val nameDashboard =
            rootView.findViewById<TextView>(com.keylimetie.dottys.R.id.profile_name_dashboard)
        val locationDashboard =
            rootView.findViewById<TextView>(com.keylimetie.dottys.R.id.location_dashboard_textview)
        val pointsEarned =
            rootView.findViewById<TextView>(com.keylimetie.dottys.R.id.points_earned_textview)
        val cashRewards =
            rootView.findViewById<TextView>(com.keylimetie.dottys.R.id.cash_rewards_textview)
        val weeklyRewards =
            rootView.findViewById<TextView>(com.keylimetie.dottys.R.id.weekly_count_textview)
        val monthlyRewards =
            rootView.findViewById<TextView>(com.keylimetie.dottys.R.id.monthly_count_textview)
        val querterlyRewards =
            rootView.findViewById<TextView>(com.keylimetie.dottys.R.id.quarterly_count_textview)
         val weeklyDays =
            rootView.findViewById<TextView>(com.keylimetie.dottys.R.id.weekly_end_days)
        val monthlyDays =
            rootView.findViewById<TextView>(com.keylimetie.dottys.R.id.monthly_end_days)
        val querterlyDays =
            rootView.findViewById<TextView>(com.keylimetie.dottys.R.id.quarterly_end_days)

        nameDashboard.text = userCurrentUserDataObserver?.currentUserModel?.fullName
        val s: String = NumberFormat.getIntegerInstance()
            .format(userCurrentUserDataObserver?.currentUserModel?.points)
        cashRewards.text = "$" + getCashForDrawing()
        pointsEarned.text = s
        locationDashboard.text = addressLocationFotmatted(rewardsLoaction)
        weeklyRewards.text =
            userCurrentUserDataObserver?.dawingSummaryModel?.filter { it.drawingType == "WEEKLY" }
                ?.first()?.numberOfEntries.toString()
        monthlyRewards.text =
            userCurrentUserDataObserver?.dawingSummaryModel?.filter { it.drawingType == "MONTHLY" }
                ?.first()?.numberOfEntries.toString()
        querterlyRewards.text =
            userCurrentUserDataObserver?.dawingSummaryModel?.filter { it.drawingType == "QUARTERLY" }
                ?.first()?.numberOfEntries.toString()
        weeklyDays.text = userCurrentUserDataObserver?.dawingSummaryModel?.filter { it.drawingType == "WEEKLY" }
            ?.first()?.endDate?.let {activity.getDiferencesDays(it) }
        monthlyDays.text = userCurrentUserDataObserver?.dawingSummaryModel?.filter { it.drawingType == "MONTHLY" }
            ?.first()?.endDate?.let { activity.getDiferencesDays(it) }
        querterlyDays.text = userCurrentUserDataObserver?.dawingSummaryModel?.filter { it.drawingType == "QUARTERLY" }
            ?.first()?.endDate?.let { activity.getDiferencesDays(it) }



    }

   fun getCashForDrawing(): String {
        val drawingUser =
            userCurrentUserDataObserver?.currentUserRewards?.rewards?.filter { it.redeemed == false }
        var cash = 0
        if (drawingUser != null) {
            for (drawing in drawingUser) {
                if (drawing.value != null) {
                    cash = (cash + drawing.value!!).toInt()
                }
            }
        }
        return NumberFormat.getIntegerInstance().format(cash)

    }

    fun addressLocationFotmatted(rewardsLoaction: com.keylimetie.dottys.ui.drawing.DottysDrawingRewardsModel): String {
        val staticFirtsText = "Your drawing entries are entered at "
        return staticFirtsText + rewardsLoaction.address1 + ", " + rewardsLoaction.city + ", " + rewardsLoaction.state + " " + rewardsLoaction.zip
    }

    fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }

    fun addProfileImage(
        mContext: DottysMainNavigationActivity,
        rootView: View,
        fragment: Fragment
    ) {
        val imageView = rootView.findViewById<CircleImageView>(id.profile_dashboard_image)
        val email = userCurrentUserDataObserver?.currentUserModel?.email//"mrirenita@gmail.com"
        val mQueue = Volley.newRequestQueue(mContext)
        val url = "https://www.gravatar.com/avatar/" + email?.md5()
        //   mContext.showLoader(mContext)

        val request = ImageRequest(url,
            Response.Listener { bitmap ->
                //   mContext.hideLoader(mContext)
                imageView.setImageBitmap(bitmap)
                //            Picasso.get().
//            Picasso.get().transform(CircleTransform())
//                .into(imageView)
                getLocationDrawing(fragment)

            }, 0, 0, null,
            Response.ErrorListener {
                //mContext.hideLoader(mContext)
                imageView.setImageResource(R.drawable.ic_menu_help)
            })



        mQueue.add(request)
    }

    private fun getDrawingSummary(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader(mContext)
        val jsonArrayRequest =
            object : JsonArrayRequest(Method.GET, mContext.baseUrl + "drawings/summary", null,
                Response.Listener<JSONArray> { response ->
                    mContext.hideLoader(mContext)
                    val drawingSummary: DottysDrawingSumaryModel =
                        DottysDrawingSumaryModel.fromJson(
                            response.toString()
                        )
                    userCurrentUserDataObserver?.dawingSummaryModel = drawingSummary
                }, object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError) {
                        mContext.hideLoader(mContext)
                        mContext.finishSession(mContext)
                    }
                }) { //no semicolon or coma

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Authorization"] = mContext.getUserPreference().token!!
                    return params
                }

            }
        mQueue.add(jsonArrayRequest)
    }

    private fun getCurrentUserRequest(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader(mContext)

        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "users/currentUser/",
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    mContext.hideLoader(mContext)

                    var user: DottysLoginResponseModel =
                        DottysLoginResponseModel.fromJson(
                            response.toString()
                        )
                    getDrawingSummary(mContext)
                    userCurrentUserDataObserver?.currentUserModel = user
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext.hideLoader(mContext)
                    mContext.finishSession(mContext)
                    Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getUserPreference().token!!
                return params
            }

        }
        mQueue.add(jsonObjectRequest)

    }

     fun getGlobalDataRequest(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader(mContext)

        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "global",
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    mContext.hideLoader(mContext)

                    var user: DottysGlobalDataModel =
                        DottysGlobalDataModel.fromJson(
                            response.toString()
                        )
                    userCurrentUserDataObserver?.currentGlobalData = user
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext.hideLoader(mContext)
                    mContext.finishSession(mContext)
                    Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getUserPreference().token!!
                return params
            }

        }
        mQueue.add(jsonObjectRequest)

    }

    fun getUserRewards(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader(mContext)

        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "rewards/currentUser/?redeemed=true",
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    mContext.hideLoader(mContext)

                    var user: DottysRewardsModel =
                        DottysRewardsModel.fromJson(
                            response.toString()
                        )
                    // getDrawingSummary(mContext)
                    userCurrentUserDataObserver?.currentUserRewards = user
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext.hideLoader(mContext)
                    mContext.finishSession(mContext)
                    Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getUserPreference().token!!
                return params
            }

        }
        mQueue.add(jsonObjectRequest)

    }


    fun getLocationDrawing(fragment: Fragment) {
        val drawingViewModel = DrawingViewModel()
        drawingViewModel.initViewSetting(
            fragment as  DashboardFragment,
            userCurrentUserDataObserver?.currentUserModel?.homeLocationID
        )
    }

    fun initDashboardPager(
        view: DottysMainNavigationActivity,
        drawingSummary: DottysGlobalDataModel
    ) {
        view.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val pager = view.findViewById<ViewPager>(id.pager_dashboard)
        pager.adapter = DashboardPagerAdapter(view, drawingSummary, displayMetrics)
        getUserRewards(view)
    }

}

/* CURRENT USER PROTOCOL */
//region
interface DottysDashboardDelegates {
    fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel)
    fun getCurrentUser(currentUser: DottysLoginResponseModel)
    fun getUserRewards(rewards: DottysRewardsModel)
    fun getGlobalData(gloabalData: DottysGlobalDataModel)
}

class DottysCurrentUserObserver(lisener: DottysDashboardDelegates) {
    private val element = ArrayList<DottysDrawingSumaryModelElement>()
    var dawingSummaryModel: DottysDrawingSumaryModel by Delegates.observable(
        initialValue = DottysDrawingSumaryModel(element),
        onChange = { prop, old, new -> lisener.getDrawingSummary(new) })

    var currentUserModel: DottysLoginResponseModel by Delegates.observable(
        initialValue = DottysLoginResponseModel(),
        onChange = { prop, old, new -> lisener.getCurrentUser(new) })

    var currentUserRewards: DottysRewardsModel by Delegates.observable(
        initialValue = DottysRewardsModel(),
        onChange = { prop, old, new -> lisener.getUserRewards(new) })

    var currentGlobalData: DottysGlobalDataModel by Delegates.observable(
        initialValue = DottysGlobalDataModel(),
        onChange = { prop, old, new -> lisener.getGlobalData(new) })
}