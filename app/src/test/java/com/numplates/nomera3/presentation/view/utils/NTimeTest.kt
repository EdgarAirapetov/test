package com.numplates.nomera3.presentation.view.utils

import com.meera.core.utils.getDurationSeconds
import junit.framework.Assert.assertEquals
import org.junit.Test

class NTimeTest {

    @Test
    fun getDurationSecondsTest() {
        val secondString = getDurationSeconds(15)                           // 00:15 seconds
        val minuteString = getDurationSeconds(60 * 12 + 42)                 // 12:42
        val hourString = getDurationSeconds((60 * 60 * 2) + (60 * 4) + 37)  // 02:04:37
        // print("Sec($secondString) Min($minuteString) Hour($hourString)")
        assertEquals("Seconds", "00:15", secondString)
        assertEquals("Minutes", "12:42", minuteString)
        assertEquals("Hours", "02:04:37", hourString)
    }

    @Test
    fun setTime(){
        val dayInMills = (1000 * 60 * 60 * 24)
        val hourInMills = (60 * 60 * 1000)
        val time = 1599999999990
        val currentTime = System.currentTimeMillis()
        val millisLeft = 1000 * 60 * 60 * 15 + 60 * 1000 * 10 //= time - currentTime
        val day = millisLeft / dayInMills
        val hoursLeft = (millisLeft - (dayInMills * day))/ hourInMills
        val minutesLeft = (millisLeft - (hoursLeft * hourInMills)) % (60 * 60 * 1000) / (60 * 1000)
        println("day = $day hours = $hoursLeft minutes = $minutesLeft")
    }


    @Test
    fun timeAgoExtendedTest(){
        // 366 * 24 * 60 * 60
        // input millis
        val nowAgo = secToMillis(7 * 60)

        // Need Android context
        //val result = NTime.timeAgoExtended(nowAgo)
        //println("timeAgo RES:$result")
    }


    private fun secToMillis(sec: Int) = System.currentTimeMillis() - sec * 1000L

}
