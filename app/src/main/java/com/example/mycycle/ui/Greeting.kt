package com.example.mycycle.ui

import androidx.annotation.StringRes
import com.example.mycycle.R

@StringRes
fun greetingRes(hour: Int): Int = when (hour) {
    in 5..11 -> R.string.greet_morning
    in 12..17 -> R.string.greet_day
    else -> R.string.greet_evening
}

fun greetingText(base: String, name: String): String =
    if (name.isBlank()) base else "$base, ${name.trim()}"
