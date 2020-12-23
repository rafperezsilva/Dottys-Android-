package com.keylimetie.dottys.register

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysLoginResponseModel
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.utils.rotateBitmap
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.roundToInt


class DottysProfilePictureActivity: DottysBaseActivity(), DottysRegisterUserDelegates, View.OnClickListener {
      val PERMISSION_CODE = 1000
      val IMAGE_CAPTURE_CODE = 1001
    var image_uri: Uri? = null

      val registerViewModel = DottysRegisterViewModel(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_profile_picture)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        hideLoader()
        val imageAccountCreated = findViewById<ImageView>(R.id.account_created_image)
        val skipTakePicture = findViewById<Button>(R.id.skip_for_now_button)
        var imageParams = imageAccountCreated.layoutParams
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        imageParams.height = (displayMetrics.heightPixels * 0.5).roundToInt()
        imageAccountCreated.layoutParams = imageParams

        val takePicture = findViewById<Button>(R.id.add_photo_button)
        skipTakePicture.setOnClickListener(this)
        takePicture.setOnClickListener(this)
        registerViewModel.activityRegisterObserver = DottysRegisterUserObserver(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        //called when user presses ALLOW or DENY from Permission Request Popup
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

     private fun openCamera() {
          val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
             cameraIntent.putExtra("android.intent.extras.CAMERA_FACING",
                 android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
             cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
             cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
         } else {
             cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
         }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
     }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            try {
                val bitmapBase = MediaStore.Images.Media.getBitmap(contentResolver, image_uri)
                val bitmap = if (bitmapBase.width > bitmapBase.height) bitmapBase.rotateBitmap() else bitmapBase
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                userPictureBM = bitmap
               // val byteArray = resizeBitmap(bitmap)
                registerViewModel.uploadProfileImage(this, stream.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

      fun resizeBitmap(bitmap: Bitmap): ByteArray {
        val size: Int = bitmap.rowBytes * bitmap.height
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(size)
        bitmap.copyPixelsToBuffer(byteBuffer)
        return byteBuffer.array()
    }

    override fun registerUser(userData: DottysLoginResponseModel) {
     }

    override fun imageProfileHasUploaded(hasUploaded: Boolean) {
        val intent = Intent(this, DottysMainNavigationActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.add_photo_button -> {
                requestCameraPermission()
            }
            R.id.skip_for_now_button -> {
                registerViewModel.activityRegisterObserver = DottysRegisterUserObserver(this)
                registerViewModel.activityRegisterObserver?.imageHasUploaded = true
            }
        }
    }

    fun requestCameraPermission(){
        //if system os is Marshmallow or Above, we need to request runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED
            ) {
                //permission was not enabled
                val permission = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                //show popup to request permission
                requestPermissions(permission, PERMISSION_CODE)
            } else {
                //permission already granted
                openCamera()
            }
        } else {
            //system os is < marshmallow
            openCamera()
        }
    }


}

fun Bitmap.bitmapFixPosition(imageUri: String): Bitmap? {
    val ei = ExifInterface(imageUri)
    val orientation: Int = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90  -> this.rotateImages(-90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> this.rotateImages(-180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> this.rotateImages(-270f)
        ExifInterface.ORIENTATION_NORMAL     -> this
        else                                 -> this
    }
}

fun Bitmap.rotateImages(angle: Float): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(this, 0, 0, this.width, this.height,
        matrix, true)
}
//
//@Throws(IOException::class)
//fun rotateImage(bitmap: Bitmap): Bitmap? {
//    var rotate = 0
//    val exif: ExifInterface
//    exif = ExifInterface(path)
//    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//        ExifInterface.ORIENTATION_NORMAL)
//    when (orientation) {
//        ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
//        ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
//        ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
//    }
//    val matrix = Matrix()
//    matrix.postRotate(rotate.toFloat())
//    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width,
//        bitmap.height, matrix, true)
//}