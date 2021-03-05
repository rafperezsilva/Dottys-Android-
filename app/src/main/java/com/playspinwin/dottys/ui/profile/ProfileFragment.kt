package com.playspinwin.dottys.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.playspinwin.dottys.DottysMainNavigationActivity
import com.playspinwin.dottys.R
import com.playspinwin.dottys.utils.DottysStatics


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