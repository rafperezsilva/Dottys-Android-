package com.keylimetie.dottys.ui.drawing

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.redeem.DottysRedeemAdapter
import com.keylimetie.dottys.redeem.RedeemRewardsSegment
import com.keylimetie.dottys.ui.dashboard.DashboardFragment
import org.json.JSONObject
import kotlin.properties.Delegates
enum class RewardsSegment {
   DRAWING_ENTRIES, CASH_REWARDS
}
class DrawingViewModel : ViewModel() {

    private var segmentSelected =  RewardsSegment.DRAWING_ENTRIES
    private val _text = MutableLiveData<String>().apply {
        value = "This is tools Fragment"
    }
    val text: LiveData<String> = _text
    var drawingObserver: DottysDrawingObserver? = null
    var drawingButton: Button? = null
    var cashButton: Button? = null
    var viewRoot: View? = null
    fun initViewSetting(fragment: Fragment, locationId: String?, activityFragment: DottysMainNavigationActivity, viewRoot: View?){
        this.viewRoot = viewRoot
        val activity =  fragment.activity as? DottysMainNavigationActivity?
        if (fragment is DashboardFragment) {
            drawingObserver = DottysDrawingObserver(fragment as DashboardFragment)
        } else {
            getUserDrawings(activity!!)
        }
        if (locationId != null) {
                getDrawingSummary(activity!!,locationId)
            }
        activityFragment?.let { segmentTabLisener(it) }
    }
    fun segmentTabLisener(activity: DottysMainNavigationActivity){
        drawingButton?.setOnClickListener {
            segmentSelected = RewardsSegment.DRAWING_ENTRIES
            viewSegmentSelectedHandler(segmentSelected, activity)
            initListView()
        }
        cashButton?.setOnClickListener {
            segmentSelected = RewardsSegment.CASH_REWARDS
            viewSegmentSelectedHandler(segmentSelected, activity)
            initListView()
        }

    }

    fun initListView(){
        val listViewRewards = viewRoot?.findViewById<ListView>(R.id.drawings_listview)
        var isRedeemed: Boolean = false
        if (segmentSelected == RedeemRewardsSegment.REDEEMED_REWARDS) {
            isRedeemed = true
        }
       /* listViewRewards?.adapter =
            redeemsUserData.rewards?.let { viewRoot?.let { it1 -> DottysRedeemAdapter(it1, it.filter { it.redeemed == isRedeemed }) } }
        titleRedeem?.text =  attributedRedeemText(redeemsUserData.rewards?.filter{ it.redeemed == isRedeemed }?.size.toString())
        */
    }

    fun viewSegmentSelectedHandler(segment: RewardsSegment,activity: DottysMainNavigationActivity){
        when(segment){
            RewardsSegment.DRAWING_ENTRIES -> {
                activity?.let { ContextCompat.getColor(it, R.color.colorTransparent) }?.let {
                    drawingButton?.setBackgroundColor(it)}
                activity?.let { ContextCompat.getColor(it,R.color.colorSelectedSegment) }?.let {
                    cashButton?.setBackgroundColor(it)}
            }
            RewardsSegment.CASH_REWARDS -> {
                activity?.let { ContextCompat.getColor(it,R.color.colorSelectedSegment) }?.let {
                    drawingButton?.setBackgroundColor(it)}
                activity?.let { ContextCompat.getColor(it,R.color.colorTransparent) }?.let {
                    cashButton?.setBackgroundColor(it)}
            }
        }
    }

    private fun getDrawingSummary(mContext: DottysMainNavigationActivity, locationId: String) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader(mContext)
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl+"locations/"+locationId,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    mContext.hideLoader(mContext)

                    var user: DottysDrawingRewardsModel =
                        DottysDrawingRewardsModel.fromJson(
                            response.toString()
                        )
                    // getDrawingSummary(mContext)
                    drawingObserver?.rewardsModel = user
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

        }

        mQueue.add(jsonObjectRequest)
    }

    private fun getUserDrawings(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader(mContext)
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl+"drawings/mydrawings",
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    mContext.hideLoader(mContext)

                    var user: DottysDrawingUserModel =
                        DottysDrawingUserModel.fromJson(
                            response.toString()
                        )
                    // getDrawingSummary(mContext)
                    drawingObserver?.drawingsModel = user
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

        }

        mQueue.add(jsonObjectRequest)
    }

}

interface DottysDrawingDelegates {
    fun getUserRewards(rewards: DottysDrawingRewardsModel)
    fun getUserDrawings(drawing: DottysDrawingUserModel)

}

class DottysDrawingObserver(lisener: DottysDrawingDelegates) {
     var rewardsModel: DottysDrawingRewardsModel by Delegates.observable(
        initialValue = DottysDrawingRewardsModel(),
        onChange = { prop, old, new -> lisener.getUserRewards(new) })
     var drawingsModel: DottysDrawingUserModel by Delegates.observable(
        initialValue = DottysDrawingUserModel(),
        onChange = { prop, old, new -> lisener.getUserDrawings(new) })
}