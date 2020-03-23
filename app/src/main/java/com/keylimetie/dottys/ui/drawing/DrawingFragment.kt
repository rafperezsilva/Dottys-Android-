package com.keylimetie.dottys.ui.drawing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R

class DrawingFragment : Fragment(), DottysDrawingDelegates {

    private lateinit var drawingViewModel: DrawingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        drawingViewModel =
            ViewModelProviders.of(this).get(DrawingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_drawing, container, false)
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?

        activity?.let { drawingViewModel.initViewSetting(this, null, it, root) }
        return root
    }

    override fun getUserRewards(rewards: DottysDrawingRewardsModel) {
    }

    override fun getUserDrawings(drawing: DottysDrawingUserModel) {
    }


}