package com.keylimetie.dottys.ui.profile

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.*
import com.keylimetie.dottys.forgot_password.DottysVerificationTypeActivity
import com.keylimetie.dottys.register.DottysProfilePictureActivity
import com.keylimetie.dottys.register.DottysRegisterUserDelegates
import com.keylimetie.dottys.register.DottysRegisterUserObserver
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.IOException

class ProfileViewModel : ViewModel(), View.OnClickListener, DottysOnProfilePictureTakenDelegate{
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
    private var activity: DottysMainNavigationActivity? = null
    private var context: Context? = null
    private var userData: DottysLoginResponseModel? = null

    var fragent: ProfileFragment?  = null
    val pictureActivity = DottysProfilePictureActivity()

    fun initProfileView(
       rootView: View,
       activity: DottysMainNavigationActivity?, context: Context, fragent:ProfileFragment
   ){
       this.fragent = fragent
       this.context = context
       this.activity = activity
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
       imageViewProfile?.setOnClickListener(this)
       activity?.let {
           setImageProfile(it)
           setNameDataProfile()
       }

       passwordEditButton?.setOnClickListener(this)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profile_image -> {
                requestCameraPermission()
            }
            R.id.password_profile_edit_text -> {
                var intent = Intent(activity, DottysVerificationTypeActivity::class.java)
                intent.putExtra("EMAIL_FORGOT",   userData?.email ?: "")
                intent.putExtra("VIEW_FROM_PROFILE",   true)
                activity?.startActivity(intent)
            }
        }
    }

    fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
       activity?.image_uri = activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, activity?.image_uri)
        activity?.cameraPictureObserver = DottysProfilePictureObserver(this)
        activity?.startActivityForResult(cameraIntent, pictureActivity.IMAGE_CAPTURE_CODE)
    }

    fun requestCameraPermission(){
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                activity!!, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                pictureActivity.PERMISSION_CODE
            )

        } else {
            openCamera()
        }
    }

    override fun onPictureTaken(bitmap: Bitmap?) {
        imageViewProfile?.setImageBitmap(bitmap)
    }


}

