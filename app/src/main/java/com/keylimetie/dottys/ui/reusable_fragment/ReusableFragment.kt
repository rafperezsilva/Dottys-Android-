package com.keylimetie.dottys.ui.reusable_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R

class ReusableFragment : Fragment() {



    private  var viewModel = ReusableFragmentViewModel()
     override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?

        viewModel =
            ViewModelProviders.of(this).get(ReusableFragmentViewModel::class.java)


        val root = inflater.inflate(R.layout.reusable_fragment, container, false)
        viewModel.initViewSetting(root, activity?.getSelectedItem()!!, activity)

        return root
    }

}
