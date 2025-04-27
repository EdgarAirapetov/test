package com.meera.core.extensions

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import com.meera.core.extensions.DateExpr.YEAR
import com.meera.core.extensions.DateExpr.DAY
import com.meera.core.extensions.DateExpr.HOUR
import com.meera.core.extensions.DateExpr.DAY_YEAR
import com.meera.core.extensions.DateExpr.WEEK
import com.meera.core.extensions.DateExpr.MINUTE
import com.meera.core.extensions.DateExpr.SECOND
import com.meera.core.extensions.DateExpr.MONTH
import com.meera.core.extensions.DateExpr.WEEK_YEAR
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit

private const val MILLI = 1000L
private const val SECOND_IN_MILLISECONDS = 1000
private const val MINUTE_OR_HOUR = 60

private enum class DateExpr {
    YEAR, MONTH, DAY,
    HOUR, MINUTE, SECOND,
    WEEK, DAY_YEAR, WEEK_YEAR
}

fun Long.date(context: Context,
              isMills: Boolean = false,
              pattern: String = "dd.MM.yyyy HH:mm:ss"
): String? = SimpleDateFormat(
    pattern,
    context.resources.configuration.locale
).format(this.isMills(isMills))

fun Long.year(isMills: Boolean = false) = isMills(isMills).getData(YEAR)

fun Long.month(isMills: Boolean = false) = this.isMills(isMills).getData(MONTH)

fun Long.day(isMills: Boolean = false) = this.isMills(isMills).getData(DAY)

fun Long.dayOfWeek(isMills: Boolean = false) = this.isMills(isMills).getData(WEEK)

fun Long.hour(isMills: Boolean = false) = this.isMills(isMills).getData(HOUR)

fun Long.minute(isMills: Boolean = false) = this.isMills(isMills).getData(MINUTE)

fun Long.second(isMills: Boolean = false) = this.isMills(isMills).getData(SECOND)

fun Long.dayOfYear(isMills: Boolean = false) = this.isMills(isMills).getData(DAY_YEAR)

fun Long.weekOfYear(isMills: Boolean = false) = this.isMills(isMills).getData(WEEK_YEAR)

fun Long.dateOnly(isMills: Boolean = false, split: String = ".") =
    "${day(isMills)}$split${month(isMills)}$split${year(isMills)}"

fun Long.timeOnly(isMills: Boolean = false, split: String = ":") =
    "${hour(isMills)}$split${minute(isMills)}"

fun Int.isLeapYear() = (this % 4 == 0) && (this % 100 != 0) || (this % 400 == 0)

private fun Long.getData(expr: DateExpr): String {
    val cal = Calendar.getInstance()
    cal.time = Date(this)
    return when (expr) {
        YEAR -> cal[Calendar.YEAR].toString()
        MONTH -> (cal[Calendar.MONTH] + 1).toString().prefix0()
        DAY -> cal[Calendar.DAY_OF_MONTH].toString().prefix0()
        WEEK -> (cal[Calendar.DAY_OF_WEEK] - 1).toString()
        HOUR -> cal[Calendar.HOUR_OF_DAY].toString().prefix0()
        MINUTE -> cal[Calendar.MINUTE].toString().prefix0()
        SECOND -> cal[Calendar.SECOND].toString().prefix0()
        DAY_YEAR -> cal[Calendar.DAY_OF_YEAR].toString()
        WEEK_YEAR -> cal[Calendar.WEEK_OF_YEAR].toString()
    }
}

private fun String.prefix0() = if (length == 1) "0$this" else this
private fun Long.isMills(isMills: Boolean) = if (isMills) this else (this * MILLI)

fun Long.toEpoch(): Long = (this / MILLI)

/**
 * Возвращает таймзон в виде смещения относительно GMT
 */
fun getTimeZone(): Float {
    val calendar = GregorianCalendar()
    val timeZone = calendar.timeZone
    val offset = timeZone.rawOffset
    val hours = TimeUnit.MILLISECONDS.toHours(offset.toLong())
    val minutes = TimeUnit.MILLISECONDS.toMinutes(offset - TimeUnit.HOURS.toMillis(hours)).toFloat() / MINUTE_OR_HOUR
    return hours + minutes
}

/**
 * Возвращает интервал времени в формате hh:mm:ss
 */
fun getTimeDifference(started: Long): String {
    val current = System.currentTimeMillis()
    val interval = current - started

    val hh = interval / (SECOND_IN_MILLISECONDS * MINUTE_OR_HOUR * MINUTE_OR_HOUR)
    val mm = interval / (SECOND_IN_MILLISECONDS * MINUTE_OR_HOUR) % MINUTE_OR_HOUR
    val ss = interval / SECOND_IN_MILLISECONDS

    return "${chars(hh)}:${chars(mm)}:${chars(ss)}"
}

private fun chars(value: Long): String {
    return if (value.toString().length < 2) "0$value" else value.toString()
}
