package com.keylimetie.dottys.ui.drawing

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.*
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
import com.keylimetie.dottys.DottysErrorModel
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.ui.dashboard.DashboardFragment
import com.keylimetie.dottys.ui.drawing.models.DottysDrawing
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingUserModel
import com.keylimetie.dottys.utils.DottysStatics
import org.json.JSONObject
import kotlin.properties.Delegates


enum class RewardsSegment {
    DRAWING_ENTRIES, CASH_REWARDS
}

class DrawingViewModel : ViewModel() {
    var titleTotalPoints: TextView? = null
    private var subTitle: TextView? = null
    var segmentLayout: LinearLayout? = null
    var listViewRewards: ListView? = null
    var segmentSelected = RewardsSegment.DRAWING_ENTRIES
    private val _text = MutableLiveData<String>().apply {
        value = "This is tools Fragment"
    }
    var activity: DottysMainNavigationActivity? = null
    val text: LiveData<String> = _text
    var drawingObserver: DottysDrawingObserver? = null


    var drawingButton: Button? = null
    var cashButton: Button? = null
    var viewRoot: View? = null
    var fragment: Fragment? = Fragment()
    var userDrawing: DottysDrawingUserModel? = null
    fun initViewSetting(
        fragment: Fragment,
        locationId: String?,
        activityFragment: DottysMainNavigationActivity?,
        viewRoot: View?,
    ) {

        activity = fragment.activity as? DottysMainNavigationActivity?
         segmentSelected = if (activity?.intent?.getBooleanExtra("IS_DASHBOARD_BUTTON",false) == true) RewardsSegment.DRAWING_ENTRIES else activity?.segmentSelect ?: RewardsSegment.DRAWING_ENTRIES
        this.viewRoot = viewRoot
        this.fragment = fragment
        if (fragment is DashboardFragment) {
            drawingObserver = DottysDrawingObserver(fragment)
        } else {
            drawingObserver = DottysDrawingObserver(fragment as DrawingFragment)
            initDrawingView(viewRoot)
           //TODO getUserDrawings(activity!!)
        }
        if (locationId != null) {
            getCurrentDrawingLocation(activity ?: return, locationId)
        }
        activityFragment?.let { segmentTabLisener(it) }
        fragment.context.let {
            it?.let { it1 -> viewSegmentSelectedHandler(segmentSelected, it1) }
        }
        emptyDrawingText(segmentSelected == RewardsSegment.DRAWING_ENTRIES)
        if(segmentSelected == RewardsSegment.CASH_REWARDS) {
            initListView()
        }
    }

    private fun initDrawingView(viewRoot: View?) {
        titleTotalPoints = viewRoot?.findViewById<TextView>(R.id.drawing_title_textview)
        drawingButton = viewRoot?.findViewById<Button>(R.id.drawing_entries_button)
        cashButton = viewRoot?.findViewById<Button>(R.id.cash_rewards_button)
        subTitle = viewRoot?.findViewById<TextView>(R.id.drawing_subtitle_textview)
        segmentLayout?.visibility = View.VISIBLE
    }

    private fun segmentTabLisener(activity: DottysMainNavigationActivity) {
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

    private fun emptyDrawingText(isVisible: Boolean) {
        val tvDynamic = viewRoot?.findViewById<TextView>(R.id.empty_drawing_textview)
        tvDynamic?.text = "There are no currently no active drawings.\nPlease check back later."
        tvDynamic?.alpha = if (isVisible) 1f else 0f
    }

    fun attributedRedeemText(unclaimedRewards: String): SpannableString {
        val spannable = SpannableString("You have $unclaimedRewards points!")
        spannable.setSpan(
            ForegroundColorSpan(Color.YELLOW),
            8, 9 + unclaimedRewards.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0, spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AbsoluteSizeSpan(22, true),
            0,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable

    }

    private fun drawingEntriesLocation(isVisiBle: Boolean) {
        val linearLayout = viewRoot?.findViewById<LinearLayout>(R.id.drawing_entries_bottom_layout)
        linearLayout?.alpha = if (isVisiBle) 1f else 0f
        val drawingEntriesLocationTextView =
            viewRoot?.findViewById<TextView>(R.id.drawing_entries_location)
        val drawinStaticLocationTextView =
            viewRoot?.findViewById<TextView>(R.id.drawing_location_static_textview)
        activity?.getDrawings().let {
            if (!it?.address1.isNullOrEmpty()) {
                drawingEntriesLocationTextView?.text = it?.address1
                drawinStaticLocationTextView?.visibility = VISIBLE
            } else {
                drawinStaticLocationTextView?.visibility = INVISIBLE
            }
        }
    }

    fun initListView() {
        listViewRewards = viewRoot?.findViewById<ListView>(R.id.drawings_listview)
        emptyDrawingText(segmentSelected == RewardsSegment.DRAWING_ENTRIES)
        listViewRewards?.alpha = if (segmentSelected == RewardsSegment.DRAWING_ENTRIES) 0f else 1f
        drawingEntriesLocation(segmentSelected == RewardsSegment.DRAWING_ENTRIES)
       // emptyDrawingText(false)

        listViewRewards?.adapter =
            activity?.let {
                fragment?.context?.let { it1 ->
                    DottysDrawingAdapter(
                        it1,
                        it,
                        cashRewardsWiredData(),
                        segmentSelected
                    )
                }
            }
    }

    private fun cashRewardsWiredData(): ArrayList<DottysDrawing> {
        var currentDrawing = ArrayList<DottysDrawing>()
        when (segmentSelected) {
            RewardsSegment.DRAWING_ENTRIES -> {
                if (userDrawing?.drawings != null) {
                    currentDrawing = userDrawing?.drawings as ArrayList<DottysDrawing>
                }
            }
            RewardsSegment.CASH_REWARDS -> {
                currentDrawing = DottysStatics.staticCashRewards
            }
        }
        return currentDrawing
    }

//    var rewardsWired0 = DottysDrawing()
//    var rewardsWired1 = DottysDrawing()
//    var rewardsWired2 = DottysDrawing()
//    rewardsWired0.title = "$10\n Cash Reward"
//    rewardsWired0.subtitle = "1,000 Points for $10"
//    rewardsWired1.title = "$20\n Cash Reward"
//    rewardsWired1.subtitle = "2,000 Points for $20"
//    rewardsWired2.title = "$50\n Cash Reward"
//    rewardsWired2.subtitle = "5,000 Points for $50"
//    currentDrawing.add(rewardsWired0)
//    currentDrawing.add(rewardsWired1)
//    currentDrawing.add(rewardsWired2)
    private fun viewSegmentSelectedHandler(segment: RewardsSegment, contex: Context) {
        when (segment) {
            RewardsSegment.DRAWING_ENTRIES -> {

                drawingButton?.setBackgroundColor(
                    ContextCompat.getColor(
                        contex,
                        R.color.colorTransparent
                    )
                )
                cashButton?.setBackgroundColor(
                    ContextCompat.getColor(
                        contex,
                        R.color.colorSelectedSegment
                    )
                )
                subTitle?.text =
                    "Redeem your points for entries into your local Dotty’s weekly, monthly and quarterly drawings."

            }
            RewardsSegment.CASH_REWARDS -> {
                contex.let { ContextCompat.getColor(it, R.color.colorSelectedSegment) }.let {
                    drawingButton?.setBackgroundColor(it)
                }
                contex.let { ContextCompat.getColor(it, R.color.colorTransparent) }.let {
                    cashButton?.setBackgroundColor(it)
                }
                subTitle?.text =
                    "Convert your points into cash to be redeemed at your local Dotty’s location."

            }
        }
        fragment?.view?.visibility = View.VISIBLE
    }

    fun getCurrentDrawingLocation(mContext: DottysMainNavigationActivity, locationId: String) {
        val mQueue = Volley.newRequestQueue(mContext)
        //  mContext.showLoader()
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "locations/" + locationId,
            null,
            Response.Listener<JSONObject> { response ->
                mContext.hideLoader()

                var user: DottysDrawingRewardsModel =
                    DottysDrawingRewardsModel.fromJson(
                        response.toString()
                    )
                // getDrawingSummary(mContext)
                drawingObserver?.rewardsModel = user


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
                        Toast.makeText(
                            mContext,
                            errorRes.error?.messages?.first() ?: "",
                            Toast.LENGTH_LONG
                        ).show()
                    }
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

    fun getUserDrawings(mContext: DottysMainNavigationActivity) {
        val mQueue = Volley.newRequestQueue(mContext)
        mContext.showLoader()
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            mContext.baseUrl + "drawings/mydrawings",
            null,
            Response.Listener<JSONObject> { response ->
                mContext.hideLoader()
                try {
                    Log.d("MY DRAWINGS", response.toString())
                    var user: DottysDrawingUserModel =
                        DottysDrawingUserModel.fromJson(
                            response.toString()
                        )
                    // getDrawingSummary(mContext)
                    userDrawing = user
                    drawingObserver?.drawingsModel = user
                } catch (e: Error) {
                    Log.d("ERROR", e.localizedMessage)
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
                        Toast.makeText(
                            mContext,
                            errorRes.error?.messages?.first() ?: "",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Log.e("TAG", error.message, error)
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = mContext.getUserPreference().token ?: ""
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