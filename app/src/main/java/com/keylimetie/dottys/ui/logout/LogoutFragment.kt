package com.keylimetie.dottys.ui.logout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R

class LogoutFragment: Fragment() {


    var viewModel =  LogoutViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.let { viewModel.logoutRequest(it) }
        return inflater.inflate(R.layout.logout_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LogoutViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
