package com.numplates.nomera3.modules.registration.ui.birthday

import com.meera.core.utils.getAge
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

const val BIRTHDAY_INPUT_FORMAT = "ddMMyyyy"
const val BIRTHDAY_GMT_0 = "GMT-0:00"
const val BIRTHDAY_MIN_AGE = 17
const val BIRTHDAY_MAX_AGE = 89

sealed class BirthdayValidationResult {
    object BirthdayIncorrect : BirthdayValidationResult()
    object BirthdayCorrect : BirthdayValidationResult()
    object TooYoung : BirthdayValidationResult()
    object TooOld : BirthdayValidationResult()
}

fun String.validateBirthday(): BirthdayValidationResult {
    return when {
        !isDateValid() -> BirthdayValidationResult.BirthdayIncorrect
        isTooYoung() -> BirthdayValidationResult.TooYoung
        isTooOld() -> BirthdayValidationResult.TooOld
        else -> BirthdayValidationResult.BirthdayCorrect
    }
}

fun String.isAgeValid(): Boolean {
    return when {
        !isDateValid() -> false
        isTooYoung() -> false
        else -> true
    }
}

fun String.isDateValid(): Boolean {
    return parsedDate() != null
}

fun String.parsedDate(): Date? {
    val format = SimpleDateFormat(BIRTHDAY_INPUT_FORMAT, Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone(BIRTHDAY_GMT_0)
    format.isLenient = false
    return try {
        format.parse(this)
    } catch (e: Exception) {
        Timber.e("Failed string format parsing ${e.message}")
        null
    }
}

fun Long.toRegistrationBirthdayString(): String? {
    val format = SimpleDateFormat(BIRTHDAY_INPUT_FORMAT, Locale.getDefault())
    format.isLenient = false
    return try {
        format.format(Date(this * 1000))
    } catch (e: Exception) {
        Timber.e("Failed long format parsing ${e.message}")
        null
    }
}

fun String.isTooYoung(): Boolean {
    val date: Long = parsedDate()?.time ?: 0
    val age: Int = getAge(date / 1000).toIntOrNull() ?: 0
    return age < BIRTHDAY_MIN_AGE
}

fun String.isTooOld(): Boolean {
    val date: Long = parsedDate()?.time ?: 0
    val age: Int = getAge(date / 1000).toIntOrNull() ?: 0
    return age > BIRTHDAY_MAX_AGE
}

fun String.getValidAge(): Int {
    val date: Long = parsedDate()?.time ?: 0
    return getAge(date / 1000).toIntOrNull() ?: 0
}

fun Long.toSeconds(): Int {
    return try {
        TimeUnit.MILLISECONDS
            .toSeconds(this)
            .toInt()
    } catch (e: Exception) {
        Timber.e("Failed long to seconds ${e.message}")
        this.toInt()
    }
}
