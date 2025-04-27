package com.numplates.nomera3.modules.user.ui.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import timber.log.Timber

interface UserBirthdayUtils {
    /**
     * Метод проверяет, что есть ли сегодня у юзера День Рождения
     */
    fun isBirthdayToday(birthday: Long?): Boolean

    /**
     * Метод проверяет, что было ли вчера День Рождения у юзера
     */
    fun isBirthdayYesterday(birthday: Long?): Boolean

    /**
     * Через функцию будет проверка, что текущее время больше указанного
     * @param source - Указываем такой тип формата: 06:00
     * @return Возвращает true, если текущее время больше указанного
     */
    fun isDateAfter(source: String): Boolean
}

class UserBirthdayUtilsImpl @Inject constructor() : UserBirthdayUtils {

    override fun isBirthdayToday(birthday: Long?) = hasBirthdayToday(birthday)

    override fun isBirthdayYesterday(birthday: Long?): Boolean = hasBirthdayYesterday(birthday)

    override fun isDateAfter(source: String): Boolean {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTimeStr = formatter.format(Calendar.getInstance().time)
        return try {
            val currentTime = formatter.parse(currentTimeStr)
            val sourceTime = formatter.parse(source)

            currentTime?.after(sourceTime) ?: true
        } catch (e: ParseException) {
            Timber.e(e)
            true
        }
    }

    private fun hasBirthdayToday(birthday: Long?): Boolean {
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

    private fun hasBirthdayYesterday(birthday: Long?): Boolean {
        if (birthday == null) return false
        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
        val bdCal = Calendar.getInstance()
        bdCal.time = Date(birthday * 1000)
        val bdMonth = bdCal.get(Calendar.MONTH)
        val bdDay = bdCal.get(Calendar.DAY_OF_MONTH)
        val yesterdayMonth = yesterdayCal.get(Calendar.MONTH)
        val yesterdayDay = yesterdayCal.get(Calendar.DAY_OF_MONTH)

        return bdMonth == yesterdayMonth && bdDay == yesterdayDay
    }
}