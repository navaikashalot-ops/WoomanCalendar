package com.example.mycycle.ui.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mycycle.MainViewModel
import com.example.mycycle.R
import com.example.mycycle.UiState
import com.example.mycycle.data.DayLog
import com.example.mycycle.data.DayMarks
import com.example.mycycle.data.Prediction
import com.example.mycycle.ui.avatarRes
import com.example.mycycle.ui.greetingRes
import com.example.mycycle.ui.iconRes
import com.example.mycycle.ui.theme.PeonyColors
import com.example.mycycle.ui.theme.PeonyShapes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

internal fun loc(): Locale = Locale.getDefault()
private fun dayFmt(): DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", loc())

@Composable
fun CalendarScreen(state: UiState, vm: MainViewModel, goSettings: () -> Unit) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selected by remember { mutableStateOf<LocalDate?>(null) }
    val today = LocalDate.now()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Header(state)
        Spacer(Modifier.height(12.dp))
        SummaryCard(state, today)

        if (state.cycles.isEmpty() && state.logs.isEmpty()) {
            Spacer(Modifier.height(10.dp))
            PeonyCard {
                Column(Modifier.padding(14.dp)) {
                    Text(stringResource(R.string.empty_banner), fontSize = 14.sp)
                    TextButton(onClick = goSettings) { Text(stringResource(R.string.open_backup_settings)) }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        PeonyCard {
            Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                MonthHeader(month, onPrev = { month = month.minusMonths(1) }, onNext = { month = month.plusMonths(1) })
                Row(Modifier.fillMaxWidth()) {
                    DayOfWeek.values().forEach {
                        Text(
                            it.getDisplayName(TextStyle.SHORT, loc()),
                            Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                val first = month.atDay(1)
                val lead = first.dayOfWeek.value - 1
                val gridStart = first.minusDays(lead.toLong())
                val rows = (lead + month.lengthOfMonth() + 6) / 7
                for (r in 0 until rows) {
                    Row(Modifier.fillMaxWidth()) {
                        for (c in 0..6) {
                            val date = gridStart.plusDays((r * 7 + c).toLong())
                            DayCell(
                                date = date,
                                pred = state.prediction,
                                log = state.logs[date.toString()],
                                inMonth = YearMonth.from(date) == month,
                                isToday = date == today,
                                onClick = { selected = date },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Legend()

        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val inPeriod = today in state.prediction.actualPeriodDays
            OutlinedButton(
                onClick = { if (inPeriod) vm.endPeriod(today) else vm.startPeriod(today) },
                shape = PeonyShapes.Button,
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Text(
                    stringResource(if (inPeriod) R.string.btn_ended_today else R.string.btn_started_today),
                    textAlign = TextAlign.Center,
                )
            }
            Button(
                onClick = { selected = today },
                shape = PeonyShapes.Button,
                modifier = Modifier.weight(1f).height(52.dp),
            ) { Text(stringResource(R.string.btn_log_day), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        }
    }

    selected?.let { date ->
        DayDialog(
            date = date,
            log = state.logs[date.toString()],
            isCycleStart = state.cycles.any { it.startDate == date.toString() },
            vm = vm,
            onClose = { selected = null },
        )
    }
}

/** Белая карточка «Пиона» со скруглением 24dp. */
@Composable
internal fun PeonyCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        shape = PeonyShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth(),
    ) { content() }
}

@Composable
private fun Header(state: UiState) {
    val greeting = stringResource(greetingRes(LocalTime.now().hour))
    val name = state.profile?.name ?: ""
    Box(Modifier.fillMaxWidth().height(56.dp)) {
        Image(
            painter = painterResource(R.drawable.deco_peony),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 18.dp, y = (-14).dp)
                .size(90.dp)
                .alpha(0.55f),
        )
        Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(avatarRes(state.profile?.avatar ?: "")),
                contentDescription = stringResource(R.string.cd_avatar),
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    greeting,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (name.isNotBlank()) {
                    Text(
                        name.trim(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(state: UiState, today: LocalDate) {
    val p = state.prediction
    PeonyCard {
        if (state.cycles.isEmpty()) {
            Text(stringResource(R.string.hint_first), Modifier.padding(16.dp))
        } else {
            val lastStart = state.cycles.maxOf { LocalDate.parse(it.startDate) }
            val cycleDay = ChronoUnit.DAYS.between(lastStart, today).toInt() + 1
            val small = 13.sp
            val sec = MaterialTheme.colorScheme.onSurfaceVariant
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CycleRing(day = cycleDay, length = p.avgCycle)
                Spacer(Modifier.width(14.dp))
                Column {
                    if (cycleDay >= 1) {
                        Text(stringResource(R.string.cycle_day, cycleDay), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    val first = p.predictedStarts.firstOrNull()
                    if (first != null) {
                        if (first.isBefore(today)) {
                            val late = ChronoUnit.DAYS.between(first, today).toInt()
                            Text(stringResource(R.string.expected_late, first.format(dayFmt()), late), fontSize = small, color = sec)
                        } else {
                            val left = ChronoUnit.DAYS.between(today, first)
                            val whenText = when (left) {
                                0L -> stringResource(R.string.when_today)
                                1L -> stringResource(R.string.when_tomorrow)
                                else -> stringResource(R.string.when_in_days, left.toInt())
                            }
                            Text(stringResource(R.string.next_period, first.format(dayFmt()), whenText), fontSize = small, color = sec)
                        }
                    }
                    val phase = when {
                        today in p.actualPeriodDays -> R.string.phase_period
                        today in p.fertileDays || today in p.ovulationDays -> R.string.phase_fertile
                        else -> null
                    }
                    if (phase != null) {
                        Text(stringResource(phase), fontSize = small, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(stringResource(R.string.avg_summary, p.avgCycle, p.avgPeriod), fontSize = 12.sp, color = sec)
                }
            }
        }
    }
}

@Composable
private fun CycleRing(day: Int, length: Int) {
    val progress = if (length > 0) (day.toFloat() / length).coerceIn(0f, 1f) else 0f
    val accent = MaterialTheme.colorScheme.primary
    val track = PeonyColors.Period
    Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 7.dp.toPx()
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke),
            )
            drawArc(
                color = accent, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Text(if (day >= 1) day.toString() else "–", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onPrev) { Text("‹", fontSize = 24.sp) }
        Text(
            text = month.month.getDisplayName(TextStyle.FULL_STANDALONE, loc())
                .replaceFirstChar { it.uppercase() } + " " + month.year,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
        )
        TextButton(onClick = onNext) { Text("›", fontSize = 24.sp) }
    }
}

private fun cellColor(d: LocalDate, p: Prediction): Color? = when {
    d in p.actualPeriodDays -> PeonyColors.Period
    d in p.predictedPeriodDays -> PeonyColors.Forecast
    d in p.ovulationDays -> PeonyColors.Ovulation
    d in p.fertileDays -> PeonyColors.Fertile
    else -> null
}

@Composable
private fun RowScope.DayCell(
    date: LocalDate,
    pred: Prediction,
    log: DayLog?,
    inMonth: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val shape = PeonyShapes.CalendarCell
    val accent = MaterialTheme.colorScheme.primary
    val color = cellColor(date, pred)
    val isForecast = date !in pred.actualPeriodDays && date in pred.predictedPeriodDays
    var m = Modifier
        .weight(1f)
        .aspectRatio(1f)
        .padding(2.dp)
        .alpha(if (inMonth) 1f else 0.3f)
        .clip(shape)
    if (color != null) m = m.background(color)
    if (isForecast) {
        m = m.drawBehind {
            drawOutline(
                outline = shape.createOutline(size, layoutDirection, this),
                color = accent,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
                ),
            )
        }
    }
    if (isToday) m = m.border(2.dp, accent, shape)
    val marks = DayMarks.cellMarks(log, date in pred.ovulationDays)
    Column(
        m.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            date.dayOfMonth.toString(),
            fontSize = 14.sp,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (marks.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                marks.forEach { mark ->
                    Image(
                        painter = painterResource(mark.iconRes()),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Legend() {
    // Сетка 2×2: значки каждой колонки стоят на одной вертикали, текст выровнен от значка.
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            LegendDot(PeonyColors.Period, stringResource(R.string.legend_period))
            LegendDot(PeonyColors.Forecast, stringResource(R.string.legend_predicted), dashed = true)
        }
        Row(Modifier.fillMaxWidth()) {
            LegendDot(PeonyColors.Fertile, stringResource(R.string.legend_fertile))
            LegendDot(PeonyColors.Ovulation, stringResource(R.string.legend_ovulation))
        }
    }
}

@Composable
private fun RowScope.LegendDot(color: Color, text: String, dashed: Boolean = false) {
    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
        var m = Modifier.size(12.dp).clip(CircleShape).background(color)
        if (dashed) m = m.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
        Box(m)
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, lineHeight = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
