package com.keylimetie.dottys.ui.reusable_fragment

 import android.content.Context
 import android.view.View
 import android.widget.TextView
 import androidx.lifecycle.ViewModel
 import com.keylimetie.dottys.DottysMainNavigationActivity
 import com.keylimetie.dottys.R

class ReusableFragmentViewModel : ViewModel() {
    var itemSelected: Int? = null
    var textViewDescription: TextView? = null
//    private val _titleReusable = MutableLiveData<String>().apply {
//        value =  titleForView(itemSelected!!)
//    }
//    val titleReusable: LiveData<String> = _titleReusable
//
//    private val _descriptionReusable = MutableLiveData<String>().apply {
//        value =  ""//reusableFragment?.getString(R.string.default_wired_text)
//    }
//    val descriptionReusable: LiveData<String> = _descriptionReusable

    fun initViewSetting(
        root: View,
        itemId: Int,
        context: Context,
        activity: DottysMainNavigationActivity
    ){
        val textView: TextView = root.findViewById(R.id.title_reusable_textview)
        textViewDescription = root.findViewById(R.id.description_reusable_textview)
        textViewDescription?.text = context.getString(R.string.default_wired_text)
        textView.text = titleForView(itemId, activity)
    }

    fun titleForView(itemId:Int, activity: DottysMainNavigationActivity):String{
       when(itemId){
           R.id.nav_privacy_policy -> {
                return "Privacy Policy"
           }
           R.id.nav_terms_and_conditions -> {
               textViewDescription?.text  = activity.getGLobalData().terms ?: ""
               return "Terms & Conditions"
           }
           R.id.nav_contact_suppport -> {
               return "Contact Suppport"
           }
           else -> {return ""}
       }
    }
}
