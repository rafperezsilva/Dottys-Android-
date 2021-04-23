package com.playspinwin.dottys.ui.reusable_fragment

 import android.os.Build
 import android.util.Log
 import android.view.View
 import android.webkit.DownloadListener
 import android.webkit.WebChromeClient
 import android.webkit.WebView
 import androidx.lifecycle.ViewModel
 import com.playspinwin.dottys.DottysConstantItems
 import com.playspinwin.dottys.DottysMainNavigationActivity
 import com.playspinwin.dottys.R
 import android.widget.TextView
 import androidx.annotation.RequiresApi


class ReusableFragmentViewModel : ViewModel(), DownloadListener {
    var textViewDescription: TextView? = null
    var webView: WebView? = null
    var textView: TextView? = null

    @RequiresApi(Build.VERSION_CODES.O)
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
        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                Log.e("Page loading"," $newProgress%")
                if (newProgress == 100) {
                    activity.hideLoader()
                }
            }
        }
    }

    private fun titleForView(itemId:Int, activity: DottysMainNavigationActivity){
        activity.hideLoader()
         when(itemId){

            R.id.nav_privacy_policy, R.id.nav_terms_and_conditions -> {
                textViewDescription?.visibility = View.INVISIBLE
                webView?.visibility = View.VISIBLE
                webView?.settings?.javaScriptEnabled = true
                webView?.setDownloadListener(this)
                if (itemId == R.id.nav_privacy_policy) {
                    webView?.loadUrl(DottysConstantItems.privacyPolicyURL)
                    textView?.text = "Privacy Policy"
                } else {
                    webView?.loadUrl(DottysConstantItems.termsAndConditionsURL)
                    textView?.text =  "Terms & Conditions"
                }

            }
            R.id.nav_contact_suppport -> {
                textView?.text =   "Contact Suppport"
            }
            else -> {
                webView?.loadUrl(DottysConstantItems.appSupportUrl)
                textView?.text =   "Support"

            }
        }
    }

    override fun onDownloadStart(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimetype: String?,
        contentLength: Long
    ) {

    }


}
