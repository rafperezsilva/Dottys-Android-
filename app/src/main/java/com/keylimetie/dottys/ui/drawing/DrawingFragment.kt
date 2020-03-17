package com.keylimetie.dottys.ui.drawing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.R

class DrawingFragment : Fragment(), DottysDrawingDelegates {

    private lateinit var toolsViewModel: DrawingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toolsViewModel =
            ViewModelProviders.of(this).get(DrawingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_tools, container, false)
        val textView: TextView = root.findViewById(R.id.text_tools)
        toolsViewModel.text.observe(this, Observer {
            textView.text = it
        })
        toolsViewModel.initViewSetting(this, null)
        return root
    }

    override fun getDrawingUser(dawing: DottysDrawingModel) {

    }
}