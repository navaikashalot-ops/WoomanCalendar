package com.example.mycycle.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DayMarksTest {
    private fun log(flow: Int = 0, s: String = "", note: String = "") = DayLog("2026-11-03", flow, s, note)

    @Test fun periodFirstThenMood() =
        assertEquals(listOf(Mark.PERIOD, Mark.MOOD_SAD), DayMarks.cellMarks(log(2, "cramps|mood"), false))
    @Test fun priorityOrder() =
        assertEquals(listOf(Mark.CRAMPS, Mark.SEX), DayMarks.cellMarks(log(0, "sweet|sex|cramps", "x"), false))
    @Test fun ovulationOnlyWhenNoLog() {
        assertEquals(listOf(Mark.OVULATION), DayMarks.cellMarks(null, true))
        assertEquals(listOf(Mark.PILL), DayMarks.cellMarks(log(0, "pill"), true))
    }
    @Test fun multipleMoodsShowFirst() =
        assertEquals(listOf(Mark.MOOD_GOOD), DayMarks.cellMarks(log(0, "mood_angry|mood_good"), false))
    @Test fun unknownKeyIgnored() =
        assertEquals(emptyList<Mark>(), DayMarks.cellMarks(log(0, "future_key"), false))
    @Test fun noteOnly() = assertEquals(listOf(Mark.NOTE), DayMarks.cellMarks(log(0, "", "hi"), false))
    @Test fun withMoodReplaces() =
        assertEquals(setOf("pill", "mood_angry"), DayMarks.withMood(setOf("pill", "mood", "mood_good"), "mood_angry"))
}
