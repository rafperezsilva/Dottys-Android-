package com.keylimetie.dottys.utils

import java.text.SimpleDateFormat
import java.util.*

fun Date.monthDayYear(): String {
    val format = SimpleDateFormat("MM/dd/yyy")
    return format.format(this)
}


fun Date.timeFromDate(): String {
    val format = SimpleDateFormat("HH:mm")
    return format.format(this)
}