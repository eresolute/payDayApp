package com.fh.payday

import com.fh.payday.utilities.DateTime.Companion.currentDate
import com.fh.payday.utilities.DateTime.Companion.isToday
import junit.framework.TestCase.assertEquals
import org.junit.Test

class DateTimeTest {

    @Test
    fun `should return true on current date`() {
        val currentDate = currentDate("yyyy-MM-dd")
        assertEquals(isToday(currentDate), true)
    }

    @Test
    fun `should return false on past dates`() {
        val currentDate = "19/11/2019"
        assertEquals(isToday(currentDate), false)
    }

    @Test
    fun `should return false on future dates`() {
        val currentDate = "19/11/2026"
        assertEquals(isToday(currentDate), false)
    }

    @Test
    fun `should return false on invalid dates`() {
        val currentDate = "88/11/1"
        assertEquals(isToday(currentDate), false)
    }

}