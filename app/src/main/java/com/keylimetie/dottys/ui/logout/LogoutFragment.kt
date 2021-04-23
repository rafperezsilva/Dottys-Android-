package com.keylimetie.dottys.ui.logout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.utils.BlurDrawable


class LogoutFragment: Fragment() {


    var viewModel =  LogoutViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.let { viewModel.logoutRequest(it) }
        val root = inflater.inflate(R.layout.logout_fragment, container, false)
        val bluredView = root.findViewById<ImageView>(R.id.blured_view)
        val blurView = BlurDrawable(root, 10)
        bluredView.background = blurView

        return root
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)?.supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LogoutViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
