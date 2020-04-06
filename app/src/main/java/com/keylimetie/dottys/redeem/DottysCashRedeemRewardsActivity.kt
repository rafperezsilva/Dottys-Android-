package com.keylimetie.dottys.redeem

import android.content.Intent
import android.os.Bundle
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysRewardsModel


class DottysCashRedeemRewardsActivity : DottysBaseActivity(), DottysRedeemedRewardsDelegates {
    private var redeemReawrdsViewModel = DottysRedeemRewardsViewmodel()
    var rewardID = String()
    var rewardLocation = String()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_cash_redeem_rewards)
        window.statusBarColor = resources.getColor(R.color.colorDottysGrey)
        rewardID  = intent.getStringExtra("REWARD_ID")
        redeemReawrdsViewModel.initCashRewardsView(this)




        // redeemReawrdsViewModel.redeemRewards(this, reardsObject.id ?: "","",reardsObject.locationID ?: "")

    }

    override fun getRedeemedRewards(dawingSummary: DottysRedeemResponseModel) {
        var intent = Intent(this, DottysRewardRedeemedActivity::class.java)
        intent.putExtra("STORE_LOCATION", dawingSummary.toJson().toString())
        startActivity(intent)
    }

}
