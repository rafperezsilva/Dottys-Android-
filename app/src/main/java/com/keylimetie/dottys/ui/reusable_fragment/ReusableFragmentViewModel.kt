package com.keylimetie.dottys.ui.reusable_fragment

 import android.annotation.TargetApi
 import android.graphics.Bitmap
 import android.os.Build
 import android.telephony.mbms.DownloadProgressListener
 import android.util.Log
 import android.view.View
 import android.webkit.DownloadListener
 import android.webkit.WebView
 import android.webkit.WebViewClient
 import androidx.lifecycle.ViewModel
 import com.keylimetie.dottys.DottysConstantItems
 import com.keylimetie.dottys.DottysMainNavigationActivity
 import com.keylimetie.dottys.R
 import android.widget.TextView


class ReusableFragmentViewModel : ViewModel() {
    var textViewDescription: TextView? = null
    var webView: WebView? = null
    var textView: TextView? = null

    fun initViewSetting(
        root: View,
        itemId: Int,
        activity: DottysMainNavigationActivity
    ){
        activity.hideLoader()
        textView = root.findViewById(R.id.title_reusable_textview)
        textViewDescription = root.findViewById(R.id.description_reusable_textview)
        webView = root.findViewById<WebView>(R.id.fragment_web_view)
        textViewDescription?.isEnabled = false
        textViewDescription?.text = activity.getGlobalData().terms ?: DottysConstantItems.termsAndConditionsDefault
         titleForView(itemId, activity)
    }

    private fun titleForView(itemId:Int, activity: DottysMainNavigationActivity){

        return when(itemId){
            R.id.nav_privacy_policy -> {
                textViewDescription?.visibility = View.INVISIBLE
                webView?.visibility = View.VISIBLE
                webView?.settings?.javaScriptEnabled = true
                webView?.loadUrl(activity.getGlobalData().privacyURL ?: DottysConstantItems.defaultPolicyURL)
//                webView?.webViewClient = object : WebViewClient() {
//                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
//                        super.onPageStarted(view, url, favicon)
//                        activity.showLoader()
//                    }
//
//                    override fun onPageFinished(view: WebView, url: String) {
//                        super.onPageFinished(view, url)
//                         activity.hideLoader()
//                    }
//
//                }
                textView?.text = "Privacy Policy"
            }
            R.id.nav_terms_and_conditions -> {
                activity.hideLoader()
                webView?.visibility = View.INVISIBLE
                textView?.text =  "Terms & Conditions"
            }
            R.id.nav_contact_suppport -> {
                textView?.text =   "Contact Suppport"
            }
            else -> {
                textView?.text =   ""
            }
        }
    }


}
