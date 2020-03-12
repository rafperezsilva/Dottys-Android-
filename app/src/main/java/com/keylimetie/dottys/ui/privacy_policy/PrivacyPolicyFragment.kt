package com.keylimetie.dottys.ui.privacy_policy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.R

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
        val textView: TextView = root.findViewById(R.id.text_share)
        privacyPolicyViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}