package com.playspinwin.dottys.redeem

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import com.playspinwin.dottys.DottysBaseActivity
import com.playspinwin.dottys.DottysMainNavigationActivity
import com.playspinwin.dottys.R


class   DottysRedeemRewardsActivity : DottysBaseActivity() {
    private val viewModel = DottysRedeemRewardsViewmodel()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_redeem_rewards)
         this.supportActionBar?.let { actionBarSetting(it,
             ColorDrawable(resources.getColor(R.color.colorDottysGrey))
         ) }
       // dashboardViewModel.getCurrentUserRequest(this)

        viewModel.initViewRedeem(this)
        setBackButton()
        backButton?.setOnClickListener {
            endActivity()
        }
     }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        endActivity()
        return false
    }

    private fun endActivity(){
        startActivity(Intent(this, DottysMainNavigationActivity::class.java))
        finish()
    }
    override fun onStop() {
        super.onStop()
        viewModel.gifAnimatedImage = null
    }

}
