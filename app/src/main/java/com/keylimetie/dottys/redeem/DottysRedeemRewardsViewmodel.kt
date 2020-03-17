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
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysRewardsModel

open class DottysRedeemRewardsViewmodel : ViewModel() {
var  redeemsUserData = DottysRewardsModel()
    fun initViewRedeem(activityRedeem: DottysRedeemRewardsActivity){
        val title = activityRedeem.findViewById<TextView>(R.id.title_bar)
        val titleRedeem = activityRedeem.findViewById<TextView>(R.id.redeem_rewards_textview)
        val listViewRewards = activityRedeem.findViewById<ListView>(R.id.redeem_rewards_listview)
        title.text = "REDEEM REWARDS"

        activityRedeem.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activityRedeem.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        activityRedeem.window.statusBarColor = ContextCompat.getColor(activityRedeem, R.color.colorDottysGrey)
        val data = activityRedeem.intent.getStringExtra("REDEEM_REWARDS")
        redeemsUserData =  DottysRewardsModel.fromJson(data.toString())
        titleRedeem.text =  attributedRedeemText(redeemsUserData.rewards?.filter{ it.redeemed == false }?.size.toString())

        listViewRewards.adapter =
            redeemsUserData.rewards?.let { DottysRedeemAdapter(activityRedeem, it.filter { it.redeemed == false }) }
     }

    fun attributedRedeemText(unclaimedRewards:String): SpannableString {

        val spannable = SpannableString("You have "+unclaimedRewards+" unclaimed rewards!")
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
