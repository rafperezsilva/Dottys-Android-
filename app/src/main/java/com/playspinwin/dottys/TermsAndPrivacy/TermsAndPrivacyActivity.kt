package com.playspinwin.dottys.TermsAndPrivacy

import android.os.Bundle
import com.playspinwin.dottys.DottysBaseActivity
import com.playspinwin.dottys.R

class TermsAndPrivacyActivity: DottysBaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_privacy)
        TermsAndPrivacyActivityViewModel(this)
    }
}
