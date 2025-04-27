package com.meera.core.utils

import android.content.Context
import android.content.res.Resources
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.meera.core.R
import com.meera.core.extensions.empty
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private const val ONE_DAY = (1000 * 3600 * 24).toLong()
private const val ONE_YEAR = ONE_DAY * 365


/**
 * Time ago for chat (today, yesterday, curr date) divider
 */
fun timeAgoChat(context: Context, timeMillis: Long): String {
    val calDate = Calendar.getInstance()
    calDate.timeInMillis = timeMillis

    val todayCal = Calendar.getInstance()
    todayCal.timeInMillis = System.currentTimeMillis()

    if (
        todayCal.get(Calendar.YEAR) == calDate.get(Calendar.YEAR) &&
        todayCal.get(Calendar.DAY_OF_YEAR) == calDate.get(Calendar.DAY_OF_YEAR)
    ) {
        return context.getString(R.string.days_ago_today)
    } else if (
        todayCal.get(Calendar.YEAR) == calDate.get(Calendar.YEAR) &&
        todayCal.get(Calendar.DAY_OF_YEAR) - calDate.get(Calendar.DAY_OF_YEAR) == 1
    ) {
        return context.getString(R.string.days_ago_yesterday)
    }
    return DateFormat.format("dd MMMM yyyy", Date(timeMillis)).toString()
}

/**
 * Time ago for chat (today, yesterday, curr date) toolbar status
 */
fun timeAgoChatToolbarStatus(context: Context, timeMillis: Long): String {
    val calDate = Calendar.getInstance()
    calDate.timeInMillis = timeMillis

    val todayCal = Calendar.getInstance()
    todayCal.timeInMillis = System.currentTimeMillis()

    val yesterdayCal = Calendar.getInstance()
    yesterdayCal.timeInMillis = System.currentTimeMillis()
    yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)

    val recentlyCal = Calendar.getInstance()
    recentlyCal.timeInMillis = System.currentTimeMillis()
    recentlyCal.add(Calendar.MINUTE, -15)

    if (isTimeWithinRange(
            Date(timeMillis),
            Date(recentlyCal.timeInMillis),
            Date(System.currentTimeMillis())
        )
    ) {
        return context.getString(R.string.recently)
    } else if (todayCal.get(Calendar.DATE) == calDate.get(Calendar.DATE)) {   // today
        return context.getString(R.string.days_ago_today)
    } else if (calDate.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR)  // yesterday
        && calDate.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)
    ) {
        return context.getString(R.string.days_ago_yesterday)
    } else if (timeMillis == 0L) {                                            // Unknown
        return String.empty()
    }

    val date = DateFormat.format("dd.MM.yyyy", Date(timeMillis)).toString()
    return context.getString(R.string.chat_status_last_seen_at, date)
}

/**
 * Method work ONLY with Android context
 *
 *   val seconds=TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
 *   val minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
 *   val hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
 *   val days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());
 */
fun timeAgoExtended(context: Context, timeMillis: Long): String {
    val currentTimeMs = System.currentTimeMillis()
    val agoNow = currentTimeMs - 11 * 1000             // 10 sec ago
    val agoRecently = currentTimeMs - 61 * 1000        // 1 minute ago
    val agoMin = currentTimeMs - 60 * 60 * 1000        // 59 min ago (max)
    val hourAgo = currentTimeMs - 24 * 60 * 60 * 1000  // 23 hour ago

    val yearAgoCal = Calendar.getInstance()
    yearAgoCal.timeInMillis = currentTimeMs
    yearAgoCal.add(Calendar.YEAR, -1)

    when {
        isTimeWithinRange(timeMillis, agoNow, currentTimeMs) -> {
            return context.getString(R.string.now)
        }

        isTimeWithinRange(timeMillis, agoRecently, agoNow) -> {
            return context.getString(R.string.recently)
        }

        isTimeWithinRange(timeMillis, agoMin, agoRecently) -> {
            //return "n минут назад"
            return DateUtils.getRelativeTimeSpanString(
                timeMillis,
                currentTimeMs,
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        }

        isTimeWithinRange(timeMillis, hourAgo, agoMin) -> {
            //return "n часов назад"
            return DateUtils.getRelativeTimeSpanString(
                timeMillis,
                currentTimeMs,
                DateUtils.HOUR_IN_MILLIS
            ).toString()
        }

        isTimeWithinRange(timeMillis, yearAgoCal.timeInMillis, hourAgo) -> {
            val days = TimeUnit.MILLISECONDS.toDays(currentTimeMs - timeMillis).toInt()
            return context.resources.getQuantityString(
                R.plurals.days_ago,
                days,
                days
            )
        }

        else -> return context.getString(R.string.long_time_ago)
    }
}

fun timeAgoNotification(context: Context, timeMillis: Long): String {
    val currentTimeMs = System.currentTimeMillis()
    val agoNow = currentTimeMs - 11 * 1000
    val agoRecently = currentTimeMs - 61 * 1000
    val agoMin = currentTimeMs - 60 * 60 * 1000
    val hourAgo = currentTimeMs - 24 * 60 * 60 * 1000

    val yearAgoCal = Calendar.getInstance()
    yearAgoCal.timeInMillis = currentTimeMs
    yearAgoCal.add(Calendar.YEAR, -1)

    when {
        isTimeWithinRange(timeMillis, agoNow, currentTimeMs) -> {
            val secondsAgo = ((currentTimeMs - timeMillis) / 1000).toInt()
            return context.resources.getQuantityString(R.plurals.seconds, secondsAgo, secondsAgo)
        }

        isTimeWithinRange(timeMillis, agoRecently, agoNow) -> {
            val secondsAgo = ((currentTimeMs - timeMillis) / 1000).toInt()
            return context.resources.getQuantityString(R.plurals.seconds, secondsAgo, secondsAgo)
        }

        isTimeWithinRange(timeMillis, agoMin, agoRecently) -> {
            val minutesAgo = DateUtils.getRelativeTimeSpanString(
                timeMillis,
                currentTimeMs,
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
            return removeAgoFromDateString(minutesAgo)
        }

        isTimeWithinRange(timeMillis, hourAgo, agoMin) -> {
            val hoursAgo = DateUtils.getRelativeTimeSpanString(
                timeMillis,
                currentTimeMs,
                DateUtils.HOUR_IN_MILLIS
            ).toString()
            return removeAgoFromDateString(hoursAgo)
        }

        isTimeWithinRange(timeMillis, yearAgoCal.timeInMillis, hourAgo) -> {
            val daysAgo = TimeUnit.MILLISECONDS.toDays(currentTimeMs - timeMillis).toInt()
            return context.resources.getQuantityString(
                R.plurals.days_without_ago,
                daysAgo,
                daysAgo
            )
        }

        else -> return context.getString(R.string.long_time_ago)
    }
}

private fun removeAgoFromDateString(dateString: String): String {
    return dateString.substring(0, dateString.lastIndexOf(" "))
}


private fun isTimeWithinRange(currentMillis: Long, startMillis: Long, endMillis: Long): Boolean {
    return !(Date(currentMillis)
        .before(Date(startMillis)) || Date(currentMillis)
        .after(Date(endMillis)))
}

private fun isTimeWithinRange(currDate: Date, startDate: Date, endDate: Date): Boolean {
    return !(currDate.before(startDate) || currDate.after(endDate))
}

fun getAge(birthday: Long): String {
    var birthday = birthday
    birthday *= 1000
    val dob = Calendar.getInstance()
    val today = Calendar.getInstance()
    dob.timeInMillis = birthday
    var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    val ageInt = age
    val ageS: String
    if (ageInt < 1)
        ageS = ""
    else
        ageS = ageInt.toString()

    return ageS
}

fun isBirthdayToday(birthday: Long?): Boolean {
    if (birthday == null) return false
    val bdCal = Calendar.getInstance()
    bdCal.time = Date(birthday * 1000)
    val bdMonth = bdCal.get(Calendar.MONTH)
    val bdDay = bdCal.get(Calendar.DAY_OF_MONTH)
    val cal = Calendar.getInstance()
    val currentMonth = cal.get(Calendar.MONTH)
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
    return bdMonth == currentMonth && bdDay == currentDay
}

fun getShortTimeWithDate(res: Resources, time: Long, is24hourMode: Boolean): String {
    val atNow = System.currentTimeMillis()

    val now = Calendar.getInstance()
    now.timeInMillis = atNow
    now.isLenient = true
    now.timeZone = TimeZone.getDefault()

    val nowDay = now.get(Calendar.DAY_OF_MONTH)

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = time
    calendar.isLenient = true //<- fun thing
    calendar.timeZone = TimeZone.getDefault()

    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH)
    val year = calendar.get(Calendar.YEAR)

    if (nowDay == dayOfMonth) {
        return getShortTime(time, is24hourMode)
    } else if ((nowDay - dayOfMonth == 1 || nowDay == 1) && atNow - time < ONE_DAY + atNow % ONE_DAY) {
        return res.getString(R.string.general_yesterday) + ", " + getShortTime(time, is24hourMode)
    }

    val months = res.getStringArray(R.array.month_names)
    return if (atNow - time < atNow % ONE_YEAR + atNow % ONE_YEAR) {
        dayOfMonth.toString() + " " + months[month] + ", " + getShortTime(time, is24hourMode)
    } else dayOfMonth.toString() + " " + months[month] + " " + year + ", " + getShortTime(
        time,
        is24hourMode
    )
}

fun convertUnixDate(unixTime: Long?): String {
    unixTime?.let {

        val date = Date(it * 1000)
        val dayTimeDate = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

        return dayTimeDate.format(date)
    } ?: run { return "" }
}


fun convertUnixTime(unixTime: Long?): String {
    val date = Date(unixTime!! * 1000)
    val timeDate = SimpleDateFormat("HH:mm", Locale.getDefault())

    return timeDate.format(date)
}

fun getMonthForDate(date: Date?): String {
    val now = Calendar.getInstance()
    val calendarData = Calendar.getInstance()
    date?.let {
        calendarData.time = date
        return if (now.get(Calendar.YEAR) == calendarData.get(Calendar.YEAR)) {
            val dateFormat = SimpleDateFormat("LLLL", Locale.getDefault())
            val res = dateFormat.format(date)
            res[0].uppercaseChar() + res.substring(1, res.length)
        } else {
            val dateFormat = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
            val res = dateFormat.format(date)
            res[0].uppercaseChar() + res.substring(1, res.length)
        }

    } ?: run {
        return "Error"
    }
}

fun getDurationSeconds(timeSec: Int): String {
    if (timeSec >= 60 * 60) {  // hour
        return String.format("%02d:%02d:%02d", timeSec / 3600, (timeSec % 3600) / 60, timeSec % 60)
    }
    return String.format("%02d:%02d", timeSec / 60, timeSec % 60)
}

/**
 * Rooms time format doc
 * https://nomera.atlassian.net/wiki/spaces/NOM/pages/359989312#%D0%92%D1%80%D0%B5%D0%BC%D1%8F%2F%D0%B4%D0%B5%D0%BD%D1%8C%2F%D0%B4%D0%B0%D1%82%D0%B0-%D0%BF%D0%BE%D1%81%D0%BB%D0%B5%D0%B4%D0%BD%D0%B5%D0%B3%D0%BE-%D1%81%D0%BE%D0%BE%D0%B1%D1%89%D0%B5%D0%BD%D0%B8%D1%8F-%D0%B8%D0%BB%D0%B8-%D1%81%D0%BE%D0%B1%D1%8B%D1%82%D0%B8%D1%8F-%D0%B2-%D1%87%D0%B0%D1%82%D0%B5
 */
fun getTimeForRooms(context: Context, timeMillis: Long): String {
    val now = Calendar.getInstance()
    now.timeInMillis = System.currentTimeMillis()
    now.isLenient = true
    now.timeZone = TimeZone.getDefault()

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeMillis
    calendar.isLenient = true
    calendar.timeZone = TimeZone.getDefault()

    return if (
        now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
    ) {
        getShortTime(millis = timeMillis, is24hourMode = true)
    } else if (
        now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) - calendar.get(Calendar.DAY_OF_YEAR) == 1
    ) {
        context.getString(R.string.days_ago_yesterday)
    } else if (timeMillis == 0L) {
        String.empty()
    } else {
        getDateDDMMYYYY(unixTime = timeMillis / 1000)
    }
}

@Deprecated("Incorrect time format. Use function above")
fun getTimeForRooms(timeMillis: Long?, yesterdayStr: String): String {
    if (timeMillis == null) return ""
    val dayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000  // 23 hour ago
    val twoDaysAgo = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000  // 23 hour ago

    return when {
        isTimeWithinRange(timeMillis, dayAgo, System.currentTimeMillis()) ->
            getShortTime(timeMillis, true)

        isTimeWithinRange(timeMillis, twoDaysAgo, dayAgo) ->
            yesterdayStr

        else ->
            getDateDDMMYYYY(timeMillis / 1000)
    }
}

/**
 * Returns time like: 15:34 according to current time zone
 */
fun getShortTime(millis: Long, is24hourMode: Boolean): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = millis
    calendar.isLenient = true //<- fun thing
    calendar.timeZone = TimeZone.getDefault()

    var minutes = ""
    if (calendar.get(Calendar.MINUTE) < 10) {
        minutes += "0"
    }
    minutes += calendar.get(Calendar.MINUTE)

    return if (is24hourMode) {
        "" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + minutes
    } else {
        "" + calendar.get(Calendar.HOUR) + ":" + minutes +
            " " + if (calendar.get(Calendar.AM_PM) == 0) "AM" else "PM"
    }
}

fun getDateDDMMYYYY(unixTime: Long?): String {
    unixTime?.let {

        val date = Date(it * 1000)
        val dayTimeDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        return dayTimeDate.format(date)
    } ?: run { return "" }
}

fun getDurationFromSeconds(context: Context, durationInSeconds: Int): String {
    var duration = ""

    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60
    if (minutes > 0) {
        duration = duration.plus(context.resources.getQuantityString(R.plurals.minutes, minutes, minutes))
    }

    val secondsString = context.resources.getQuantityString(R.plurals.seconds, seconds, seconds)

    return if (duration.isNotEmpty()) {
        if (seconds > 0) {
            duration+= " "
            duration = duration.plus(secondsString)
        }

        duration
    } else {
        duration.plus(secondsString)
    }
}

fun getInDurationFromSeconds(context: Context, durationInSeconds: Int): String {
    var duration = ""

    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60
    if (minutes > 0) {
        duration = duration.plus(context.resources.getQuantityString(R.plurals.in_minutes, minutes, minutes))
    }

    val secondsString = context.resources.getQuantityString(R.plurals.in_seconds, seconds, seconds)

    return if (duration.isNotEmpty()) {
        if (seconds > 0) {
            duration+= " "
            duration = duration.plus(secondsString)
        }

        duration
    } else {
        duration.plus(secondsString)
    }
}

private const val deltaTime: Long = 0
private lateinit var periods_1: Array<String>
private lateinit var periods_2_3_4: Array<String>
private lateinit var periods_5_and_more: Array<String>
private val lengths = arrayOf(1f, 60f, 60f, 24f, 7f, 4.35f, 12f)

fun timeAgo(context: Context, date: Long, shouldTrimAgo: Boolean = false): String {

    periods_1 = context.resources.getStringArray(R.array.time_ago_units_1_comment)
    periods_2_3_4 = context.resources.getStringArray(R.array.time_ago_units_2_3_4_comment)
    periods_5_and_more = context.resources.getStringArray(R.array.time_ago_units_5_and_more_comment)

    val now = System.currentTimeMillis() / 1000 - deltaTime

    var periods: Array<String>

    var delta = (now - date).toDouble()

    var resDiff = 0

    if (delta < 20) {
        return periods_1[0]
    }

    var period = periods_1[1]
    var j = 1
    while (j <= lengths.size && delta >= lengths[j - 1]) {

        delta = delta / lengths[j - 1]

        resDiff = Math.abs(Math.floor(delta).toInt())

        if (resDiff % 100 > 10 && resDiff % 100 < 20) {
            periods = periods_5_and_more
        } else if (resDiff % 10 == 1) {
            periods = periods_1
        } else if (resDiff % 10 > 1 && resDiff % 10 < 5) {
            periods = periods_2_3_4
        } else {
            periods = periods_5_and_more
        }
        period = periods[j]
        j++
    }

    return String.format("%d %s", resDiff, period trimAgoText shouldTrimAgo)
}

infix fun String.trimAgoText(isTrim: Boolean) =
    if (isTrim) if (this.contains("назад")) this.substringBeforeLast(" ") else this else this

