package com.keylimetie.dottys.redeem

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.ui.drawing.DottysDrawing


class DottysRedeemRewardsActivity : DottysBaseActivity() {
    val sd = DottysDrawing()
    private val viewModel = DottysRedeemRewardsViewmodel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_redeem_rewards)
         this.supportActionBar?.let { actionBarSetting(it,
             ColorDrawable(resources.getColor(R.color.colorDottysGrey))
         ) }
       // dashboardViewModel.getCurrentUserRequest(this)

        viewModel.initViewRedeem(this)
     }

//    override fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel) {}
//
//    override fun getCurrentUser(currentUser: DottysLoginResponseModel) {
//        saveDataPreference(PreferenceTypeKey.USER_DATA,currentUser.toJson().toString())
//        viewModel.initViewRedeem(this)
//    }

//    override fun getUserRewards(rewards: DottysRewardsModel) {}
//
//    override fun getGlobalData(gloabalData: DottysGlobalDataModel) {}
//
//    override fun getDottysUserLocation(locationData: DottysDrawingRewardsModel) {}
//
//    override fun getBeaconList(beaconList: DottysBeaconsModel) {}


}
