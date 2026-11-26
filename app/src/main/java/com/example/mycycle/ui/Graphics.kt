package com.example.mycycle.ui

import androidx.annotation.DrawableRes
import com.example.mycycle.R
import com.example.mycycle.data.Mark

val AVATAR_KEYS: List<String> =
    listOf("bun", "long", "curly", "cat", "bunny", "peony", "butterfly", "moon")

@DrawableRes
fun Mark.iconRes(): Int = when (this) {
    Mark.PERIOD -> R.drawable.ic_mark_period
    Mark.FERTILE -> R.drawable.ic_mark_fertile
    Mark.OVULATION -> R.drawable.ic_mark_ovulation
    Mark.CRAMPS -> R.drawable.ic_mark_cramps
    Mark.HEADACHE -> R.drawable.ic_mark_headache
    Mark.BLOATING -> R.drawable.ic_mark_bloating
    Mark.FATIGUE -> R.drawable.ic_mark_fatigue
    Mark.MOOD_GOOD -> R.drawable.ic_mark_mood_good
    Mark.MOOD_SAD -> R.drawable.ic_mark_mood_sad
    Mark.MOOD_ANGRY -> R.drawable.ic_mark_mood_angry
    Mark.SEX -> R.drawable.ic_mark_sex
    Mark.PILL -> R.drawable.ic_mark_pill
    Mark.SWEET -> R.drawable.ic_mark_sweet
    Mark.NOTE -> R.drawable.ic_mark_note
}

@DrawableRes
fun avatarRes(key: String): Int = when (key) {
    "long" -> R.drawable.avatar_long
    "curly" -> R.drawable.avatar_curly
    "cat" -> R.drawable.avatar_cat
    "bunny" -> R.drawable.avatar_bunny
    "peony" -> R.drawable.avatar_peony
    "butterfly" -> R.drawable.avatar_butterfly
    "moon" -> R.drawable.avatar_moon
    else -> R.drawable.avatar_bun
}
