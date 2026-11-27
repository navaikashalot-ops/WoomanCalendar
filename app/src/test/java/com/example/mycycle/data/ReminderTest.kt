package com.example.mycycle.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderTest {
    private val starts = listOf(LocalDate.parse("2026-12-10"), LocalDate.parse("2027-01-07"))

    @Test fun fiveDaysBeforeAtNine() {
        val r = nextReminder(starts, ReminderConfig(), LocalDateTime.parse("2026-11-27T12:00"))
        assertEquals(PlannedReminder(LocalDate.parse("2026-12-10"), LocalDateTime.parse("2026-12-05T09:00")), r)
    }

    @Test fun passedTimeMovesToNextCycle() {
        val r = nextReminder(starts, ReminderConfig(), LocalDateTime.parse("2026-12-05T09:00"))
        assertEquals(LocalDateTime.parse("2027-01-02T09:00"), r?.at)
    }

    @Test fun customDaysAndTime() {
        val r = nextReminder(starts, ReminderConfig(true, 2, LocalTime.of(20, 30)), LocalDateTime.parse("2026-11-27T12:00"))
        assertEquals(LocalDateTime.parse("2026-12-08T20:30"), r?.at)
    }

    @Test fun disabledGivesNothing() =
        assertNull(nextReminder(starts, ReminderConfig(enabled = false), LocalDateTime.parse("2026-11-27T12:00")))

    @Test fun noPredictionGivesNothing() =
        assertNull(nextReminder(emptyList(), ReminderConfig(), LocalDateTime.parse("2026-11-27T12:00")))
}
