package com.example.mycycle.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** Один цикл: от первого дня месячных. Даты — ISO (yyyy-MM-dd). */
@Serializable
@Entity(tableName = "cycles")
data class Cycle(
    @PrimaryKey val id: String,
    val startDate: String,
    val endDate: String? = null,
    val updatedAt: Long = 0,
)

/** Дневная запись: интенсивность, симптомы (через "|"), заметка. */
@Serializable
@Entity(tableName = "day_logs")
data class DayLog(
    @PrimaryKey val date: String,
    val flow: Int = 0,
    val symptoms: String = "",
    val note: String = "",
    val updatedAt: Long = 0,
)

@Serializable
@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val cycleLength: Int = 28,
    val periodLength: Int = 5,
    val updatedAt: Long = 0,
)

@Serializable
@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val avatar: String = "bun",
    val onboarded: Boolean = false,
    val updatedAt: Long = 0,
)

/** Полный снимок данных для резервной копии (JSON). */
@Serializable
data class Snapshot(
    val schemaVersion: Int = 2,
    val exportedAt: Long = 0,
    val settings: Settings = Settings(),
    val cycles: List<Cycle> = emptyList(),
    val dayLogs: List<DayLog> = emptyList(),
    val profile: Profile? = null,
)
