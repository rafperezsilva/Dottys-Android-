package com.keylimetie.dottys.redeem

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconsModel
import com.keylimetie.dottys.ui.dashboard.DottysDashboardDelegates
import com.keylimetie.dottys.ui.dashboard.models.DottysDrawingSumaryModel
import com.keylimetie.dottys.ui.drawing.DottysDrawing
import com.keylimetie.dottys.ui.drawing.DottysDrawingRewardsModel

class DottysRewardRedeemedActivity : DottysBaseActivity(), DottysDashboardDelegates, DottysRedeemedRewardsDelegates {
    var rewardsRedemmed: DottysRedeemResponseModel? = null
    var drawing: DottysDrawing? = null
    val redemeedViewModel = DottysRedeemRewardsViewmodel()
    var rewardsTypeView: String? = null
    //val imageData = "data:image/PNG;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAABkEAIAAAAcdDQfAAABiUlEQVR42u3UMQ7DIBAAQcv//ytPwG2qGJzGG82UFMchoT3mV8eHMcccN+crc8z8v5m/nK/subtbfebb3rJ778oOz/7quXIlwBsIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWEDGBZ2uaNVxoYLJAAAAAElFTkSuQmCC"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_reward_redeemed)
        rewardsTypeView = intent.getStringExtra("REDEEM_REWARDS_VIEW_TYPE") ?: ""
        if (rewardsTypeView?.isEmpty() ?: true) {
            var dataLocation = intent.getStringExtra("STORE_LOCATION")
            rewardsRedemmed = DottysRedeemResponseModel.fromJson(dataLocation)
            redemeedViewModel.initRewardRedeemedView(this)
        } else {
            var drawingData = intent.getStringExtra("DRAWING_DATA")
            drawing =  DottysDrawing.fromJson(drawingData)
            redemeedViewModel.initDrawingEntriesView(this)
        }
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

    override fun getGlobalData(gloabalData: DottysGlobalDataModel) {

    }

    override fun getDottysUserLocation(locationData: DottysDrawingRewardsModel) {
    }

    override fun getBeaconList(beaconList: DottysBeaconsModel) {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && drawing == null) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun getRedeemedRewards(dawingSummary: DottysRedeemResponseModel) {

    }

    override fun getPurchaseDrawing(isPurchase: Boolean) {
       finish()
    }

    override fun getCachRewards(isCashed: Boolean) {
        finish()
    }
}
