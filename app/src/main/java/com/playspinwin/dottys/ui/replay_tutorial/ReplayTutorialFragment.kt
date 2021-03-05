package com.playspinwin.dottys.ui.replay_tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.playspinwin.dottys.R
import com.playspinwin.dottys.ui.profile.ProfileViewModel

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
