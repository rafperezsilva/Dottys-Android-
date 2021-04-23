package com.keylimetie.dottys.utils

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Rational
import android.util.Size
import android.view.Surface
import androidx.camera.core.*
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.ui.locations.showSnackBarMessage
import kotlinx.android.synthetic.main.activity_dottys_camera.*
import java.io.File


class DottysCameraActivity  : DottysBaseActivity()
       {
           private var lensFacing = CameraX.LensFacing.FRONT
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dottys_camera)
      //  cameraObserver =  DottysPictureTakenObserver(baseActivity)
        texture.post { startCamera() }

        // Every time the provided texture view changes, recompute layout
        texture.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }


    }
           private fun startCamera() {
               val metrics = DisplayMetrics().also { texture.display.getRealMetrics(it) }
               val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
               val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)

               val previewConfig = PreviewConfig.Builder().apply {
                   setLensFacing(lensFacing)
                   setTargetResolution(screenSize)
                   setTargetAspectRatio(screenAspectRatio)
                   setTargetRotation(windowManager.defaultDisplay.rotation)
                   setTargetRotation(texture.display.rotation)
               }.build()

               val preview = Preview(previewConfig)
               preview.setOnPreviewOutputUpdateListener {
                   //var texture = findViewById<TextureView>(R.id.texture)
                   texture?.setSurfaceTexture(it.surfaceTexture)
                   updateTransform()
               }


               // Create configuration object for the image capture use case
               val imageCaptureConfig = ImageCaptureConfig.Builder()
                   .apply {
                       setLensFacing(lensFacing)
                       setTargetAspectRatio(screenAspectRatio)
                       setTargetRotation(Surface.ROTATION_180)
                       setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)

                   }.build()

               // Build the image capture use case and attach button click listener
               val imageCapture = ImageCapture(imageCaptureConfig)
               btn_take_picture.setOnClickListener {

                   val file = File.createTempFile("image",".jpg") // File(Environment.getStorageDirectory().toString() +
                            //"${System.currentTimeMillis()}.jpg")//File("${System.currentTimeMillis()}.jpg")

                   imageCapture.takePicture(file,
                       object : ImageCapture.OnImageSavedListener {
                           override fun onError(
                               error: ImageCapture.UseCaseError,
                               message: String, exc: Throwable?
                           ) {
                               val msg = "Photo capture failed: $message"
                               DottysBaseActivity().showSnackBarMessage(this@DottysCameraActivity,

                                   msg
                               )

                           }

                           override fun onImageSaved(file: File) {
                               val bm =  BitmapFactory.decodeFile(file.path)
                               val msg = "Photo Dimen  width:${bm.width} / height: ${bm.height}"
                               DottysBaseActivity().showSnackBarMessage(this@DottysCameraActivity,
                                   msg
                               )
                              // cameraObserver?.temporalFile = file
                               setResult(DottysStatics.PICTURE_TAKE_REQUEST_CODE, Intent().putExtra("FILE_PATH", file.path))
                               finish()
                           }
                       })

               }

               CameraX.bindToLifecycle(this, preview, imageCapture)
           }

           private fun updateTransform() {
               val matrix = Matrix()
               val centerX = texture.width / 2f
               val centerY = texture.height / 2f

               val rotationDegrees = when (texture.display.rotation) {
                   Surface.ROTATION_0 -> 0
                   Surface.ROTATION_90 -> 90
                   Surface.ROTATION_180 -> 180
                   Surface.ROTATION_270 -> 270
                   else -> return
               }
               matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
               texture.setTransform(matrix)
           }


       }

