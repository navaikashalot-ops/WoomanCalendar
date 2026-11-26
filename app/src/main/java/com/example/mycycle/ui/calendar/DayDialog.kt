@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.mycycle.ui.calendar

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mycycle.MainViewModel
import com.example.mycycle.R
import com.example.mycycle.data.DayLog
import com.example.mycycle.data.DayMarks
import com.example.mycycle.data.Mark
import com.example.mycycle.ui.iconRes
import com.example.mycycle.ui.theme.PeonyShapes
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FLOW_LABELS = listOf(R.string.flow_none, R.string.flow_light, R.string.flow_medium, R.string.flow_heavy)

// Ключи хранятся в базе независимо от языка интерфейса.
private data class Option(val key: String, @StringRes val label: Int, val mark: Mark)

private val MOOD_OPTIONS = listOf(
    Option("mood_good", R.string.mood_good, Mark.MOOD_GOOD),
    Option("mood", R.string.sym_mood, Mark.MOOD_SAD),
    Option("mood_angry", R.string.mood_angry, Mark.MOOD_ANGRY),
)
private val SYMPTOM_OPTIONS = listOf(
    Option("cramps", R.string.sym_cramps, Mark.CRAMPS),
    Option("headache", R.string.sym_headache, Mark.HEADACHE),
    Option("bloating", R.string.sym_bloating, Mark.BLOATING),
    Option("fatigue", R.string.sym_fatigue, Mark.FATIGUE),
)
private val EXTRA_OPTIONS = listOf(
    Option("sex", R.string.extra_sex, Mark.SEX),
    Option("pill", R.string.extra_pill, Mark.PILL),
    Option("sweet", R.string.extra_sweet, Mark.SWEET),
)

@Composable
fun DayDialog(date: LocalDate, log: DayLog?, isCycleStart: Boolean, vm: MainViewModel, onClose: () -> Unit) {
    val initial = remember(date) { DayMarks.parse(log?.symptoms ?: "") }
    var flow by remember(date) { mutableStateOf(log?.flow ?: 0) }
    var mood by remember(date) { mutableStateOf(DayMarks.MOODS.firstOrNull { it in initial }) }
    var keys by remember(date) { mutableStateOf(initial.filterNot { it in DayMarks.MOODS }.toSet()) }
    var note by remember(date) { mutableStateOf(log?.note ?: "") }

    AlertDialog(
        onDismissRequest = onClose,
        shape = PeonyShapes.Card,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", loc())), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { vm.startPeriod(date); onClose() },
                        shape = PeonyShapes.Button,
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.dlg_start)) }
                    OutlinedButton(
                        onClick = { vm.endPeriod(date); onClose() },
                        shape = PeonyShapes.Button,
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.dlg_end)) }
                }
                if (isCycleStart) {
                    TextButton(onClick = { vm.deleteCycleStartingAt(date); onClose() }) {
                        Text(stringResource(R.string.dlg_delete_cycle))
                    }
                }

                SectionTitle(R.string.dlg_flow)
                val dropIcon: @Composable () -> Unit = { MarkIcon(Mark.PERIOD, 16) }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FLOW_LABELS.forEachIndexed { i, labelRes ->
                        FilterChip(
                            selected = flow == i,
                            onClick = { flow = i },
                            label = { Text(stringResource(labelRes), fontSize = 12.sp) },
                            leadingIcon = dropIcon.takeIf { i > 0 },
                            shape = PeonyShapes.Button,
                        )
                    }
                }

                SectionTitle(R.string.mood_title)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    MOOD_OPTIONS.forEach { opt ->
                        val on = mood == opt.key
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(opt.mark.iconRes()),
                                contentDescription = stringResource(opt.label),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
                                    )
                                    .border(
                                        width = if (on) 2.dp else 1.dp,
                                        color = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape,
                                    )
                                    .clickable { mood = if (on) null else opt.key }
                                    .padding(7.dp),
                            )
                            Text(
                                stringResource(opt.label),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                SectionTitle(R.string.dlg_symptoms)
                MultiChips(SYMPTOM_OPTIONS + EXTRA_OPTIONS, keys) { key ->
                    keys = if (key in keys) keys - key else keys + key
                }

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.dlg_note)) },
                    shape = PeonyShapes.Button,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val all = DayMarks.withMood(keys, mood)
                vm.saveLog(DayLog(date.toString(), flow, all.joinToString("|"), note))
                onClose()
            }) { Text(stringResource(R.string.dlg_save), fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.dlg_close)) } },
    )
}

@Composable
private fun SectionTitle(@StringRes res: Int) {
    Spacer(Modifier.height(10.dp))
    Text(stringResource(res), fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun MarkIcon(mark: Mark, sizeDp: Int) {
    Image(
        painter = painterResource(mark.iconRes()),
        contentDescription = null,
        modifier = Modifier.size(sizeDp.dp),
    )
}

@Composable
private fun MultiChips(options: List<Option>, selected: Set<String>, onToggle: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { opt ->
            FilterChip(
                selected = opt.key in selected,
                onClick = { onToggle(opt.key) },
                label = { Text(stringResource(opt.label)) },
                leadingIcon = { MarkIcon(opt.mark, 20) },
                shape = PeonyShapes.Button,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}
