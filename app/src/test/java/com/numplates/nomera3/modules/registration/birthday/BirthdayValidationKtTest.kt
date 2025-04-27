package com.numplates.nomera3.modules.registration.birthday

import com.numplates.nomera3.modules.registration.ui.birthday.isDateValid
import com.numplates.nomera3.modules.registration.ui.birthday.isTooOld
import com.numplates.nomera3.modules.registration.ui.birthday.isTooYoung
import org.junit.Assert.assertEquals
import org.junit.Test

class BirthdayValidationKtTest  {

    @Test
    fun `isDateValid() return false when day is less than 01`() {
        val date = "00121990"
        assertEquals(false, date.isDateValid())
    }

    @Test
    fun `isDateValid() return false when day is larger than 31`() {
        val date = "32121990"
        assertEquals(false, date.isDateValid())
    }

    @Test
    fun `isDateValid() return false when day is larger than 28 in February`() {
        val date = "29022021"
        assertEquals(false, date.isDateValid())
    }

    @Test
    fun `isDateValid() return true when day it is 28 February 2020`() {
        val date = "29022020"
        assertEquals(true, date.isDateValid())
    }

    @Test
    fun `isDateValid() return false when month is less than 01`() {
        val date = "14001990"
        assertEquals(false, date.isDateValid())
    }

    @Test
    fun `isDateValid() return false when month is larger than 12`() {
        val date = "14131990"
        assertEquals(false, date.isDateValid())
    }

    @Test
    fun `isTooYoung() return true when age is less than 17`() {
        val date = "14122008"
        assertEquals(true, date.isTooYoung())
    }

    @Test
    fun `isTooYoung() return false when age is larger than 17`() {
        val date = "14101990"
        assertEquals(false, date.isTooYoung())
    }

    @Test
    fun `isTooOld() return false when age is less than 89`() {
        val date = "14101940"
        assertEquals(false, date.isTooOld())
    }

    @Test
    fun `isTooOld() return true when age is larger than 89`() {
        val date = "14101920"
        assertEquals(true, date.isTooOld())
    }
}