@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mycycle.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mycycle.R
import com.example.mycycle.data.OnboardingMode
import com.example.mycycle.ui.AVATAR_KEYS
import com.example.mycycle.ui.avatarRes
import com.example.mycycle.ui.theme.PeonyShapes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val DEFAULT_CYCLE = 28
private const val DEFAULT_PERIOD = 5

@Composable
fun OnboardingScreen(
    mode: OnboardingMode,
    onDone: (name: String, avatar: String, lastStart: LocalDate?, cycleLen: Int, periodLen: Int) -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var avatar by rememberSaveable { mutableStateOf(AVATAR_KEYS.first()) }
    var cycleLen by rememberSaveable { mutableIntStateOf(DEFAULT_CYCLE) }
    var periodLen by rememberSaveable { mutableIntStateOf(DEFAULT_PERIOD) }
    val totalSteps = if (mode == OnboardingMode.FULL) 2 else 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        StepDots(total = totalSteps, current = step)
        Spacer(Modifier.height(20.dp))
        if (step == 0) {
            ProfileStep(
                name = name,
                onName = { name = it },
                avatar = avatar,
                onAvatar = { avatar = it },
                onNext = {
                    if (mode == OnboardingMode.FULL) step = 1
                    else onDone(name, avatar, null, DEFAULT_CYCLE, DEFAULT_PERIOD)
                },
            )
        } else {
            CycleStep(
                cycleLen = cycleLen,
                onCycleLen = { cycleLen = it },
                periodLen = periodLen,
                onPeriodLen = { periodLen = it },
                onSkip = { onDone(name, avatar, null, cycleLen, periodLen) },
                onDone = { date -> onDone(name, avatar, date, cycleLen, periodLen) },
            )
        }
    }
}

@Composable
private fun StepDots(total: Int, current: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(total) { i ->
            val on = i == current
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = if (on) 22.dp else 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer),
            )
        }
    }
}

@Composable
private fun Header(title: String, sub: String) {
    Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
    Spacer(Modifier.height(6.dp))
    Text(sub, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun ProfileStep(
    name: String,
    onName: (String) -> Unit,
    avatar: String,
    onAvatar: (String) -> Unit,
    onNext: () -> Unit,
) {
    Header(stringResource(R.string.ob_hello_title), stringResource(R.string.ob_hello_sub))
    AVATAR_KEYS.chunked(4).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            row.forEach { key ->
                val selected = key == avatar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                        )
                        .clickable { onAvatar(key) }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(avatarRes(key)),
                        contentDescription = key,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = name,
        onValueChange = { onName(it.take(40)) },
        label = { Text(stringResource(R.string.ob_name)) },
        singleLine = true,
        shape = PeonyShapes.Button,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = onNext,
        shape = PeonyShapes.Button,
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) { Text(stringResource(R.string.ob_next), fontWeight = FontWeight.Bold) }
}

private object PastOrToday : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        !millisToDate(utcTimeMillis).isAfter(LocalDate.now())

    override fun isSelectableYear(year: Int): Boolean = year <= LocalDate.now().year
}

private fun millisToDate(ms: Long): LocalDate =
    Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
private fun CycleStep(
    cycleLen: Int,
    onCycleLen: (Int) -> Unit,
    periodLen: Int,
    onPeriodLen: (Int) -> Unit,
    onSkip: () -> Unit,
    onDone: (LocalDate) -> Unit,
) {
    Header(stringResource(R.string.ob_cycle_title), stringResource(R.string.ob_cycle_sub))
    val currentYear = remember { LocalDate.now().year }
    val pickerState = rememberDatePickerState(
        yearRange = (currentYear - 1)..currentYear,
        selectableDates = PastOrToday,
    )
    Card(
        shape = PeonyShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            stringResource(R.string.ob_last_start),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp),
        )
        DatePicker(
            state = pickerState,
            title = null,
            headline = null,
            showModeToggle = false,
            colors = DatePickerDefaults.colors(containerColor = Color.Transparent),
        )
    }
    Spacer(Modifier.height(12.dp))
    Card(
        shape = PeonyShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OnboardingStepper(stringResource(R.string.set_cycle_len), cycleLen, 21, 45, onCycleLen)
            OnboardingStepper(stringResource(R.string.set_period_len), periodLen, 2, 10, onPeriodLen)
        }
    }
    Spacer(Modifier.height(24.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onSkip,
            shape = PeonyShapes.Button,
            modifier = Modifier.weight(1f).height(52.dp),
        ) { Text(stringResource(R.string.ob_dont_remember)) }
        val selected = pickerState.selectedDateMillis
        Button(
            onClick = { if (selected != null) onDone(millisToDate(selected)) },
            enabled = selected != null,
            shape = PeonyShapes.Button,
            modifier = Modifier.weight(1f).height(52.dp),
        ) { Text(stringResource(R.string.ob_done), fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun OnboardingStepper(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        OutlinedButton(
            onClick = { if (value > min) onChange(value - 1) },
            enabled = value > min,
            shape = PeonyShapes.Button,
        ) { Text("−") }
        Text(
            value.toString(),
            Modifier.width(44.dp).padding(horizontal = 4.dp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(
            onClick = { if (value < max) onChange(value + 1) },
            enabled = value < max,
            shape = PeonyShapes.Button,
        ) { Text("+") }
    }
}
