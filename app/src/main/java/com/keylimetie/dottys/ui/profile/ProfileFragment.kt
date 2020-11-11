package com.keylimetie.dottys.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.keylimetie.dottys.utils.DottysCameraActivity
import com.keylimetie.dottys.utils.DottysStatics
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


    class ProfileFragment : Fragment()  {

    private var profileViewModel: ProfileViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.hideLoader()
        profileViewModel = ProfileViewModel(activity, activity?.getUserPreference())
        profileViewModel?.initProfileView(root, activity, requireContext(), this )


        return root
    }

        override fun onStop() {
            super.onStop()
            profileViewModel?.getDataToUpdate()
        }


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            when(requestCode){
                DottysStatics.PICTURE_TAKE_REQUEST_CODE -> {
                   val file = data?.getStringExtra("FILE_PATH")
                    if(file.isNullOrEmpty()){return}
                    val bmf = BitmapFactory.decodeFile(file)
                    (activity as DottysMainNavigationActivity).userPictureBM = bmf
                    profileViewModel?.imageViewProfile?.setImageBitmap(bmf)
                }
            }
        }
//    override fun registerUser(userData: DottysLoginResponseModel) {
//     }
//
//    override fun imageProfileHasUploaded(hasUploaded: Boolean) {
//     }
}