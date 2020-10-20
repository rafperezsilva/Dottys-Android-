package com.keylimetie.dottys.utils.volley_multipart

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream


/**
 * Sketch Project Studio
 * Created by Angga on 12/04/2016 14.27.
 */
object AppHelper {
    /**
     * Turn drawable resource into byte array.
     *
     * @param context parent context
     * @param id      drawable resource id
     * @return byte array
     */
    fun getFileDataFromDrawable(context: Context, id: Int): ByteArray {
        val drawable = ContextCompat.getDrawable(context, id)
        val bitmap = (drawable as BitmapDrawable?)!!.bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * Turn drawable into byte array.
     *
     * @param drawable data
     * @return byte array
     */
    fun getFileDataFromDrawable(
        context: Context?,
        drawable: Drawable
    ): ByteArray {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}