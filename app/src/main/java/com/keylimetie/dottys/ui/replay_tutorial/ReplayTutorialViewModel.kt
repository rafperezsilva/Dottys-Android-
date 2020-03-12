package com.keylimetie.dottys.ui.replay_tutorial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReplayTutorialViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is Replay Tutorial Fragment"
    }
    val text: LiveData<String> = _text
}
