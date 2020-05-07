package com.keylimetie.dottys.ui.profile

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.*
import com.keylimetie.dottys.forgot_password.DottysForgotPasswordMainActivity
import com.keylimetie.dottys.forgot_password.DottysVerificationTypeActivity
import de.hdodenhof.circleimageview.CircleImageView

class ProfileViewModel : ViewModel() {
    var imageViewProfile : CircleImageView? = null
    var nameProfileTextView : TextView? = null
    var sinceProfileTextView : TextView? = null

    private var firstNameEditText: EditText? = null
    private var lastNameEditText:  EditText? = null
    private var phoneEditText:     EditText? = null
    private var emailEditText:     EditText? = null
    private var passwordEditButton:  Button? = null
    private var myPlayLocation:    EditText? = null
    private var userLocation:      TextView? = null

   private var userData: DottysLoginResponseModel? = null

   fun initProfileView(
       rootView: View,
       activity: DottysMainNavigationActivity?
   ){
       userData = activity?.getUserPreference()
       imageViewProfile = rootView.findViewById<CircleImageView>(R.id.profile_image)
       nameProfileTextView = rootView.findViewById<TextView>(R.id.name_user_profile_textview)
       sinceProfileTextView = rootView.findViewById<TextView>(R.id.since_profile_textview)

       firstNameEditText =  rootView.findViewById<EditText>(R.id.first_name_profile_edit_text)
       lastNameEditText = rootView.findViewById<EditText>(R.id.last_name_profile_edit_text)
       phoneEditText = rootView.findViewById<EditText>(R.id.phone_profile_edit_text)
       emailEditText = rootView.findViewById<EditText>(R.id.email_profile_edit_text)
       passwordEditButton = rootView.findViewById<Button>(R.id.password_profile_edit_text)
       myPlayLocation = rootView.findViewById<EditText>(R.id.play_location_profile_edit_text)
       userLocation = rootView.findViewById<EditText>(R.id.user_location_textview)
       activity?.let {
           setImageProfile(it)
           setNameDataProfile()
       }

       passwordEditButton?.setOnClickListener {
           var intent = Intent(activity, DottysVerificationTypeActivity::class.java)
           intent.putExtra("EMAIL_FORGOT",   userData?.email ?: "")
           intent.putExtra("VIEW_FROM_PROFILE",   true)
           activity?.startActivity(intent)
       }
   }



    fun setNameDataProfile(){

        nameProfileTextView?.text = userData?.fullName
        sinceProfileTextView?.text = "Wired Member Since 2019"

        firstNameEditText?.setText(userData?.firstName)
        lastNameEditText?.setText(userData?.lastName)
        phoneEditText?.setText(userData?.cell)
        emailEditText?.setText(userData?.email)
        passwordEditButton?.setText("Change Password")
        myPlayLocation?.setText(userData?.zip)
        userLocation?.text = "Dottys\n ${userData?.address1}\n${userData?.city}. ${userData?.state} ${userData?.zip}"
    }
    fun setImageProfile(rootActivity: DottysBaseActivity){
        val email = rootActivity?.getUserPreference()?.email//"mrirenita@gmail.com"
        var url = "https://www.gravatar.com/avatar/" + email?.md5() + "?s=400&r=pg&d=404"
        if (rootActivity?.getUserPreference()?.profilePicture != null){
            url = rootActivity?.getUserPreference()?.profilePicture ?: ""
        }
        val mQueue = Volley.newRequestQueue(rootActivity)
        val request = ImageRequest(url,
            Response.Listener { bitmap ->
                imageViewProfile?.setImageBitmap(bitmap)
               // getLocationDrawing(fragment)
            }, 0, 0, null,
            Response.ErrorListener {
                imageViewProfile?.setImageResource(R.mipmap.default_profile_image)
            })
        mQueue.add(request)
    }
}

