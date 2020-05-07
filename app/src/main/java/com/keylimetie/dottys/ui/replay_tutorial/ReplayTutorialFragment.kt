package com.keylimetie.dottys.ui.replay_tutorial

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer

import com.keylimetie.dottys.R
import com.keylimetie.dottys.ui.profile.ProfileViewModel

class ReplayTutorialFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel =
            ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.replay_tutorial_fragment, container, false)
        val textView: TextView = root.findViewById(R.id.replay_tutorial_textview)
//        profileViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
        return root
    }

}
