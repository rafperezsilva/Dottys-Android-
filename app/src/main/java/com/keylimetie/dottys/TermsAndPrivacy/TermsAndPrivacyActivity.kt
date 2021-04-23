package com.keylimetie.dottys.TermsAndPrivacy

import android.os.Bundle
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R

class TermsAndPrivacyActivity: DottysBaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_privacy)
        TermsAndPrivacyActivityViewModel(this)
    }
}
