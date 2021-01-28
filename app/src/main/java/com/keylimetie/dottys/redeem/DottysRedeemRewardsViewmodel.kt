package com.keylimetie.dottys.redeem

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.Typeface.BOLD
import android.graphics.drawable.ColorDrawable
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import com.agik.AGIKSwipeButton.Controller.OnSwipeCompleteListener
import com.agik.AGIKSwipeButton.View.Swipe_Button_View
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysErrorModel
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.ui.dashboard.DashboardViewModel
import com.keylimetie.dottys.ui.dashboard.DottysCurrentUserObserver
import com.keylimetie.dottys.ui.drawing.DrawingViewModel
import com.keylimetie.dottys.ui.drawing.RewardsSegment
import org.json.JSONObject
import java.text.NumberFormat
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
    private var swipeRewardsItem: Swipe_Button_View? = null
      var gifAnimatedImage: ImageView? = null

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

    /* REDEMEED REWARDS ITEMS */
    private var redemmedContainer: ImageView? = null
    // private var swipeRedemeedItem: Swipe_Button_View? = null

    /* DRAWING ITEMS */
    var dashBardViewModel = DashboardViewModel(null)
    var swipeType: RewardsSegment? = null
    /* REDEEEM REWARDS VIEW */
    fun initViewRedeem(activityRedeem: DottysRedeemRewardsActivity) {
        this.activityRedeem = activityRedeem
        activityRedeem.hideLoader()
        val title = activityRedeem.findViewById<TextView>(R.id.title_bar)
        avaibleButton = activityRedeem.findViewById<Button>(R.id.aviable_rewards_button)
        redeemedButton = activityRedeem.findViewById<Button>(R.id.redeemed_rewards_button)
        titleRedeem = activityRedeem.findViewById<TextView>(R.id.redeem_rewards_textview)

        title.text = "REDEEM REWARDS"

        activityRedeem.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activityRedeem.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        activityRedeem.window.statusBarColor =
            ContextCompat.getColor(activityRedeem, R.color.colorDottysGrey)
        val data = activityRedeem.intent.getStringExtra("REDEEM_REWARDS")

        redeemsUserData = DottysRewardsModel.fromJson(data.toString())
        segmentTabLisener()
        viewSegmentSelectedHandler(segmentSelected)
        initListView()

    }

      fun initListView() {
        val listViewRewards = activityRedeem?.findViewById<ListView>(R.id.redeem_rewards_listview)
        var isRedeemed: Boolean = false
        if (segmentSelected == RedeemRewardsSegment.REDEEMED_REWARDS) {
            isRedeemed = true
        }
        listViewRewards?.adapter =
            redeemsUserData.rewards?.let {
                activityRedeem?.let { it1 ->
                    DottysRedeemAdapter(
                        it1,
                        it.filter { it.redeemed == isRedeemed })
                }
            }
          redeemsUserData.rewards.let {
        titleRedeem?.text =
            attributedRedeemText(redeemsUserData.rewards?.filter { it.redeemed == isRedeemed }?.size.toString())
          }
          titleRedeem?.text =  attributedRedeemText("0")
    }

    private fun segmentTabLisener() {
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
        when (segment) {
            RedeemRewardsSegment.AVAIBLE_REWARDS -> {
                activityRedeem?.let { ContextCompat.getColor(it, R.color.colorTransparent) }?.let {
                    avaibleButton?.setBackgroundColor(it)
                }
                activityRedeem?.let { ContextCompat.getColor(it, R.color.colorSelectedSegment) }
                    ?.let {
                        redeemedButton?.setBackgroundColor(it)
                    }

            }
            RedeemRewardsSegment.REDEEMED_REWARDS -> {
                activityRedeem?.let { ContextCompat.getColor(it, R.color.colorSelectedSegment) }
                    ?.let {
                        avaibleButton?.setBackgroundColor(it)
                    }
                activityRedeem?.let { ContextCompat.getColor(it, R.color.colorTransparent) }?.let {
                    redeemedButton?.setBackgroundColor(it)
                }

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
        val spannable = SpannableString("You have " +   unclaimedRewards
          + claimed + "rewards!")
        spannable.setSpan(
            ForegroundColorSpan(Color.YELLOW),
            8, 9 + unclaimedRewards.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(BOLD),
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

    /* CASH REWARDS VIEW */
    fun initCashRewardsView(activityRewards: DottysCashRedeemRewardsActivity) {
        activityRewards.hideLoader()
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
        swipeButtonSetting(activityRewards)
        buttonsPadsLisener(activityRewards)
        congratsTextview?.text = "CONGRATS \n\n${activityRewards.getUserPreference().firstName}"
        rewardsObserver = DottysRedeemedRewardsObserver(activityRewards)


    }

    private fun initRewardsItemsView(activityRewards: DottysCashRedeemRewardsActivity) {
        swipeRewardsItem = activityRewards.findViewById<Swipe_Button_View>(R.id.start_swipe)
        gifAnimatedImage = activityRewards.findViewById<ImageView>(R.id.animated_gif_redeem_rewards)
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
        Glide.with(activityRewards).asGif().load(R.drawable.falling_money).into(gifAnimatedImage ?: return)
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
            /*MOCK REDEEM REWARDS*/
//            var intent = Intent(activityRewards, DottysRewardRedeemedActivity::class.java)
//            intent.putExtra("STORE_LOCATION", "")
//         activityRewards.  startActivity(intent)
            redeemRewards(activityRewards, getHostCode())
        } else if (codeHostArray.size == 6 && !validateStoreCode(activityRewards)) {
            Toast.makeText(activityRewards, "INVALID HOST CODE ID", Toast.LENGTH_LONG).show()
            codeHostArray = ArrayList<String>()
            editTextCodeManager(activityRewards)

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

    fun swipeButtonSetting(activityRewards: AppCompatActivity) {
        val pointsToRedeem = if (activityRewards is DottysRewardRedeemedActivity)
            (activityRewards).let {
                "Redeem ${it.drawing?.subtitle?.split(
            "Points")?.get(0)} points"
        }  else "Host Swipe to Redeem"
        swipeRewardsItem?.textView?.text = pointsToRedeem
        swipeRewardsItem?.textView?.textSize = 17f
        swipeRewardsItem?.textView?.setTextColor(activityRewards.getColor(R.color.colorTextGrey))
        swipeRewardsItem?.textView?.textAlignment = View.TEXT_ALIGNMENT_CENTER
         swipeRewardsItem?.textView?.typeface  = ResourcesCompat.getFont(activityRewards.applicationContext, R.font.proxima_nova_bold)
        swipeRewardsItem?.setBackgroundResource(R.drawable.shape_swipe_view)
        //swipeRewardsItem?.setThumbImage(activityRewards.resources.getDrawable(R.drawable.soda_win))
        swipeRewardsItem?.setThumbBackgroundColor(activityRewards.resources.getColor(R.color.colorSecundaryDark))
        swipeRewardsItem?.setTextColor(activityRewards.resources.getColor(R.color.colorTextGrey))
        swipeRewardsItem?.setOnSwipeCompleteListener_forward_reverse(object :
            OnSwipeCompleteListener {
            override fun onSwipe_Forward(swipeView: Swipe_Button_View) {
                if (activityRewards is DottysRewardRedeemedActivity) {
                    if (swipeType == null) {

                        dashBardViewModel.userCurrentUserDataObserver =
                            DottysCurrentUserObserver(activityRewards as DottysRewardRedeemedActivity)
                        dashBardViewModel.getUserRewards(activityRewards)
                    } else if (swipeType == RewardsSegment.DRAWING_ENTRIES) {
                        drawingPurchaseRequest(activityRewards)
                    } else {

                        cashRewardsRequest(activityRewards)
                    }
                } else {
                    codeVerificationLayout?.animate()?.x(0.0f)?.y(0.0f)?.scaleY(1f)
                        ?.setDuration(350)
                        ?.start()
                }
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
    private fun redeemRewards(
        activityRewards: DottysCashRedeemRewardsActivity, hostCode: String,
    ) {
        val mQueue = Volley.newRequestQueue(activityRewards)
        activityRewards.showLoader()
        val params = HashMap<String, String>()
        if (activityRewards.getBeaconStatus()?.beaconArray?.first()?.locationID ?: "" == "") {
            Toast.makeText(
                activityRewards,
                "GO TO DOTTY'S LOCATION TO CHANGE CODE",
                Toast.LENGTH_LONG
            ).show()

            return
        }
        params["hostCode"] = hostCode
        params["curLocationId"] = activityRewards.getBeaconStatus()?.beaconArray?.first()?.locationID ?: ""

        val jsonObject = JSONObject(params as Map<*, *>)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.PATCH,
            activityRewards.baseUrl + "rewards/redeem/" + activityRewards.rewardID.id,
            jsonObject,
            Response.Listener<JSONObject> {
                activityRewards.hideLoader()
                val redeemedReward: DottysRedeemResponseModel =
                    DottysRedeemResponseModel.fromJson(
                        it.toString()
                    )
                rewardsObserver?.rewardsRedeemed = redeemedReward
            },
            Response.ErrorListener { error ->
                activityRewards.hideLoader()
                if (error.networkResponse == null) {
                    return@ErrorListener
                }
                try {
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
                        Toast.makeText(
                            activityRewards,
                            errorRes.error?.messages?.first() ?: "",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Log.e("ERROR VOLLEY ", error.message, error)
                } catch (e: Error){
                    Log.e("ERROR ", e.toString())
                }
            }) { //no semicolon or coma

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                print(response?.statusCode)
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

    /* NETWORK DRAWING PURCHASE */
    fun drawingPurchaseRequest(
        drawingActivity: DottysRewardRedeemedActivity,
    ) {
        val mQueue = Volley.newRequestQueue(drawingActivity)
        drawingActivity.showLoader()
        //val jsonArray = JSONArray()
        val jsonObject = JSONObject()
        try {
            jsonObject.put("drawingId", drawingActivity.drawing?.id ?: "")
            jsonObject.put("entryCount", 1)
            //people is a integer

            //jsonArray.put(jsonObject)
            Log.i("jsonString", jsonObject.toString())
        } catch (e: Exception) {
        }

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            drawingActivity.baseUrl + "drawingEntries/purchase",
            jsonObject,
            Response.Listener<JSONObject> {
                drawingActivity.hideLoader()

            },
            Response.ErrorListener { error ->
                drawingActivity.hideLoader()
                if (error.networkResponse == null) {
                    return@ErrorListener
                }
                val errorRes =
                    DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                if (errorRes.error?.messages?.size ?: 0 > 0) {
                    Toast.makeText(
                        drawingActivity,
                        errorRes.error?.messages?.first() ?: "",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.e("ERROR VOLLEY ", error.message, error)
            }) { //no semicolon or coma

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                when(response?.statusCode){
                    201 -> {
                        rewardsObserver?.purchaseDrawing = true
                    }
                    else -> {
                        rewardsObserver?.purchaseDrawing = false
                    }
                }
                return super.parseNetworkResponse(response)
            }

//            override fun getParams(): MutableMap<String, String>? {
//                val params = HashMap<String, String>()
//                val qtty = 1
//                params["drawingId"] = drawingActivity.drawing?.id ?: ""
//                params["entryCount"] = qtty.toString()
//                return params
//            }
            override fun getHeaders(): MutableMap<String, String> {
                val params = java.util.HashMap<String, String>()
                params["Authorization"] = drawingActivity.getUserPreference().token!!
                return params
            }
        }
        mQueue.add(jsonObjectRequest)
    }

    /* NETWORK CASH REWARDS */
    fun cashRewardsRequest(
        drawingActivity: DottysRewardRedeemedActivity,
    ) {
        val mQueue = Volley.newRequestQueue(drawingActivity)
        drawingActivity.showLoader()
        val params = HashMap<String, String>()

        params["id"] = drawingActivity.getUserPreference().id ?: ""


        val jsonObject = JSONObject(params as Map<*, *>)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            drawingActivity.baseUrl + "rewards/pointsForCash/",
            jsonObject,
            Response.Listener<JSONObject> {
                drawingActivity.hideLoader()

            },
            Response.ErrorListener { error ->
                drawingActivity.hideLoader()
                if (error.networkResponse == null) {
                    return@ErrorListener
                }
                val errorRes =
                    DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                if (errorRes.error?.messages?.size ?: 0 > 0) {
                    Toast.makeText(
                        drawingActivity,
                        errorRes.error?.messages?.first() ?: "",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.e("ERROR VOLLEY ", error.message, error)
            }) { //no semicolon or coma

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                when(response?.statusCode){
                    200 -> {
                        rewardsObserver?.chashRewards = true
                    }
                    else -> {
                        rewardsObserver?.chashRewards = false
                    }
                }
                return super.parseNetworkResponse(response)
            }

            override fun getHeaders(): MutableMap<String, String> {
                val params = java.util.HashMap<String, String>()
                params["Authorization"] = drawingActivity.getUserPreference().token!!
                return params
            }
        }
        mQueue.add(jsonObjectRequest)
    }

    private fun generalViewSetting(view: DottysBaseActivity) {
        view.supportActionBar?.let {
            view.actionBarSetting(
                it,
                ColorDrawable(view.resources.getColor(R.color.colorDottysGrey))
            )
        }
        view.hideLoader()
        view.window.statusBarColor = ContextCompat.getColor(view, R.color.colorDottysGrey)
    }

    private fun containerTicketLayoutParams(view: DottysBaseActivity?, layout: ConstraintLayout):ConstraintLayout.LayoutParams{
        var containerParams = layout.layoutParams
        view?.windowManager?.defaultDisplay?.getMetrics(view?.displayMetrics)
        val containerWidthSize =
            ((view?.displayMetrics?.widthPixels ?: 0) * 0.85).roundToInt()
        containerParams?.width = containerWidthSize
        containerParams?.height =  ((view?.displayMetrics?.widthPixels ?: 0)*0.8).roundToInt() // containerWidthSize + (containerWidthSize * 0.2).roundToInt()
        return containerParams as ConstraintLayout.LayoutParams
    }

    /*REWARDS REDEMEE VIEW */
    fun initRewardRedeemedView(redemeedActivity: DottysRewardRedeemedActivity) {
        generalViewSetting(redemeedActivity)
        val titleBar = redemeedActivity.actionBarView?.findViewById<TextView>(R.id.title_bar)
        titleBar?.text = "REDEEM REWARDS"
        redemeedActivity.backButton ?: return
        redemeedActivity.backButton!!.visibility = View.INVISIBLE
      //  val image = redemeedActivity.findViewById<ImageView>(R.id.reward_barcode)
        val redemmedContainer =
            redemeedActivity.findViewById<ConstraintLayout>(R.id.ticket_redemeed_container)
        val drawingContainer =
            redemeedActivity.findViewById<ConstraintLayout>(R.id.drawing_layout_container)
        var redemmedCodeItem = redemeedActivity.findViewById<TextView>(R.id.redemeed_code_textview)
        drawingContainer.visibility = View.INVISIBLE
        redemmedCodeItem.text = redemeedActivity.rewardsRedemmed?.validationCode
        if (redemeedActivity.rewardsRedemmed?.barcode?.split(",")?.size ?: 0 > 0) {
            val bm = Base64.decode(redemeedActivity.rewardsRedemmed?.barcode?.split(",")?.get(1), 0)
           // image.setImageBitmap(BitmapFactory.decodeByteArray(bm, 0, bm.size))
        }

        redemmedContainer?.layoutParams = containerTicketLayoutParams(activityRedeem,
            redemmedContainer)

        swipeRewardsItem = redemeedActivity.findViewById<Swipe_Button_View>(R.id.start_swipe)
        swipeButtonSetting(redemeedActivity as DottysRewardRedeemedActivity)
    }

    /*DRAWING VIEW */
    @SuppressLint("DefaultLocale")
    fun initDrawingEntriesView(drawingActivity: DottysRewardRedeemedActivity) {
        generalViewSetting(drawingActivity)
        if (drawingActivity.rewardsTypeView == "DRAWING_ENTRIES") {
            swipeType = RewardsSegment.DRAWING_ENTRIES
        } else {
            swipeType = RewardsSegment.CASH_REWARDS
        }
        val titleBar = drawingActivity.actionBarView?.findViewById<TextView>(R.id.title_bar)
        titleBar?.text = "DRAWING ENTRIES"
        val redemmedContainer =
            drawingActivity.findViewById<ConstraintLayout>(R.id.ticket_redemeed_container)
        var redemmedCodeItem = drawingActivity.findViewById<TextView>(R.id.redemeed_code_textview)
        var titleDrawing = drawingActivity.findViewById<TextView>(R.id.title_drawing_textview)
        var wiredDrawingTextView = drawingActivity.findViewById<TextView>(R.id.wired_drawing_label)
       // var tagEntriesTextView = drawingActivity.findViewById<TextView>(R.id.tag_entries_textview)
        var qttyEntries = drawingActivity.findViewById<TextView>(R.id.quantity_entries)
        var subtitleDrawing = drawingActivity.findViewById<TextView>(R.id.subtitle_drawing_textview)
        var titleGeneral = drawingActivity.findViewById<TextView>(R.id.host_id_textview)
        var descriptioDrawinTextView = drawingActivity.findViewById<TextView>(R.id.you_earned_label)
        titleGeneral.alpha = 0f
        redemmedCodeItem.textSize = 18f
        redemmedCodeItem.letterSpacing = 0f
        val quantityEntries =  (drawingActivity.drawing?.quantity ?: 0) * 7
        val entriesType =  drawingActivity.drawing?.drawingType?.toLowerCase()?.capitalize() ?: ""
        val drawingViewModel = DrawingViewModel()
        descriptioDrawinTextView.text = drawingViewModel.attributedRedeemText(
            NumberFormat.getIntegerInstance()
                .format(drawingActivity.getUserPreference()?.points ?: (0).toLong()))
        rewardsObserver = DottysRedeemedRewardsObserver(drawingActivity)
        if (drawingActivity.rewardsTypeView == "CASH_REWARDS"){
           subtitleDrawing.text = drawingActivity.drawing?.subtitle
           redemmedCodeItem.text = "Visit a local Dotty's to redeem after purchase"
          // tagEntriesTextView.visibility = View.INVISIBLE
           wiredDrawingTextView.visibility = View.INVISIBLE
           titleDrawing.textSize = 30f
           titleDrawing.text = "CASH REWARDS"
            qttyEntries.text = drawingActivity.drawing?.subtitle?.split(" ")?.last() ?: ""
            redemmedContainer?.layoutParams = containerTicketLayoutParams(drawingActivity,
                redemmedContainer)

        } else {

           qttyEntries.text = drawingActivity.drawing?.quantity.toString()
           titleDrawing.text = drawingActivity.drawing?.title
           subtitleDrawing.text = drawingActivity.drawing?.subtitle
           redemmedCodeItem.text = "You can redeem your points for up to $quantityEntries entries into the $entriesType drawing."
           redemmedContainer?.layoutParams = containerTicketLayoutParams(activityRedeem,
               redemmedContainer)
       }
        swipeRewardsItem = drawingActivity.findViewById<Swipe_Button_View>(R.id.start_swipe)
        swipeButtonSetting(drawingActivity as DottysRewardRedeemedActivity)
    }
}

/* REDEEMED REWARDS PROTOCOL */
//region
interface DottysRedeemedRewardsDelegates {
    fun getRedeemedRewards(dawingSummary: DottysRedeemResponseModel)
    fun getPurchaseDrawing(isPurchase: Boolean)
    fun getCachRewards(isCashed: Boolean)

}

class DottysRedeemedRewardsObserver(lisener: DottysRedeemedRewardsDelegates) {
    var rewardsRedeemed: DottysRedeemResponseModel by Delegates.observable(
        initialValue = DottysRedeemResponseModel(),
        onChange = { prop, old, new -> lisener.getRedeemedRewards(new) })
    var purchaseDrawing: Boolean by Delegates.observable(
        initialValue = false,
        onChange = { prop, old, new -> lisener.getPurchaseDrawing(new) })
     var chashRewards: Boolean by Delegates.observable(
         initialValue = false,
         onChange = { prop, old, new -> lisener.getCachRewards(new) })
}