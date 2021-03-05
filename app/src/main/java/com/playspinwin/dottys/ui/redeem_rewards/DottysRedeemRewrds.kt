package com.playspinwin.dottys.ui.redeem_rewards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.playspinwin.dottys.R

class DottysRedeemRewrds : Fragment() {

    companion object {
        fun newInstance() =
            DottysRedeemRewrds()
    }

    private lateinit var viewModel: DottysRedeemRewrdsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dottys_redeem_rewrds_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DottysRedeemRewrdsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
