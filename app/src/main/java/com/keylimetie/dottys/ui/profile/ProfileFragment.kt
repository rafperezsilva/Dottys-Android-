package com.keylimetie.dottys.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R

class ProfileFragment : Fragment() {

    private val profileViewModel = ProfileViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.hideLoader(activity)
        profileViewModel.initProfileView(root, activity)
        return root
    }
}