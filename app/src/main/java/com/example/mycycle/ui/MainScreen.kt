@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mycycle.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mycycle.MainViewModel
import com.example.mycycle.R
import com.example.mycycle.UiState
import com.example.mycycle.data.BackupInfo
import com.example.mycycle.data.DayLog
import com.example.mycycle.data.Prediction
import java.text.DateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

private fun loc(): Locale = Locale.getDefault()
private fun dayFmt(): DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", loc())

private val PeriodColor = Color(0xFFEF9A9A)
private val PredictedColor = Color(0xFFFFD6D6)
private val OvulationColor = Color(0xFF81C784)
private val FertileColor = Color(0xFFDCEFDC)

// Ключи симптомов хранятся в базе независимо от языка интерфейса.
private val SYMPTOMS = listOf(
    "cramps" to R.string.sym_cramps,
    "headache" to R.string.sym_headache,
    "bloating" to R.string.sym_bloating,
    "fatigue" to R.string.sym_fatigue,
    "mood" to R.string.sym_mood,
)
private val FLOW_LABELS = listOf(R.string.flow_none, R.string.flow_light, R.string.flow_medium, R.string.flow_heavy)

/** Выбор существующего файла с правом записи и постоянным доступом (для связи с файлом копии на Drive). */
class OpenWritableDocument : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
            )
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        if (resultCode == android.app.Activity.RESULT_OK) intent?.data else null
}

@Composable
fun MainScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val info by vm.backupInfo.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Text("📅") },
                    label = { Text(stringResource(R.string.nav_calendar)) },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Text("⚙️") },
                    label = { Text(stringResource(R.string.nav_settings)) },
                )
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            if (tab == 0) {
                CalendarScreen(state, vm, goSettings = { tab = 1 })
            } else {
                SettingsScreen(state, info, vm)
            }
        }
    }
}

@Composable
private fun CalendarScreen(state: UiState, vm: MainViewModel, goSettings: () -> Unit) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selected by remember { mutableStateOf<LocalDate?>(null) }
    val today = LocalDate.now()
    val pred = state.prediction

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
    ) {
        SummaryCard(state, today)

        if (state.loaded && state.cycles.isEmpty() && state.logs.isEmpty()) {
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.empty_banner))
                    TextButton(onClick = goSettings) { Text(stringResource(R.string.open_backup_settings)) }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { month = month.minusMonths(1) }) { Text("‹", fontSize = 24.sp) }
            Text(
                text = month.month.getDisplayName(TextStyle.FULL_STANDALONE, loc())
                    .replaceFirstChar { it.uppercase() } + " " + month.year,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            TextButton(onClick = { month = month.plusMonths(1) }) { Text("›", fontSize = 24.sp) }
        }

        Row(Modifier.fillMaxWidth()) {
            java.time.DayOfWeek.values().forEach {
                Text(
                    it.getDisplayName(TextStyle.SHORT, loc()),
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                )
            }
        }

        val offset = month.atDay(1).dayOfWeek.value - 1
        val len = month.lengthOfMonth()
        val rows = (offset + len + 6) / 7
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (c in 0..6) {
                    val dayNum = r * 7 + c - offset + 1
                    if (dayNum in 1..len) {
                        val date = month.atDay(dayNum)
                        DayCell(
                            date = date,
                            color = cellColor(date, pred),
                            isToday = date == today,
                            hasLog = state.logs.containsKey(date.toString()),
                            onClick = { selected = date },
                        )
                    } else {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Legend()

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.startPeriod(today) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.btn_started_today)) }
            OutlinedButton(onClick = { vm.endPeriod(today) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.btn_ended_today)) }
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

private fun cellColor(d: LocalDate, p: Prediction): Color? = when {
    d in p.actualPeriodDays -> PeriodColor
    d in p.predictedPeriodDays -> PredictedColor
    d in p.ovulationDays -> OvulationColor
    d in p.fertileDays -> FertileColor
    else -> null
}

@Composable
private fun RowScope.DayCell(date: LocalDate, color: Color?, isToday: Boolean, hasLog: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    var m = Modifier
        .weight(1f)
        .aspectRatio(1f)
        .padding(2.dp)
        .clip(shape)
    if (color != null) m = m.background(color)
    if (isToday) m = m.border(2.dp, MaterialTheme.colorScheme.primary, shape)
    Box(m.clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(date.dayOfMonth.toString(), fontSize = 14.sp)
        if (hasLog) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun Legend() {
    Column {
        LegendRow(PeriodColor, stringResource(R.string.legend_period))
        LegendRow(PredictedColor, stringResource(R.string.legend_predicted))
        LegendRow(FertileColor, stringResource(R.string.legend_fertile))
        LegendRow(OvulationColor, stringResource(R.string.legend_ovulation))
        Text(stringResource(R.string.legend_note), fontSize = 12.sp)
    }
}

@Composable
private fun LegendRow(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
        Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Spacer(Modifier.size(8.dp))
        Text(text, fontSize = 13.sp)
    }
}

@Composable
private fun SummaryCard(state: UiState, today: LocalDate) {
    val p = state.prediction
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            if (state.cycles.isEmpty()) {
                Text(stringResource(R.string.hint_first))
            } else {
                val lastStart = state.cycles.maxOf { LocalDate.parse(it.startDate) }
                val cycleDay = ChronoUnit.DAYS.between(lastStart, today).toInt() + 1
                if (cycleDay >= 1) Text(stringResource(R.string.cycle_day, cycleDay), fontWeight = FontWeight.Bold)
                val first = p.predictedStarts.firstOrNull()
                if (first != null) {
                    if (first.isBefore(today)) {
                        val late = ChronoUnit.DAYS.between(first, today)
                        Text(stringResource(R.string.expected_late, first.format(dayFmt()), late.toInt()))
                    } else {
                        val left = ChronoUnit.DAYS.between(today, first)
                        val whenText = when (left) {
                            0L -> stringResource(R.string.when_today)
                            1L -> stringResource(R.string.when_tomorrow)
                            else -> stringResource(R.string.when_in_days, left.toInt())
                        }
                        Text(stringResource(R.string.next_period, first.format(dayFmt()), whenText))
                    }
                }
                Text(stringResource(R.string.avg_summary, p.avgCycle, p.avgPeriod), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun DayDialog(date: LocalDate, log: DayLog?, isCycleStart: Boolean, vm: MainViewModel, onClose: () -> Unit) {
    var flow by remember(date) { mutableStateOf(log?.flow ?: 0) }
    var symptoms by remember(date) { mutableStateOf(log?.symptoms?.split("|")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()) }
    var note by remember(date) { mutableStateOf(log?.note ?: "") }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", loc()))) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.startPeriod(date); onClose() }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.dlg_start)) }
                    OutlinedButton(onClick = { vm.endPeriod(date); onClose() }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.dlg_end)) }
                }
                if (isCycleStart) {
                    TextButton(onClick = { vm.deleteCycleStartingAt(date); onClose() }) { Text(stringResource(R.string.dlg_delete_cycle)) }
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.dlg_flow), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FLOW_LABELS.forEachIndexed { i, labelRes ->
                        FilterChip(selected = flow == i, onClick = { flow = i }, label = { Text(stringResource(labelRes), fontSize = 12.sp) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.dlg_symptoms), fontWeight = FontWeight.Bold)
                SYMPTOMS.forEach { (key, labelRes) ->
                    FilterChip(
                        selected = key in symptoms,
                        onClick = { symptoms = if (key in symptoms) symptoms - key else symptoms + key },
                        label = { Text(stringResource(labelRes)) },
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.dlg_note)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                vm.saveLog(DayLog(date.toString(), flow, symptoms.joinToString("|"), note))
                onClose()
            }) { Text(stringResource(R.string.dlg_save)) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.dlg_close)) } },
    )
}

@Composable
private fun SettingsScreen(state: UiState, info: BackupInfo, vm: MainViewModel) {
    var cycleLen by remember(state.settings) { mutableStateOf(state.settings.cycleLength) }
    var periodLen by remember(state.settings) { mutableStateOf(state.settings.periodLength) }
    var pendingRestore by remember { mutableStateOf<Uri?>(null) }

    val createLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> if (uri != null) vm.createBackupFile(uri) }

    val openLauncher = rememberLauncherForActivityResult(OpenWritableDocument()) { uri ->
        if (uri != null) {
            if (state.cycles.isEmpty() && state.logs.isEmpty()) vm.restore(uri) else pendingRestore = uri
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.set_defaults_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(stringResource(R.string.set_defaults_hint), fontSize = 12.sp)
        Stepper(stringResource(R.string.set_cycle_len), cycleLen, 20, 45) { cycleLen = it }
        Stepper(stringResource(R.string.set_period_len), periodLen, 2, 10) { periodLen = it }
        Button(onClick = { vm.saveSettings(cycleLen, periodLen) }) { Text(stringResource(R.string.set_save)) }

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.bk_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(stringResource(R.string.bk_desc), fontSize = 13.sp)
        Text(stringResource(R.string.bk_file, info.fileLabel ?: stringResource(R.string.bk_file_none)))
        info.lastOk?.let {
            Text(
                stringResource(R.string.bk_last, DateFormat.getDateTimeInstance().format(Date(it))),
                fontSize = 13.sp,
            )
        }
        info.lastError?.let {
            Text(stringResource(R.string.bk_error, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        Button(onClick = { createLauncher.launch("cycle-backup.json") }) { Text(stringResource(R.string.bk_create)) }
        OutlinedButton(onClick = { vm.backupNow() }) { Text(stringResource(R.string.bk_now)) }
        OutlinedButton(onClick = { openLauncher.launch(Unit) }) { Text(stringResource(R.string.bk_restore)) }
        Text(stringResource(R.string.bk_restore_hint), fontSize = 12.sp)
    }

    pendingRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(stringResource(R.string.restore_confirm_title)) },
            text = { Text(stringResource(R.string.restore_confirm_text)) },
            confirmButton = { TextButton(onClick = { vm.restore(uri); pendingRestore = null }) { Text(stringResource(R.string.replace)) } },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun Stepper(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f))
        OutlinedButton(onClick = { if (value > min) onChange(value - 1) }) { Text("−") }
        Text(value.toString(), Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = { if (value < max) onChange(value + 1) }) { Text("+") }
    }
}
