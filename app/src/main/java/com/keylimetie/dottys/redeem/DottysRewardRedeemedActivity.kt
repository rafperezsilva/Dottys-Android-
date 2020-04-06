package com.keylimetie.dottys.redeem

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.keylimetie.dottys.R

class DottysRewardRedeemedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_reward_redeemed)
        val image = findViewById<ImageView>(R.id.reward_barcode)

        var dataLocation = intent.getStringExtra("STORE_LOCATION")
        var rewardsRedemmed: DottysRedeemResponseModel =
            DottysRedeemResponseModel.fromJson(
                dataLocation
            )
        val encodedString = "data:image/jpg;base64, ...."
        if (rewardsRedemmed.barcode?.split(",")?.size ?: 0 > 0) {
            val bm = Base64.decode(rewardsRedemmed.barcode?.split(",")?.get(1), 0)
            image.setImageBitmap(BitmapFactory.decodeByteArray(bm, 0, bm.size))
        }
    }
}
