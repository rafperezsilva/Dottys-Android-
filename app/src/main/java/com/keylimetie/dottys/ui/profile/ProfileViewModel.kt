package com.keylimetie.dottys.ui.profile

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.lifecycle.ViewModel
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.keylimetie.dottys.*
import com.keylimetie.dottys.forgot_password.DottysVerificationTypeActivity
import com.keylimetie.dottys.models.DottysErrorModel
import com.keylimetie.dottys.models.DottysLoginResponseModel
import com.keylimetie.dottys.register.DottysProfilePictureActivity
import com.keylimetie.dottys.utils.geofence.Constants
import com.keylimetie.dottys.utils.md5
import com.keylimetie.dottys.utils.stringGetYear
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.util.*
import kotlin.jvm.Throws
import kotlin.properties.Delegates

class ProfileViewModel(
    activityMain: DottysMainNavigationActivity?,
    profileData: DottysLoginResponseModel?
) : ViewModel(), View.OnClickListener,
    DottysOnProfilePictureTakenDelegate {
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
    private var switchNotification:      Switch? = null
    private var activity: DottysMainNavigationActivity? = activityMain
    private var context: Context? = null
    private var userData: DottysLoginResponseModel? = profileData
    public  var profileUpdateObserver: DottysProfileObserver? = null

    var fragent: ProfileFragment?  = null
    private val pictureActivity = DottysProfilePictureActivity()

    fun initProfileView(
        rootView: View,
        activity: DottysMainNavigationActivity?, context: Context, fragent: ProfileFragment,
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
       switchNotification = rootView.findViewById<Switch>(R.id.profile_switch_notification)
        switchNotification?.isChecked = activity?.isPushNotificationEnable() ?: false
        switchNotification?.setOnClickListener(this)
       imageViewProfile?.setOnClickListener(this)
       activity?.let {
           setImageProfile(it)
           setNameDataProfile()
       }

       passwordEditButton?.setOnClickListener(this)
   }



    private fun setNameDataProfile(){

        nameProfileTextView?.text = userData?.fullName
        sinceProfileTextView?.text = "Member Since ${userData?.createdAt?.stringGetYear()}"
        firstNameEditText?.setText(userData?.firstName ?: "")
        lastNameEditText?.setText(userData?.lastName)
        phoneEditText?.setText(userData?.cell)
        emailEditText?.setText(userData?.email)
        passwordEditButton?.text = "Change Password"
        myPlayLocation?.setText(userData?.zip)
        userData?.address1?.let {
            userLocation?.text =
                "${userData?.address1 ?: ""}\n${userData?.city ?: ""}. ${userData?.state ?: ""} ${userData?.zip ?: ""}"
        }
    }

    private fun setImageProfile(rootActivity: DottysBaseActivity){
        rootActivity.userPictureBM?.let {
            imageViewProfile?.setImageBitmap(it)
            return
        }
        val email = rootActivity?.getUserPreference()?.email
        var url = "https://www.gravatar.com/avatar/" + email?.md5() + "?s=400&r=pg&d=404"
        if (rootActivity?.getUserPreference()?.profilePicture != null){
            url = rootActivity?.getUserPreference()?.profilePicture ?: ""
        }
        val mQueue = Volley.newRequestQueue(rootActivity)
        val request = ImageRequest(url,
            { bitmap ->
                imageViewProfile?.setImageBitmap(bitmap)
                // getLocationDrawing(fragment)
            }, 0, 0, null,
            {
                imageViewProfile?.setImageResource(R.mipmap.default_profile_image)
            })
        mQueue.add(request)
    }


    fun getDataToUpdate() {
        val data = activity?.getUserPreference()
        data?.firstName = firstNameEditText?.text.toString()
        data?.lastName = lastNameEditText?.text.toString()
        data?.fullName = "${data?.firstName} ${data?.lastName}"
        data?.let { uploadProfile(it, activity) }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profile_switch_notification -> {
                activity?.saveDataPreference(PreferenceTypeKey.PUSH_NOTIFICATION, ((switchNotification?.isChecked ?: false)).toString())
                Log.i("SWICH ACTIVE ",switchNotification?.isChecked.toString())

            }
            R.id.profile_image -> {
                //TODO FIRST OPTION
                requestCameraPermission()
                //TODO SECOND OPTION
//                var intent = Intent(fragent?.context, DottysCameraActivity::class.java)
//                fragent?.startActivityForResult(intent,PICTURE_TAKE_REQUEST_CODE)
            }
            R.id.password_profile_edit_text -> {
                var intent = Intent(activity, DottysVerificationTypeActivity::class.java)
                intent.putExtra("EMAIL_FORGOT", userData?.email ?: "")
                intent.putExtra("VIEW_FROM_PROFILE", true)
                activity?.startActivity(intent)
            }
        }
    }

    private fun openCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
       activity?.image_uri = activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
           values)
        //camera intent


        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
          // cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
         //  cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
           cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        } else {
            cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, activity?.image_uri)
        activity?.cameraPictureObserver = DottysProfilePictureObserver(this)
        activity?.startActivityForResult(cameraIntent, pictureActivity.IMAGE_CAPTURE_CODE)
    }

    private fun requestCameraPermission(){
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
                Constants.PERMISSION_CODE
            )

        } else {
            openCamera()
        }
    }

    override fun onPictureTaken(bitmap: Bitmap?) {


        //val rotatedBM = bitmap?.rotateCustomBitmap( if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {-180f} else {0f})
        activity?.userPictureBM = bitmap
        imageViewProfile?.setImageBitmap(bitmap)

    }


      fun uploadProfile(
          profileData: DottysLoginResponseModel,
          activity: DottysBaseActivity?
      ) {
        val mQueue = Volley.newRequestQueue(activity)
       activity?.showLoader()
          profileData.cell = null
        val jsonProfile = JSONObject(profileData.toJson())
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.PATCH,
            activity?.baseUrl + "users/updateProfile",
            jsonProfile,
            Response.Listener<JSONObject> { response ->
                activity?.hideLoader()
                val userData: DottysLoginResponseModel =
                    DottysLoginResponseModel.fromJson(
                        response.toString()
                    )
                Log.d("PROFILE UPDATED", userData.toJson())
                var user = userData
                user.token = activity?.getUserPreference()?.token
                activity?.saveDataPreference(PreferenceTypeKey.USER_DATA, user.toJson())
                profileUpdateObserver?.updateProfile = userData


            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    activity?.hideLoader()

                    if (error.networkResponse == null) {
                        return
                    }
                    val errorRes =
                        DottysErrorModel.fromJson(error.networkResponse.data.toString(Charsets.UTF_8))
                    if (errorRes.error?.messages?.size ?: 0 > 0) {
//                        DottysBaseActivity().showSnackBarMessage(this,
//                            activity,
//                            errorRes.error?.messages?.first() ?: "",
//                            Toast.LENGTH_LONG
//                        ).show()
                    }
                    Log.e("TAG", error.message, error)
                 //    profileUpdateObserver?.updateProfile = null
                }
            }) { //no semicolon or coma

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = activity?.getCurrentToken() ?: ""
                return params
            }

        }
        mQueue.add(jsonObjectRequest)

    }


}

/* UPDATE PROFILE PROTOCOL */
//region
interface DottysProfileDelegates {
    fun onProfileUpdated(userProfile: DottysLoginResponseModel?)


}
class DottysProfileObserver(lisener: DottysProfileDelegates) {
    val initValue = DottysLoginResponseModel()
    var updateProfile: DottysLoginResponseModel by Delegates.observable(
        initialValue = initValue,
        onChange = { _, _, new -> lisener.onProfileUpdated(new) })

}



