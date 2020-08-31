package com.keylimetie.dottys.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.fragment.app.Fragment
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.register.DottysProfilePictureActivity
import com.keylimetie.dottys.register.DottysRegisterUserDelegates
import com.keylimetie.dottys.register.DottysRegisterUserObserver
import java.io.ByteArrayOutputStream
import java.io.IOException


    class ProfileFragment : Fragment(), DottysRegisterUserDelegates {

    private val profileViewModel = ProfileViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.hideLoader()
        profileViewModel.initProfileView(root, activity, context!!, this )
        return root
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//          super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            try {
//                val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, profileViewModel.image_uri)
//                val stream = ByteArrayOutputStream()
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
//                val byteArray = profileViewModel.pictureActivity.resizeBitmap(bitmap)
//                profileViewModel.pictureActivity.registerViewModel.activityRegisterObserver  =  DottysRegisterUserObserver(this)
//                profileViewModel.pictureActivity.registerViewModel.uploadImgage(activity as DottysBaseActivity,stream.toByteArray())
//                profileViewModel.imageViewProfile?.setImageBitmap(bitmap)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }


    override fun registerUser(userData: DottysLoginResponseModel) {
     }

    override fun imageProfileHasUploaded(hasUploaded: Boolean) {
     }
}