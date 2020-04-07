package com.keylimetie.dottys.redeem

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.KeyEvent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.ui.dashboard.DottysBeaconsModel
import com.keylimetie.dottys.ui.dashboard.DottysDashboardDelegates
import com.keylimetie.dottys.ui.dashboard.DottysDrawingSumaryModel
import com.keylimetie.dottys.ui.drawing.DottysDrawingRewardsModel

class DottysRewardRedeemedActivity : DottysBaseActivity(), DottysDashboardDelegates {
     var rewardsRedemmed: DottysRedeemResponseModel? = null
    val redemeedViewModel = DottysRedeemRewardsViewmodel()
    //val imageData = "data:image/PNG;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAABkEAIAAAAcdDQfAAABiUlEQVR42u3UMQ7DIBAAQcv//ytPwG2qGJzGG82UFMchoT3mV8eHMcccN+crc8z8v5m/nK/subtbfebb3rJ778oOz/7quXIlwBsIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWECGYAEZggVkCBaQIVhAhmABGYIFZAgWkCFYQIZgARmCBWQIFpAhWEDGBZ2uaNVxoYLJAAAAAElFTkSuQmCC"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_reward_redeemed)

        var dataLocation = intent.getStringExtra("STORE_LOCATION")
        rewardsRedemmed = DottysRedeemResponseModel.fromJson(dataLocation)
        redemeedViewModel.initRewardRedeemedView(this)


    }


    override fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel) { }

    override fun getCurrentUser(currentUser: DottysLoginResponseModel) {  }

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

    override fun onBackPressed() {
        super.onBackPressed()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
