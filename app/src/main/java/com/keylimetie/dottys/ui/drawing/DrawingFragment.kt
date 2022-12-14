package com.keylimetie.dottys.ui.drawing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.*
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.ui.dashboard.DashboardViewModel
import com.keylimetie.dottys.ui.dashboard.DottysCurrentUserObserver
import com.keylimetie.dottys.ui.dashboard.DottysDashboardDelegates
import com.keylimetie.dottys.ui.dashboard.models.DottysBannerModel
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconsModel
import com.keylimetie.dottys.ui.dashboard.models.DottysDrawingSumaryModel
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingUserModel
import java.text.NumberFormat

class DrawingFragment : Fragment(), DottysDrawingDelegates, DottysDashboardDelegates {

    private lateinit var drawingViewModel: DrawingViewModel
    private var dashboardViewModel: DashboardViewModel? = null
    private var viewRoot: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        drawingViewModel =
            ViewModelProviders.of(this).get(DrawingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_drawing, container, false)
//        val inflater2 = TransitionInflater.from(requireContext())
//        enterTransition = inflater2.inflateTransition(R.transition.slide_right)
        drawingViewModel.segmentLayout = root.findViewById(R.id.segment_drawing_layout)
        dashboardViewModel = DashboardViewModel(activity as DottysMainNavigationActivity)
        drawingViewModel.segmentLayout?.visibility = View.INVISIBLE
        viewRoot = root
        return root
    }

    override fun onResume() {
        super.onResume()
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.let { drawingViewModel.initViewSetting(this, null, it, viewRoot) }
        activity?.getUserPreference()?.let { fillItemsInView(it) }

        dashboardViewModel?.userCurrentUserDataObserver = DottysCurrentUserObserver(this)
        activity?.let { dashboardViewModel?.getCurrentUserRequest(it) }

    }


    override fun getUserRewards(rewards: DottysDrawingRewardsModel) {
    }

    override fun getUserDrawings(drawing: DottysDrawingUserModel) {
        //drawingViewModel.segmentSelected = RewardsSegment.DRAWING_ENTRIES
        drawingViewModel.initListView()
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        dashboardViewModel?.userCurrentUserDataObserver = DottysCurrentUserObserver(this)
        activity?.let { dashboardViewModel?.getCurrentUserRequest(it) }
    }


    override fun getDrawingSummary(dawingSummary: DottysDrawingSumaryModel) {}

    override fun getCurrentUser(currentUser: DottysLoginResponseModel) {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
//        activity?.let { drawingViewModel.initViewSetting(this, null, it, viewRoot) }
        var userAux = activity?.getUserPreference()
        userAux?.points = currentUser.points
        activity?.saveDataPreference(PreferenceTypeKey.USER_DATA, userAux?.toJson().toString())
        activity?.getUserPreference()?.let { fillItemsInView(it) }

    }

    //region
    override fun getUserRewards(rewards: DottysRewardsModel) {}

    override fun getGlobalData(gloabalData: DottysGlobalDataModel) {}

    override fun getDottysUserLocation(locationData: DottysDrawingRewardsModel) {}

    override fun getBeaconList(beaconList: DottysBeaconsModel) {}

    override fun onDashboardBanners(banners: DottysBannerModel) {}

    //endregion
    private fun fillItemsInView(currentUser: DottysLoginResponseModel) {
        drawingViewModel.titleTotalPoints?.text = drawingViewModel.attributedRedeemText(
            NumberFormat.getIntegerInstance()
                .format(currentUser.points ?: (0).toLong())
        )
    }
}