package com.example.mycycle.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SnapshotTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun v1DecodesWithNullProfile() {
        val s = json.decodeFromString<Snapshot>("""{"schemaVersion":1,"cycles":[{"id":"a","startDate":"2026-11-03"}]}""")
        assertNull(s.profile)
        assertEquals(1, s.cycles.size)
    }

    @Test fun v2RoundTrip() {
        val s = Snapshot(profile = Profile(name = "Аня", avatar = "cat", onboarded = true))
        assertEquals(s.profile, json.decodeFromString<Snapshot>(json.encodeToString(s)).profile)
    }
}
