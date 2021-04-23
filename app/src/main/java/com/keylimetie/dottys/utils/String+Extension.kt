package com.keylimetie.dottys.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.util.Patterns
import org.skyscreamer.jsonassert.JSONAssert
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

fun String.isEquivalentToString(jsonString:String?):Boolean{
    return try {
        JSONAssert.assertEquals(this, jsonString, false)
        true
    } catch (e: Error) {
        false
    }
}

fun String.encodeToBitmap():Bitmap? {
    return try {
        val decodedByte = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedByte, 0,decodedByte?.size ?: return null)
    }catch (e: java.lang.Exception){
        Log.e("BITMAP DECODE","${e.message}")
        null
    }
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

fun String.stringToDate(): Date {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS").parse(this.replace("Z", ""))
}
fun String.stringGetYear(): Int {
    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS").parse(this.replace("Z", ""))
    val cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"))

    cal.time = date
    return cal.get(Calendar.YEAR)

}

fun String.getleftDays(): String {
    val date: Date =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS").parse(this.replace("Z", ""))
    val diferenceAtTime = date.time - Date().time
    return "end in " + (diferenceAtTime / (1000 * 3600 * 24)).toString() + " days"
}

fun String.isValidPassword(): Boolean {
    val str = this
    var valid = true
    var mssg = String()
    // Password policy check
    // Password should be minimum minimum 8 characters long
    if (str.length < 7) {
        mssg = "Must be have at least 6 characters"
        valid = false
    }
    // Password should contain at least one number
//        var exp = ".*[0-9].*"
//        var pattern = Pattern.compile(exp, Pattern.CASE_INSENSITIVE)
//        var matcher = pattern.matcher(str)
//        if (!matcher.matches()) {
//            valid = false
//        }

    // Password should contain at least one capital letter
    var exp = ".*[A-Z].*"
    var pattern = Pattern.compile(exp)
    var matcher = pattern.matcher(str)
    if (!matcher.matches()) {
        mssg = "Must be have at least one capital letter"
        valid = false
    }

    // Password should contain at least one small letter
    exp = ".*[a-z].*"
    pattern = Pattern.compile(exp)
    matcher = pattern.matcher(str)
    if (!matcher.matches()) {
        mssg = "Must be have at least one small letter"
        valid = false
    }

    // Password should contain at least one special character
    // Allowed special characters : "~!@#$%^&*()-_=+|/,."';:{}[]<>?"
//        exp = ".*[~!@#\$%\\^&*()\\-_=+\\|\\[{\\]};:'\",<.>/?].*"
//        pattern = Pattern.compile(exp)
//        matcher = pattern.matcher(str)
//        if (!matcher.matches()) {
//            valid = false
//        }

    // Set error if required
//        if (updateUI) {
//            val error: String? = if (valid) null else PASSWORD_POLICY
//            setError(data, error)
//        }
//    if (!valid) {
//        DottysBaseActivity().showSnackBarMessage(this, mssg)
//    }
    return valid
}

fun String.isValidEmail(): Boolean {
    return if (TextUtils.isEmpty(this)) {
        false
    } else {
        Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}


fun String.currentDateTime():String{
    val current = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        LocalDateTime.now()
    } else {
        return  ""
    }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    return  current.format(formatter)

}

   fun isProbablyAnEmulator() = Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknown")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for x86")
        || Build.BOARD == "QC_Reference_Phone" //bluestacks
        || Build.MANUFACTURER.contains("Genymotion")
        || Build.HOST.startsWith("Build") //MSI App Player
        || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        || "google_sdk" == Build.PRODUCT


/*
* BITMAP EXTENSION
* */

fun Bitmap.rotateBitmap():Bitmap{
    val matrix = Matrix()

    matrix.postRotate(90F)

    val scaledBitmap = Bitmap.createScaledBitmap(this, width, height, true)

    return  Bitmap.createBitmap(scaledBitmap,
        0,
        0,
        scaledBitmap.width,
        scaledBitmap.height,
        matrix,
        true)
}
fun Bitmap.rotateCustomBitmap(degreess:Float):Bitmap {
    val matrix = Matrix()

    matrix.postRotate(degreess)

    val scaledBitmap = Bitmap.createScaledBitmap(this, width, height, true)

    return Bitmap.createBitmap(
        scaledBitmap,
        0,
        0,
        scaledBitmap.width,
        scaledBitmap.height,
        matrix,
        true
    )
}