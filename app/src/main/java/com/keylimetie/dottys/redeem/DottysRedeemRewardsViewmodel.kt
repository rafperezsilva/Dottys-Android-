package com.keylimetie.dottys.redeem

import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.drawable.ColorDrawable
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.agik.AGIKSwipeButton.Controller.OnSwipeCompleteListener
import com.agik.AGIKSwipeButton.View.Swipe_Button_View
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.DottysErrorModel
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysRewardsModel
import org.json.JSONObject
import kotlin.math.roundToInt
import kotlin.properties.Delegates

enum class RedeemRewardsSegment {
    AVAIBLE_REWARDS, REDEEMED_REWARDS
}
open class DottysRedeemRewardsViewmodel : ViewModel() {

    private var codeVerificationLayout: ConstraintLayout? = null
    private var congratsTextview: TextView? = null

    /* REDEEEM REWARDS VIEW ITEMS */
    var avaibleButton: Button? = null
    var redeemedButton: Button? = null
    var segmentSelected: RedeemRewardsSegment = RedeemRewardsSegment.AVAIBLE_REWARDS
    var redeemsUserData = DottysRewardsModel()
    var activityRedeem: DottysRedeemRewardsActivity? = null
    var titleRedeem: TextView? = null

    /* CASH REWARDS VIEW ITEMS */
    private var redeemImage: ImageView? = null
    private var start: Swipe_Button_View? = null

    private var zeroButton: Button? = null
    private var oneButton: Button? = null
    private var twoButton: Button? = null
    private var threeButton: Button? = null
    private var fourButton: Button? = null
    private var fiveButton: Button? = null
    private var sixButton: Button? = null
    private var sevenButton: Button? = null
    private var eigthButton: Button? = null
    private var nineButton: Button? = null
    private var backSpaceButton: Button? = null
    private var firstCode: EditText? = null
    private var secondCode: EditText? = null
    private var thirdCode: EditText? = null
    private var fourtCode: EditText? = null
    private var fifthCode: EditText? = null
    private var sixtCode: EditText? = null

    private var padButtonsArray = ArrayList<Button?>()
    private var padEditText = ArrayList<EditText?>()
    private var codeHostArray = ArrayList<String>()
    private var rewardsObserver: DottysRedeemedRewardsObserver? = null

    /* REDEEEM REWARDS VIEW */
    fun initViewRedeem(activityRedeem: DottysRedeemRewardsActivity){
        this.activityRedeem = activityRedeem
        val title = activityRedeem.findViewById<TextView>(R.id.title_bar)
        avaibleButton = activityRedeem.findViewById<Button>(R.id.aviable_rewards_button)
        redeemedButton = activityRedeem.findViewById<Button>(R.id.redeemed_rewards_button)
        titleRedeem = activityRedeem.findViewById<TextView>(R.id.redeem_rewards_textview)

        title.text = "REDEEM REWARDS"

        activityRedeem.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activityRedeem.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        activityRedeem.window.statusBarColor = ContextCompat.getColor(activityRedeem, R.color.colorDottysGrey)
        val data = activityRedeem.intent.getStringExtra("REDEEM_REWARDS")
        redeemsUserData =  DottysRewardsModel.fromJson(data.toString())
        segmentTabLisener()
        viewSegmentSelectedHandler(segmentSelected)
        initListView()

    }

    fun initListView(){
        val listViewRewards = activityRedeem?.findViewById<ListView>(R.id.redeem_rewards_listview)
        var isRedeemed: Boolean = false
        if (segmentSelected == RedeemRewardsSegment.REDEEMED_REWARDS) {
            isRedeemed = true
        }
        listViewRewards?.adapter =
        redeemsUserData.rewards?.let { activityRedeem?.let { it1 -> DottysRedeemAdapter(it1, it.filter { it.redeemed == isRedeemed }) } }
        titleRedeem?.text =  attributedRedeemText(redeemsUserData.rewards?.filter{ it.redeemed == isRedeemed }?.size.toString())

    }

    fun segmentTabLisener(){
        avaibleButton?.setOnClickListener {
            segmentSelected = RedeemRewardsSegment.AVAIBLE_REWARDS
            viewSegmentSelectedHandler(segmentSelected)
            initListView()
        }
        redeemedButton?.setOnClickListener {
            segmentSelected = RedeemRewardsSegment.REDEEMED_REWARDS
            viewSegmentSelectedHandler(segmentSelected)
            initListView()
        }

    }

    private fun viewSegmentSelectedHandler(segment: RedeemRewardsSegment) {
       when(segment){
           RedeemRewardsSegment.AVAIBLE_REWARDS -> {
               activityRedeem?.let { ContextCompat.getColor(it,R.color.colorTransparent) }?.let {
                   avaibleButton?.setBackgroundColor(it)}
               activityRedeem?.let { ContextCompat.getColor(it,R.color.colorSelectedSegment) }?.let {
                   redeemedButton?.setBackgroundColor(it)}

           }
           RedeemRewardsSegment.REDEEMED_REWARDS -> {
               activityRedeem?.let { ContextCompat.getColor(it,R.color.colorSelectedSegment) }?.let {
                   avaibleButton?.setBackgroundColor(it)}
               activityRedeem?.let { ContextCompat.getColor(it,R.color.colorTransparent) }?.let {
                   redeemedButton?.setBackgroundColor(it)}

           }
       }
    }

    private fun attributedRedeemText(unclaimedRewards: String): SpannableString {
        var claimed = String()
        if (segmentSelected == RedeemRewardsSegment.AVAIBLE_REWARDS) {
            claimed = " unclaimed "
        } else {
            claimed = " claimed "
        }
        val spannable = SpannableString("You have "+unclaimedRewards+claimed+"rewards!")
        spannable.setSpan(
            ForegroundColorSpan(Color.YELLOW),
            8, 9 + unclaimedRewards.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
            StyleSpan(BOLD),
            0, spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),0, spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
            AbsoluteSizeSpan(22, true),
            0,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable

    }

    /* CASH REWARDS VIEW */
    fun initCashRewardsView(activityRewards: DottysCashRedeemRewardsActivity) {
        activityRewards.hideLoader(activityRewards)
        activityRewards.supportActionBar?.let {
            activityRewards.actionBarSetting(
                it,
                ColorDrawable(activityRewards.resources.getColor(R.color.colorDottysGrey))
            )
        }
        val titleBar = activityRewards.actionBarView?.findViewById<TextView>(R.id.title_bar)
        titleBar?.text = "REDEEM REWARDS"
        initRewardsItemsView(activityRewards)
        imageReedemSetting(activityRewards)
        swipeBUttonSetting(activityRewards)
        buttonsPadsLisener(activityRewards)
        congratsTextview?.text = "Congrats \n${activityRewards.getUserPreference().firstName}"
        rewardsObserver = DottysRedeemedRewardsObserver(activityRewards)


    }

    private fun initRewardsItemsView(activityRewards: DottysCashRedeemRewardsActivity) {
        start = activityRewards.findViewById<Swipe_Button_View>(R.id.start_swipe)
        codeVerificationLayout =
            activityRewards.findViewById<ConstraintLayout>(R.id.enter_code_layout)
        redeemImage = activityRewards.findViewById<ImageView>(R.id.redeem_rewards_image)
        congratsTextview = activityRewards.findViewById<TextView>(R.id.congrats_textview)
        zeroButton = activityRewards.findViewById<Button>(R.id.button_zero)
        oneButton = activityRewards.findViewById<Button>(R.id.button_one)
        twoButton = activityRewards.findViewById<Button>(R.id.button_two)
        threeButton = activityRewards.findViewById<Button>(R.id.button_three)
        fourButton = activityRewards.findViewById<Button>(R.id.button_four)
        fiveButton = activityRewards.findViewById<Button>(R.id.button_five)
        sixButton = activityRewards.findViewById<Button>(R.id.button_six)
        sevenButton = activityRewards.findViewById<Button>(R.id.button_seven)
        eigthButton = activityRewards.findViewById<Button>(R.id.button_eigth)
        nineButton = activityRewards.findViewById<Button>(R.id.button_nine)
        backSpaceButton = activityRewards.findViewById<Button>(R.id.button_backspace)

        firstCode = activityRewards.findViewById<EditText>(R.id.firts_host_code_edittext)
        secondCode = activityRewards.findViewById<EditText>(R.id.second_host_code_edittext)
        thirdCode = activityRewards.findViewById<EditText>(R.id.third_host_code_edittext)
        fourtCode = activityRewards.findViewById<EditText>(R.id.fourth_host_code_edittext)
        fifthCode = activityRewards.findViewById<EditText>(R.id.fifth_host_code_edittext)
        sixtCode = activityRewards.findViewById<EditText>(R.id.sixth_host_code_edittext)
        padEditText = arrayListOf(
            firstCode, secondCode, thirdCode,
            fourtCode, fifthCode, sixtCode
        )
        codeVerificationLayout?.animate()?.x(0.0f)?.y(0.0f)?.scaleY(0f)?.setDuration(0)?.start()
    }

    private fun editTextCodeManager(activityRewards: DottysCashRedeemRewardsActivity) {

        for (itemEdit in padEditText.indices) {
            if (itemEdit < codeHostArray.size) {
                padEditText.get(itemEdit)?.setText(codeHostArray.get(itemEdit))
            } else {
                padEditText.get(itemEdit)?.setText("")
            }
        }
        if (codeHostArray.size == 6 && validateStoreCode(activityRewards)) {
            redeemRewards(activityRewards, getHostCode())
        }
    }

    private fun validateStoreCode(activityRewards: DottysCashRedeemRewardsActivity): Boolean {
        val store = activityRewards.getUserNearsLocations().locations?.first()?.storeNumber ?: 0
        var code: String
        if (store < 10) {
            code = "0$store" + "0$store" + "0$store"
        } else if (store < 100) {
            code = "$store$store$store"
        } else {
            code = "$store$store"
        }
        if (code == getHostCode()) {
            return true
        }
        return false
    }

    private fun getHostCode(): String {
        var textCode = StringBuilder()
        for (item in codeHostArray) {
            textCode.append(item)
        }
        return textCode.toString()
    }

    private fun buttonsPadsLisener(activityRewards: DottysCashRedeemRewardsActivity) {
        padButtonsArray = arrayListOf(
            zeroButton, oneButton, twoButton, threeButton,
            fourButton, fiveButton, sixButton, sevenButton,
            eigthButton, nineButton, backSpaceButton
        )
        for (itemPad in padButtonsArray) {
            itemPad?.setOnClickListener {
                val buttonAuxliary = activityRewards.findViewById<Button>(itemPad.id)
                when (itemPad.id) {
                    R.id.button_backspace -> {
                        if (codeHostArray.size > 0) {
                            codeHostArray.removeAt(codeHostArray.size - 1)
                        }
                    }
                    else -> {
                        if (codeHostArray.size < 6) {
                            codeHostArray.add(buttonAuxliary.text.toString())
                        }
                    }
                }
                editTextCodeManager(activityRewards)
            }
        }
    }

    fun swipeBUttonSetting(activityRewards: DottysCashRedeemRewardsActivity) {
        start?.setBackgroundResource(R.drawable.shape_swipe_view)
        start?.setThumbBackgroundColor(activityRewards.resources.getColor(R.color.colorSecundaryDark))
        start?.setTextColor(activityRewards.resources.getColor(R.color.colorTextGrey))
        start?.setOnSwipeCompleteListener_forward_reverse(object : OnSwipeCompleteListener {
            override fun onSwipe_Forward(swipeView: Swipe_Button_View) {
                codeVerificationLayout?.animate()?.x(0.0f)?.y(0.0f)?.scaleY(1f)?.setDuration(350)
                    ?.start()
            }

            override fun onSwipe_Reverse(swipeView: Swipe_Button_View) {}
        })
    }

    fun imageReedemSetting(activityRewards: DottysCashRedeemRewardsActivity) {
        var imageParams = redeemImage?.layoutParams
        imageParams?.width =
            ((activityRewards.resources.displayMetrics?.widthPixels ?: 0) * 0.8).roundToInt()
        imageParams?.height =
            ((activityRewards.resources.displayMetrics?.heightPixels ?: 0) * 0.3).roundToInt()
        redeemImage?.layoutParams = imageParams
    }

    /* NETWORK REDEEM REWARDS  */
    fun redeemRewards(
        activityRewards: DottysCashRedeemRewardsActivity, hostCode: String) {
        val mQueue = Volley.newRequestQueue(activityRewards)
        activityRewards.showLoader(activityRewards)
        val params = HashMap<String, String>()
        if (activityRewards.getBeaconAtStoreLocation().locationID ?: "" == ""){
            Toast.makeText(activityRewards, "GO TO DOTTY'S LOCATION TO CHANGE CODE", Toast.LENGTH_LONG).show()

            return
        }
        params["hostCode"] = hostCode
        params["curLocationId"] = activityRewards.getBeaconAtStoreLocation().locationID ?: ""

        val jsonObject = JSONObject(params as Map<*, *>)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.PATCH,
            activityRewards.baseUrl + "rewards/redeem/" + activityRewards.rewardID,
            jsonObject,
            Response.Listener<JSONObject> {
                activityRewards.hideLoader(activityRewards)
                var redeemedReward: DottysRedeemResponseModel =
                    DottysRedeemResponseModel.fromJson(
                        it.toString()
                    )
                rewardsObserver?.rewardsRedeemed = redeemedReward
            },
            Response.ErrorListener { error ->
                activityRewards.hideLoader(activityRewards)
                val errorRes = DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                if (errorRes.error?.messages?.size ?: 0 > 0) {
                    Toast.makeText(activityRewards, errorRes.error?.messages?.first() ?: "", Toast.LENGTH_LONG).show()
                }
                Log.e("ERROR VOLLEY ", error.message, error)
            }) { //no semicolon or coma

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                print (response?.statusCode)
                return super.parseNetworkResponse(response)
            }

            override fun getHeaders(): MutableMap<String, String> {
                val params = java.util.HashMap<String, String>()
                params["Authorization"] = activityRewards.getUserPreference().token!!
                return params
            }
        }
        mQueue.add(jsonObjectRequest)
    }
}

/* REDEEMED REWARDS PROTOCOL */
//region
interface DottysRedeemedRewardsDelegates {
    fun getRedeemedRewards(dawingSummary: DottysRedeemResponseModel)

}

class DottysRedeemedRewardsObserver(lisener: DottysRedeemedRewardsDelegates) {
    var rewardsRedeemed: DottysRedeemResponseModel by Delegates.observable(
        initialValue = DottysRedeemResponseModel(),
        onChange = { prop, old, new -> lisener.getRedeemedRewards(new) })
}