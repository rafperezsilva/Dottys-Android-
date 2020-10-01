package com.keylimetie.dottys.utils

import android.text.TextUtils
import android.util.Patterns
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


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
    if (str.length < 8) {
        mssg = "Must be have at least 7 characters"
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
//        Toast.makeText(this, mssg, Toast.LENGTH_LONG).show()
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