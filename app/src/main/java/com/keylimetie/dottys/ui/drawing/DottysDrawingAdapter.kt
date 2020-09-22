package com.keylimetie.dottys.ui.drawing

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.utils.getleftDays
import com.keylimetie.dottys.redeem.DottysRewardRedeemedActivity
import kotlin.math.roundToInt


class DottysDrawingAdapter(
    activity: Context,
    val activityFragment: DottysMainNavigationActivity,
    private val dataSource: List<DottysDrawing>,
    val segmentSelected: RewardsSegment
) : BaseAdapter() {

    var expireRewards: TextView? = null
    var backImage: ImageView? = null
    var titleRewards: TextView? = null

    // var subTitleRewards: TextView? = null
    var descriptionRewards: TextView? = null
    var leftLateralText: TextView? = null
    var rigthLateralText: TextView? = null
    var buttonRewards: Button? = null

    private val inflater: LayoutInflater =
        activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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
        val rowView = inflater.inflate(R.layout.drawing_fragment, parent, false)
        val params = rowView.layoutParams
        params.height = heigthForItem(parent)
        rowView.layoutParams = params
        val rewards = dataSource[position]
        expireRewards = rowView.findViewById<TextView>(R.id.expire_item_textview)
        backImage = rowView.findViewById<ImageView>(R.id.drawing_background_image)
        titleRewards = rowView.findViewById<TextView>(R.id.title_item_textview)
        descriptionRewards = rowView.findViewById<TextView>(R.id.description_item_textview)
        leftLateralText = rowView.findViewById<TextView>(R.id.lateral_left_textview)
        rigthLateralText = rowView.findViewById<TextView>(R.id.lateral_right_textview)
        buttonRewards = rowView.findViewById<Button>(R.id.rewards_button)
        fillItemsInView(rewards)
        return rowView
    }

    fun heigthForItem(parent: ViewGroup):Int{
        when(segmentSelected){
            RewardsSegment.DRAWING_ENTRIES -> {
                return (parent.height / 3.0).roundToInt()
            }
            RewardsSegment.CASH_REWARDS -> {
                return (parent.height / 3.0).roundToInt()
            }
        }
    }

    fun fillItemsInView(rewards: DottysDrawing) {
        var intent = Intent(activityFragment, DottysRewardRedeemedActivity::class.java)
        when (segmentSelected) {
            RewardsSegment.DRAWING_ENTRIES -> {
                leftLateralText?.visibility = View.VISIBLE
                rigthLateralText?.visibility = View.VISIBLE
                expireRewards?.visibility = View.VISIBLE
                //subTitleRewards?.visibility = View.INVISIBLE
                leftLateralText?.text = attributedRedeemText(rewards.quantity.toString())
                rigthLateralText?.text = attributedRedeemText(rewards.quantity.toString())
                titleRewards?.text = rewards.title
                descriptionRewards?.text =
                    rewards.priceInPoints.toString() + " Points for " + rewards.quantity + " Entries"
                expireRewards?.text =
                    "Expire in " + rewards.endDate?.let { it.getleftDays()  } + " days"
                backImage?.setImageDrawable(activityFragment.resources.getDrawable(R.mipmap.ticket_background_image))
                buttonRewards?.text = "Convert Points for Entries"
                intent.putExtra("REDEEM_REWARDS_VIEW_TYPE", "DRAWING_ENTRIES")

            }

            RewardsSegment.CASH_REWARDS -> {
                leftLateralText?.visibility = View.INVISIBLE
                rigthLateralText?.visibility = View.INVISIBLE
                expireRewards?.visibility = View.INVISIBLE
                //subTitleRewards?.visibility = View.VISIBLE
                titleRewards?.text = attributedRedeemText2(rewards.title ?: "")
                //subTitleRewards?.text = rewards.subtitle
                descriptionRewards?.text = rewards.subtitle
                backImage?.setImageDrawable(activityFragment.resources.getDrawable(R.mipmap.cash_rewards_background_image))
                buttonRewards?.text = "Convert Points for Cash"
                intent.putExtra("REDEEM_REWARDS_VIEW_TYPE", "CASH_REWARDS")
            }
        }
        buttonRewards?.setOnClickListener {
            intent.putExtra("DRAWING_DATA", rewards.toJson().toString())
            activityFragment.startActivity(intent)
        }
    }

    private  fun attributedRedeemText2(text:String): SpannableString {
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0, 3,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.NORMAL),
            0, spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AbsoluteSizeSpan(30, true), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    fun attributedRedeemText(point:String): SpannableString {
        val spannable = SpannableString("$point points!")
        spannable.setSpan(
            ForegroundColorSpan(Color.GREEN),
            0, point.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.NORMAL),
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
}
