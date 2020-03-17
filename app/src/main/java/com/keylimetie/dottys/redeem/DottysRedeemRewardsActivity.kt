package com.keylimetie.dottys.redeem

import android.graphics.drawable.ColorDrawable
import android.os.Bundle

import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R


class DottysRedeemRewardsActivity : DottysBaseActivity() {

    private val viewModel = DottysRedeemRewardsViewmodel()

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_redeem_rewards)
         this.supportActionBar?.let { actionBarSetting(it,
             ColorDrawable(resources.getColor(R.color.colorDottysGrey))
         ) }
         viewModel.initViewRedeem(this)
     }

}
