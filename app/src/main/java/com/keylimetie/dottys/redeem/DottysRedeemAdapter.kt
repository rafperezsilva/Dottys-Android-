package com.keylimetie.dottys.redeem

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysRewardModel
import com.keylimetie.dottys.models.IconType
import com.keylimetie.dottys.utils.getleftDays
import com.keylimetie.dottys.utils.monthDayYear
import com.keylimetie.dottys.utils.stringToDate
import com.keylimetie.dottys.utils.timeFromDate


class DottysRedeemAdapter(private val activity: DottysRedeemRewardsActivity,
                          private val dataSource: List<DottysRewardModel>
) : BaseAdapter() {
    private val inflater: LayoutInflater
            = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    //1
    override fun getCount(): Int {
        return dataSource.size
    }

    //2
    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item

        val rowView = inflater.inflate(R.layout.redeem_rewards_item, parent, false)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
       // params.height = (parent.height / 3.2).roundToInt()
        rowView.layoutParams = params
        val rewards = dataSource[position]
        val imageRewards = rowView.findViewById<ImageView>(R.id.reward_image_item)
        val expireRewards = rowView.findViewById<TextView>(R.id.expire_item_textview)
        val titleRewards = rowView.findViewById<TextView>(R.id.title_item_textview)
        val descriptionRewards = rowView.findViewById<TextView>(R.id.description_item_textview)
        val redeemRewardsLayout = rowView.findViewById<RelativeLayout>(R.id.redeem_rewards_relative)
        val rewardsForCashButton = rowView.findViewById<Button>(R.id.rewards_for_cash_button)
        val redeemedDateTextView = rowView.findViewById<TextView>(R.id.redeemed_date_textview)
        val redeemedLocationTextView = rowView.findViewById<TextView>(R.id.redeemed_location_textview)

        titleRewards.text = rewards.title
        descriptionRewards.text = rewards.description
        expireRewards.text = "Expire in "+ rewards.endDate?.let {it.getleftDays() }+" days"
        when (rewards.iconType) {
            IconType.PointsToDollars -> {
                imageRewards.setImageResource(R.mipmap.cash_image)
            }
            else -> {
                imageRewards.setImageResource(R.mipmap.cash_10_image)
                imageRewards.scaleX = 0.8F
                imageRewards.scaleY = 0.8F
            }
        }
        var paramsRelative = redeemRewardsLayout.layoutParams
        if (rewards.redeemed == true) {
            val dateEndedString = rewards.redeemedDate?.stringToDate()?.monthDayYear()
            val timeEndedString = rewards.redeemedDate?.stringToDate()?.timeFromDate()
            paramsRelative.height = params.height
            redeemRewardsLayout.visibility = View.VISIBLE
            rewardsForCashButton.isEnabled = false
            redeemedDateTextView.text = "$dateEndedString @ $timeEndedString"
            redeemedLocationTextView.text = rewards.location
        } else {
            rewardsForCashButton.isEnabled = true
            paramsRelative.height = 1
            redeemRewardsLayout.visibility = View.INVISIBLE
        }
        redeemRewardsLayout.layoutParams = paramsRelative
        rewardsForCashButton?.setOnClickListener {
            val intent = Intent(activity, DottysCashRedeemRewardsActivity::class.java)
            //intent.putExtra("LOCATION_ID",rewards.locationID)
            intent.putExtra("REWARD_ID",rewards.toJson())

            activity.startActivity(intent)
        }

        return rowView
    }

}