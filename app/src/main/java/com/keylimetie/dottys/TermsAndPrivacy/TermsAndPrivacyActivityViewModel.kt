package com.keylimetie.dottys.TermsAndPrivacy

import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.keylimetie.dottys.DottysConstantItems
import com.keylimetie.dottys.R

open class TermsAndPrivacyActivityViewModel(mainActivity: TermsAndPrivacyActivity) : ViewModel() {
    private val pPWebView = mainActivity.findViewById<WebView>(R.id.privacy_policy_webview)
    private val termsTextContainer = mainActivity.findViewById<TextView>(R.id.terms_of_service_container_view)
    init {
        val typeView = mainActivity.intent.getStringExtra("TERMS_PRIVACY")
        termsTextContainer.isEnabled = false
        when (typeView){
            "TERMS" -> {
                initActionBar(mainActivity,"Terms of Service")
                initTermsOfService(mainActivity)
            }
            "PRIVACY" -> {
                initActionBar(mainActivity,"Privacy Policy")
                initPrivacyPolicy(mainActivity)
            }
        }

    }

    private fun initPrivacyPolicy(mainActivity: TermsAndPrivacyActivity){
        termsTextContainer.visibility = View.INVISIBLE
        val url = mainActivity.getGlobalData().privacyURL ?: DottysConstantItems.defaultPolicyURL
        pPWebView.settings.domStorageEnabled = true
        pPWebView.settings.javaScriptEnabled = true
        pPWebView.loadUrl(url)
//        pPWebView?.webViewClient = object : WebViewClient() {
//            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
//                super.onPageStarted(view, url, favicon)
//                mainActivity.showLoader()
//            }
//
//            override fun onPageFinished(view: WebView, url: String) {
//                super.onPageFinished(view, url)
//                mainActivity.hideLoader()
//            }
//
//        }
    }

   private fun initActionBar(mainActivity: TermsAndPrivacyActivity, title: String){
        mainActivity.supportActionBar?.let {
            mainActivity.actionBarSetting(
                it,
                ColorDrawable(mainActivity.resources.getColor(R.color.colorPrimary))
            )
        }
        val titleBar = mainActivity.actionBarView!!.findViewById<TextView>(R.id.title_bar)
        titleBar.text = title
    }

    private fun initTermsOfService(mainActivity: TermsAndPrivacyActivity){

        pPWebView.visibility = View.INVISIBLE
        termsTextContainer.text =  DottysConstantItems.termsAndConditionsDefault

    }
}