package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.numplates.nomera3.R

private const val ONE_SEC_IN_MILLIS = 1000
private const val ONE_MIN_IN_SEC = 60
private const val ONE_DAY_IN_HOURS = 24
private const val DURATION_TEN = 10

class MeeraNomeraTimerView : ConstraintLayout {

    private var day: TextView? = null
    private var hour: TextView? = null
    private var minute: TextView? = null
    private var timerDay: TextView? = null
    private var timerHour: TextView? = null
    private var timerMinute: TextView? = null

    private val dayInMills = ONE_SEC_IN_MILLIS * ONE_MIN_IN_SEC * ONE_MIN_IN_SEC * ONE_DAY_IN_HOURS
    private val hourInMills = ONE_MIN_IN_SEC * ONE_MIN_IN_SEC * ONE_SEC_IN_MILLIS
    private val minuteInMills = ONE_MIN_IN_SEC * ONE_SEC_IN_MILLIS

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        inflate(context, R.layout.nomera_timer_view_meera, this)
        day = findViewById(R.id.tv_day)
        hour = findViewById(R.id.tv_hour)
        minute = findViewById(R.id.tv_minute)
        timerDay = findViewById(R.id.tv_timer_day_meera)
        timerHour = findViewById(R.id.tv_timer_hour_meera)
        timerMinute = findViewById(R.id.tv_timer_minute_meera)
    }

    fun setTime(blockTime: Long) {
        val time = blockTime * ONE_SEC_IN_MILLIS
        val currentTime = System.currentTimeMillis()
        val millisLeft = time - currentTime
        val dayLeft = millisLeft / dayInMills
        val hoursLeft = (millisLeft - (dayInMills * dayLeft)) / hourInMills
        val minutesLeft = (millisLeft - (hoursLeft * hourInMills)) % hourInMills / minuteInMills

        updateDate(dayLeft)
        updateHour(hoursLeft)
        updateMinutes(minutesLeft)
    }

    private fun updateDate(dayLeft: Long) {
        if (dayLeft >= DURATION_TEN) {
            val countDay = (dayLeft.toString().getOrNull(0) ?: '0').toString() +
                (dayLeft.toString().getOrNull(1) ?: '0').toString()
            day?.text = countDay
            timerDay?.text = resources.getQuantityString(R.plurals.declination_days,countDay.toInt())
        } else {
            val countDay = "0" + (dayLeft.toString().getOrNull(0) ?: '0').toString()
            day?.text = countDay
            timerDay?.text = resources.getQuantityString(R.plurals.declination_days,countDay.toInt())
        }
    }

    private fun updateHour(hoursLeft: Long) {
        if (hoursLeft >= DURATION_TEN) {
            val countHour = (hoursLeft.toString().getOrNull(0) ?: '0').toString() +
                (hoursLeft.toString().getOrNull(1) ?: '0').toString()
            hour?.text = countHour
            timerHour?.text = resources.getQuantityString(R.plurals.declination_hour,countHour.toInt())
        } else {
            val countHour = "0" + (hoursLeft.toString().getOrNull(0) ?: '0').toString()
            hour?.text = countHour
            timerHour?.text = resources.getQuantityString(R.plurals.declination_hour,countHour.toInt())
        }
    }

    private fun updateMinutes(minutesLeft: Long) {
        if (minutesLeft >= DURATION_TEN) {
            val countMinute = (minutesLeft.toString().getOrNull(0) ?: '0').toString() +
                (minutesLeft.toString().getOrNull(1) ?: '0').toString()
            minute?.text = countMinute
            timerMinute?.text = resources.getQuantityString(R.plurals.declination_minutes,countMinute.toInt())
        } else {
            val countMinute = "" + (minutesLeft.toString().getOrNull(0) ?: '0').toString()
            minute?.text = "" + (minutesLeft.toString().getOrNull(0) ?: '0').toString()
            timerMinute?.text = resources.getQuantityString(R.plurals.declination_minutes,countMinute.toInt())
        }
    }
}
