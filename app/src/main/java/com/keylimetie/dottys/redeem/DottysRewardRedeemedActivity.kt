package com.keylimetie.dottys.redeem

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import com.keylimetie.dottys.*
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.ui.dashboard.DottysDashboardDelegates
import com.keylimetie.dottys.ui.dashboard.models.DottysBannerModel
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconsModel
import com.keylimetie.dottys.ui.dashboard.models.DottysDrawingSumaryModel
import com.keylimetie.dottys.ui.drawing.models.DottysDrawing
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.locations.showSnackBarMessage

class DottysRewardRedeemedActivity : DottysBaseActivity(), DottysDashboardDelegates, DottysRedeemedRewardsDelegates {
    var rewardsRedemmed: DottysRedeemResponseModel? = null
    var drawing: DottysDrawing? = null
    val redemeedViewModel = DottysRedeemRewardsViewmodel()
    var rewardsTypeView: String? = null
    //val imageData = "data:image/png;base64,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"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_reward_redeemed)
     /// rewardsRedemmed = DottysRedeemResponseModel()

        rewardsTypeView = intent.getStringExtra("REDEEM_REWARDS_VIEW_TYPE") ?: ""
        if (rewardsTypeView?.isEmpty() ?: true) {
            var dataLocation = intent.getStringExtra("STORE_LOCATION")
            rewardsRedemmed = dataLocation?.let { DottysRedeemResponseModel.fromJson(it) }
            redemeedViewModel.initRewardRedeemedView(this)
        } else {
            var drawingData = intent.getStringExtra("DRAWING_DATA")
            drawing = drawingData?.let { DottysDrawing.fromJson(it) }
            redemeedViewModel.initDrawingEntriesView(this)
        }
        setBackButton()
    }

    override fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel) { }

    override fun getCurrentUser(currentUser: DottysLoginResponseModel) {
        saveDataPreference(PreferenceTypeKey.USER_DATA, currentUser.toJson().toString())
        finish()
    }

    override fun getUserRewards(rewards: DottysRewardsModel) {
        val intent = Intent(this, DottysRedeemRewardsActivity::class.java)
        intent.putExtra("REDEEM_REWARDS", rewards.toJson().toString())
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun getGlobalData(gloabalData: DottysGlobalDataModel) {}

    override fun getDottysUserLocation(locationData: DottysDrawingRewardsModel) {}

    override fun getBeaconList(beaconList: DottysBeaconsModel) {}

    override fun onDashboardBanners(banners:  DottysBannerModel) {}

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && drawing == null) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun getRedeemedRewards(dawingSummary: DottysRedeemResponseModel) {
            showSnackBarMessage(this, "HAS REDEEMED")
    }

    override fun getPurchaseDrawing(isPurchase: Boolean) {
       finish()
    }

    override fun getCachRewards(isCashed: Boolean) {
        finish()
    }
}
