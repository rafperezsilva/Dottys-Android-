package com.keylimetie.dottys.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.redeem.DottysRedeemRewardsActivity
import com.keylimetie.dottys.ui.drawing.DottysDrawingDelegates
import com.keylimetie.dottys.ui.drawing.DottysDrawingModel
import com.keylimetie.dottys.ui.redeem_rewards.DottysRedeemRewrds
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class DashboardFragment : Fragment(), DottysDashboardDelegates, DottysDrawingDelegates {

    var homeViewModel = DashboardViewModel()
    var viewFragment: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        viewFragment = root
        return root
    }

    override fun onStart() {
        super.onStart()
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        if (activity != null) {
            homeViewModel.initDashboardViewSetting(this, activity)
        }

    }

    override fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.let { homeViewModel.getGlobalDataRequest(it) }
    }

    override fun getCurrentUser(currentUser: DottysLoginResponseModel) {

    }

    override fun getUserRewards(rewards: DottysRewardsModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        viewFragment?.let { activity?.let { it1 -> homeViewModel.addProfileImage(it1, it, this) } }
    }

    override fun getGlobalData(gloabalData: DottysGlobalDataModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.let { homeViewModel.initDashboardPager(it, gloabalData) }
    }

    override fun getDrawingUser(dawing: DottysDrawingModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?

        homeViewModel.initDashboardItemsView(viewFragment!!, dawing,activity!!)

        val redeemButton =
            viewFragment?.findViewById<Button>(com.keylimetie.dottys.R.id.redeem_rewards_button)
        redeemButton?.setOnClickListener {

            val intent = Intent(context, DottysRedeemRewardsActivity::class.java)
            intent.putExtra("REDEEM_REWARDS", homeViewModel.userCurrentUserDataObserver?.currentUserRewards?.toJson().toString())
            startActivity(intent)
//            // Get the text fragment instance
//            val imageFragment = DottysRedeemRewrds()
//
//            // Get the support fragment manager instance
//            val manager = activity.supportFragmentManager
//
//            // Begin the fragment transition using support fragment manager
//            val transaction = manager.beginTransaction()
//
//            // Replace the fragment on container
//            transaction.replace(R.id.nav_terms_and_conditions,imageFragment)
//            transaction.addToBackStack(null)
//
//            // Finishing the transition
//            transaction.commit()
        }


        }
    }
