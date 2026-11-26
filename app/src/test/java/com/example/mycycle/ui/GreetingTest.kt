package com.example.mycycle.ui

import com.example.mycycle.R
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {
    @Test
    fun blankName() {
        assertEquals("Доброе утро", greetingText("Доброе утро", ""))
        assertEquals("Доброе утро", greetingText("Доброе утро", "   "))
        assertEquals("Доброе утро, Аня", greetingText("Доброе утро", "Аня"))
    }

    @Test
    fun boundaries() {
        assertEquals(R.string.greet_evening, greetingRes(4))
        assertEquals(R.string.greet_morning, greetingRes(5))
        assertEquals(R.string.greet_morning, greetingRes(11))
        assertEquals(R.string.greet_day, greetingRes(12))
        assertEquals(R.string.greet_day, greetingRes(17))
        assertEquals(R.string.greet_evening, greetingRes(18))
        assertEquals(R.string.greet_evening, greetingRes(0))
    }
}
