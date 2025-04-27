package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.numplates.nomera3.R

class NomeraTimerView: ConstraintLayout {

    private var day1: TextView? = null
    private var day2: TextView? = null

    private var hour1: TextView? = null
    private var hour2: TextView? = null

    private var minute1: TextView? = null
    private var minute2: TextView? = null

    private val dayInMills = 1000 * 60 * 60 * 24
    private val hourInMills = 60 * 60 * 1000
    private val minuteInMills = 60 * 1000

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init(){
        inflate(context, R.layout.nomera_timer_view, this)
        day1 = findViewById(R.id.tv_day1)
        day2 = findViewById(R.id.tv_day2)

        hour1 = findViewById(R.id.tv_hour1)
        hour2 = findViewById(R.id.tv_hour2)

        minute1 = findViewById(R.id.tv_minute1)
        minute2 = findViewById(R.id.tv_minute2)
    }

     fun setTime(blockTime: Long){
         val time = blockTime * 1000
         val currentTime = System.currentTimeMillis()
         val millisLeft = time - currentTime
         val day = millisLeft / dayInMills
         val hoursLeft = (millisLeft - (dayInMills * day))/ hourInMills
         val minutesLeft = (millisLeft - (hoursLeft * hourInMills)) % hourInMills / minuteInMills


         if (day >= 10){
             day1?.text = (day.toString().getOrNull(0)?: '0').toString()
             day2?.text = (day.toString().getOrNull(1)?: '0').toString()
         } else {
             day1?.text = "0"
             day2?.text = (day.toString().getOrNull(0)?: '0').toString()
         }

         if (hoursLeft >= 10) {
             hour1?.text = (hoursLeft.toString().getOrNull(0) ?: '0').toString()
             hour2?.text = (hoursLeft.toString().getOrNull(1) ?: '0').toString()
         } else {
             hour1?.text = "0"
             hour2?.text = (hoursLeft.toString().getOrNull(0) ?: '0').toString()
         }

         if (minutesLeft >= 10) {
             minute1?.text = (minutesLeft.toString().getOrNull(0) ?: '0').toString()
             minute2?.text = (minutesLeft.toString().getOrNull(1) ?: '0').toString()
         } else {
             minute1?.text = ""
             minute2?.text = (minutesLeft.toString().getOrNull(0) ?: '0').toString()
         }
    }
}
