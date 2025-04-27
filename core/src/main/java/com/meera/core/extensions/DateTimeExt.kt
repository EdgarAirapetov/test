package com.meera.core.extensions

import java.text.SimpleDateFormat
import java.util.*


/**
 * Check times in millis is same day or not
 */
fun isSameDay(currentTimeMillis: Long, endTimeMillis: Long): Boolean {
    val sdf = SimpleDateFormat("yyyyMMdd")
    return sdf.format(Date(currentTimeMillis)) == sdf.format(Date(endTimeMillis))
}