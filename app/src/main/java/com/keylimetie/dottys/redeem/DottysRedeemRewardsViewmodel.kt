package com.keylimetie.dottys.redeem

import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.WindowManager
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysRewardsModel
enum class RedeemRewardsSegment {
    AVIABLE_REWARDS, REDEEMED_REWARDS
}
open class DottysRedeemRewardsViewmodel : ViewModel() {

    var avaibleButton: Button? = null
    var redeemedButton: Button? = null
    var segmentSelected: RedeemRewardsSegment = RedeemRewardsSegment.AVIABLE_REWARDS
    var  redeemsUserData = DottysRewardsModel()
    var activityRedeem: DottysRedeemRewardsActivity? = null
    var titleRedeem: TextView? = null
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
            segmentSelected = RedeemRewardsSegment.AVIABLE_REWARDS
            viewSegmentSelectedHandler(segmentSelected)
            initListView()
        }
        redeemedButton?.setOnClickListener {
            segmentSelected = RedeemRewardsSegment.REDEEMED_REWARDS
            viewSegmentSelectedHandler(segmentSelected)
            initListView()
        }

    }

    fun viewSegmentSelectedHandler(segment:RedeemRewardsSegment){
       when(segment){
           RedeemRewardsSegment.AVIABLE_REWARDS -> {
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

    fun attributedRedeemText(unclaimedRewards:String): SpannableString {
        var claimed = String()
        if (segmentSelected == RedeemRewardsSegment.AVIABLE_REWARDS){
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

}
