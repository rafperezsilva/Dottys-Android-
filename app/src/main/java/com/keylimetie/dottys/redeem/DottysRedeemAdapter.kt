package com.keylimetie.dottys.redeem

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysReward
import com.keylimetie.dottys.models.IconType
import kotlin.math.roundToInt


class DottysRedeemAdapter(private val activity: DottysRedeemRewardsActivity,
                          private val dataSource: List<DottysReward>): BaseAdapter() {
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
        val params = rowView.layoutParams
        params.height = (parent.height / 3.5).roundToInt()
        rowView.layoutParams = params
        val rewards = dataSource[position]
        val imageRewards = rowView.findViewById<ImageView>(R.id.reward_image_item)
        val expireRewards = rowView.findViewById<TextView>(R.id.expire_item_textview)
        val titleRewards = rowView.findViewById<TextView>(R.id.title_item_textview)
        val descriptionRewards = rowView.findViewById<TextView>(R.id.description_item_textview)
        titleRewards.text = rewards.title
        descriptionRewards.text = rewards.description
        expireRewards.text = "Expire in "+ rewards.endDate?.let { activity.getDiferencesDays(it) }+" days"
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

        return rowView
    }

}