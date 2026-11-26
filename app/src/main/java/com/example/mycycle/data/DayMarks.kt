package com.example.mycycle.data

enum class Mark {
    PERIOD, FERTILE, OVULATION, CRAMPS, HEADACHE, BLOATING, FATIGUE,
    MOOD_GOOD, MOOD_SAD, MOOD_ANGRY, SEX, PILL, SWEET, NOTE,
}

object DayMarks {
    val MOODS: List<String> = listOf("mood_good", "mood", "mood_angry")
    val SYMPTOMS: List<String> = listOf("cramps", "headache", "bloating", "fatigue")
    val EXTRAS: List<String> = listOf("sex", "pill", "sweet")

    private val MOOD_MARKS = mapOf(
        "mood_good" to Mark.MOOD_GOOD,
        "mood" to Mark.MOOD_SAD,
        "mood_angry" to Mark.MOOD_ANGRY,
    )
    private val SYMPTOM_MARKS = mapOf(
        "cramps" to Mark.CRAMPS,
        "headache" to Mark.HEADACHE,
        "bloating" to Mark.BLOATING,
        "fatigue" to Mark.FATIGUE,
    )
    private val EXTRA_MARKS = mapOf(
        "sex" to Mark.SEX,
        "pill" to Mark.PILL,
        "sweet" to Mark.SWEET,
    )

    fun parse(symptoms: String): Set<String> =
        symptoms.split("|").filter { it.isNotEmpty() }.toSet()

    fun withMood(keys: Set<String>, mood: String?): Set<String> {
        val result = keys.filterNot { it in MOODS }.toMutableSet()
        if (mood != null) result.add(mood)
        return result
    }

    fun cellMarks(log: DayLog?, isOvulation: Boolean, max: Int = 2): List<Mark> {
        if (log == null) return if (isOvulation) listOf(Mark.OVULATION).take(max) else emptyList()
        val keys = parse(log.symptoms)
        val marks = mutableListOf<Mark>()
        if (log.flow > 0) marks.add(Mark.PERIOD)
        MOODS.firstOrNull { it in keys }?.let { marks.add(MOOD_MARKS.getValue(it)) }
        SYMPTOMS.filter { it in keys }.forEach { marks.add(SYMPTOM_MARKS.getValue(it)) }
        EXTRAS.filter { it in keys }.forEach { marks.add(EXTRA_MARKS.getValue(it)) }
        if (log.note.isNotBlank()) marks.add(Mark.NOTE)
        return marks.take(max)
    }
}
