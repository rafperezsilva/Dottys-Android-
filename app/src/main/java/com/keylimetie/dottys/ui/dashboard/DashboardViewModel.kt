package com.keylimetie.dottys.ui.dashboard


import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R.id
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.ui.drawing.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.drawing.DrawingViewModel
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates
import android.R as R1


class DashboardViewModel : ViewModel() {

    var userCurrentUserDataObserver: DottysCurrentUserObserver? = null
   // private val displayMetrics = DisplayMetrics()

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
            rootView.findViewById<TextView>(id.profile_name_dashboard)
        val locationDashboard =
            rootView.findViewById<TextView>(id.location_dashboard_textview)
        val pointsEarned =
            rootView.findViewById<TextView>(id.points_earned_textview)
        val cashRewards =
            rootView.findViewById<TextView>(id.cash_rewards_textview)
        val weeklyRewards =
            rootView.findViewById<TextView>(id.weekly_count_textview)
        val monthlyRewards =
            rootView.findViewById<TextView>(id.monthly_count_textview)
        val querterlyRewards =
            rootView.findViewById<TextView>(id.quarterly_count_textview)
         val weeklyDays =
            rootView.findViewById<TextView>(id.weekly_end_days)
        val monthlyDays =
            rootView.findViewById<TextView>(id.monthly_end_days)
        val querterlyDays =
            rootView.findViewById<TextView>(id.quarterly_end_days)

        nameDashboard.text = userCurrentUserDataObserver?.currentUserModel?.fullName
        val stringFormated: String = NumberFormat.getIntegerInstance()
            .format(userCurrentUserDataObserver?.currentUserModel?.points)
        cashRewards.text = "$" + getCashForDrawing()
        pointsEarned.text = stringFormated
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

        //   mContext.showLoader(mContext)
        var url = "https://www.gravatar.com/avatar/" + email?.md5()
        if (userCurrentUserDataObserver?.currentUserModel?.profilePicture != null){
             url =  userCurrentUserDataObserver?.currentUserModel?.profilePicture ?: ""
        }

        val mQueue = Volley.newRequestQueue(mContext)

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
                imageView.setImageResource(R1.drawable.ic_menu_help)
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

     /**/
   fun getNearsDottysLocations(mContext: DottysMainNavigationActivity) {
       val mQueue = Volley.newRequestQueue(mContext)

       mContext.showLoader(mContext)

       val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
           mContext.baseUrl + "locations/"+mContext.getUserPreference().homeLocationID,
           null,
           object : Response.Listener<JSONObject> {
               override fun onResponse(response: JSONObject) {
                   mContext.hideLoader(mContext)

                   var dottysLocation: DottysDrawingRewardsModel =
                       DottysDrawingRewardsModel.fromJson(
                           response.toString()
                       )
                   userCurrentUserDataObserver?.dottysLocation = dottysLocation
                   // getDrawingSummary(mContext)

               }
           },
           object : Response.ErrorListener {
               override fun onErrorResponse(error: VolleyError) {
                   println(error.networkResponse.data.toString(Charsets.UTF_8))
                   Log.e("TAG", error.message, error)
               }
           }) { //no semicolon or coma
//           override fun getParams(): MutableMap<String, String> {
//               val location = mContext.getLocation(mContext.gpsTracker,mContext)
//               val params = HashMap<String, String>()
//               params["latitude"] = location.latitude.toString()
//               params["longitude"] = location.longitude.toString()
//               params["limit"] = "50"
//               params["page"] = "1"
//               params["distance"] = "50"
//               return params
//           }
           @Throws(AuthFailureError::class)
           override fun getHeaders(): Map<String, String> {
               val params = HashMap<String, String>()
               params["Authorization"] = mContext.getUserPreference().token!!
               return params
           }
           override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {


               return super.parseNetworkResponse(response)
           }


       }
       mQueue.add(jsonObjectRequest)

   }
    /**/

    fun getUserRewards(mContext: DottysBaseActivity) {
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
                  //  user.rewards = user.rewards?.filter { it.locationID != null }
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
            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                println(response.toString())

                return super.parseNetworkResponse(response)
            }


        }
        mQueue.add(jsonObjectRequest)

    }
/*BEACON LIST REQUEST */
    fun getBeaconList(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader(mContext)

        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "beacons",
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    mContext.hideLoader(mContext)

                    var user: DottysBeaconsModel =
                        DottysBeaconsModel.fromJson(
                            response.toString()
                        )
                    userCurrentUserDataObserver?.dottysBeaconList = user

                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    mContext.hideLoader(mContext)
                    Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getUserPreference().token!!
                return params
            }
            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                println(response.toString())

                return super.parseNetworkResponse(response)
            }


        }
        mQueue.add(jsonObjectRequest)

    }

    fun getLocationDrawing(fragment: Fragment) {
        val drawingViewModel = DrawingViewModel()
        drawingViewModel.initViewSetting(
            fragment as  DashboardFragment,
            userCurrentUserDataObserver?.currentUserModel?.homeLocationID, null, null
        )
    }

    fun initDashboardPager(
        view: DottysMainNavigationActivity,
        drawingSummary: DottysGlobalDataModel
    ) {
        view.windowManager.defaultDisplay.getMetrics(view.displayMetrics)
        val pager = view.findViewById<ViewPager>(id.pager_dashboard)
        pager.adapter = DashboardPagerAdapter(view, drawingSummary, view.displayMetrics)
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
            fun getDottysUserLocation(locationData: DottysDrawingRewardsModel)
            fun getBeaconList(beaconList: DottysBeaconsModel)
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
            var dottysLocation: DottysDrawingRewardsModel by Delegates.observable(
                initialValue = DottysDrawingRewardsModel(),
                onChange = { prop, old, new -> lisener.getDottysUserLocation(new) })
             var dottysBeaconList: DottysBeaconsModel by Delegates.observable(
                initialValue = DottysBeaconsModel(),
                onChange = { prop, old, new -> lisener.getBeaconList(new) })
        }