package com.playspinwin.dottys.ui.privacy_policy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.playspinwin.dottys.DottysMainNavigationActivity
import com.playspinwin.dottys.R

class PrivacyPolicyFragment : Fragment() {

    private lateinit var privacyPolicyViewModel: PrivacyPolicyModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        privacyPolicyViewModel =
            ViewModelProviders.of(this).get(PrivacyPolicyModel::class.java)
        val root = inflater.inflate(R.layout.fragment_share, container, false)
        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?

        return root
    }
}