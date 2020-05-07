package com.keylimetie.dottys.ui.drawing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.R
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.ui.dashboard.*
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconsModel
import com.keylimetie.dottys.ui.dashboard.models.DottysDrawingSumaryModel
import java.text.NumberFormat

class DrawingFragment : Fragment(), DottysDrawingDelegates, DottysDashboardDelegates {

    private lateinit var drawingViewModel: DrawingViewModel
    private var dashboardViewModel =  DashboardViewModel()
    private var viewRoot: View? = null
     override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        drawingViewModel =
            ViewModelProviders.of(this).get(DrawingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_drawing, container, false)

         viewRoot = root
        return root
    }

    override fun onResume() {
        super.onResume()
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        dashboardViewModel.userCurrentUserDataObserver = DottysCurrentUserObserver(this)
        activity?.let { dashboardViewModel.getCurrentUserRequest(it) }

    }
    override fun getUserRewards(rewards: DottysDrawingRewardsModel) {
    }

    override fun getUserDrawings(drawing: DottysDrawingUserModel) {
        drawingViewModel.initListView()
    }

    override fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel) {}

    override fun getCurrentUser(currentUser: DottysLoginResponseModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.let { drawingViewModel.initViewSetting(this, null, it, viewRoot) }
        var userAux = activity?.getUserPreference()
        userAux?.points = currentUser.points

        activity?.saveDataPreference(PreferenceTypeKey.USER_DATA,userAux?.toJson().toString())
        drawingViewModel.titleTotalPoints?.text = drawingViewModel.attributedRedeemText(
            NumberFormat.getIntegerInstance()
                .format(currentUser.points)
        )
    }

    override fun getUserRewards(rewards: DottysRewardsModel) {}

    override fun getGlobalData(gloabalData: DottysGlobalDataModel) {}

    override fun getDottysUserLocation(locationData: DottysDrawingRewardsModel) {}

    override fun getBeaconList(beaconList: DottysBeaconsModel) { }

}