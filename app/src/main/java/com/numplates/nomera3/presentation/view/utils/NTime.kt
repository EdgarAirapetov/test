package com.numplates.nomera3.presentation.view.utils

import android.content.Context
import com.numplates.nomera3.R
import kotlin.math.abs
import kotlin.math.floor


@Deprecated("Partially transited to CORE module")
class NTime(context: Context) {

    init {
        periods_1 = context.resources.getStringArray(R.array.time_ago_units_1)
        periods_2_3_4 = context.resources.getStringArray(R.array.time_ago_units_2_3_4)
        periods_5_and_more = context.resources.getStringArray(R.array.time_ago_units_5_and_more)

        periods_1_comment = context.resources.getStringArray(R.array.time_ago_units_1_comment)
        periods_2_3_4_comment = context.resources.getStringArray(R.array.time_ago_units_2_3_4_comment)
        periods_5_and_more_comment = context.resources.getStringArray(R.array.time_ago_units_5_and_more_comment)
    }

    fun setServerTime(serverTime: Long) {
        deltaTime = System.currentTimeMillis() / 1000 - serverTime
    }

    companion object {

        /* Add your time unit conversions here */
        private var deltaTime: Long = 0 //sec
        internal lateinit var periods_1: Array<String>
        internal lateinit var periods_2_3_4: Array<String>
        internal lateinit var periods_5_and_more: Array<String>

        internal lateinit var periods_1_comment: Array<String>
        internal lateinit var periods_2_3_4_comment: Array<String>
        internal lateinit var periods_5_and_more_comment: Array<String>
        internal val lengths = arrayOf(1f, 60f, 60f, 24f, 7f, 4.35f, 12f)

        val ONE_DAY = (1000 * 3600 * 24).toLong()
        val ONE_YEAR = ONE_DAY * 365


        fun timeAgo(date: Long,
                    shouldTrimAgo: Boolean = false): String {
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


        fun timeAgoComment(date: Long, shouldTrimAgo: Boolean = false): String {
            val crntTime = System.currentTimeMillis()
            val now = crntTime / 1000 - deltaTime

            var periods: Array<String>
            var delta = (now - date).toDouble()
            var resDiff = 0
            //if (delta < 20) return periods_1_comment[0]

            var period = periods_1_comment[1]
            var j = 1
            while (j <= lengths.size && delta >= lengths[j - 1]) {
                delta /= lengths[j - 1]
                resDiff = abs(floor(delta).toInt())

                periods = when {
                    resDiff % 100 in 11..19 ->  periods_5_and_more_comment
                    resDiff % 10 == 1 -> periods_1_comment
                    resDiff % 10 in 2..4 -> periods_2_3_4_comment
                    else -> periods_5_and_more_comment
                }
                period = periods[j]
                j++
            }
            if (resDiff == 0) resDiff = 1 // no need to show 0 sec
            return String.format("%d %s", resDiff, period trimAgoText shouldTrimAgo)
        }

        // Убираем текст "Назад" если нужно
        private infix fun String.trimAgoText(isTrim: Boolean) =
                if (isTrim) if (this.contains("назад")) this.substringBeforeLast(" ") else this else this


    }
}
