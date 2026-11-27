@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mycycle.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.example.mycycle.R
import com.example.mycycle.data.ReminderConfig
import com.example.mycycle.ui.theme.PeonyShapes
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Карточка «Напоминание»: вкл/выкл, за сколько дней (1–10) и время. Изменения применяются сразу. */
@Composable
fun ReminderCard(cfg: ReminderConfig, onChange: (ReminderConfig) -> Unit, stepper: @Composable (String, Int, Int, Int, (Int) -> Unit) -> Unit) {
    val ctx = LocalContext.current
    var picking by remember { mutableStateOf(false) }
    var allowed by remember { mutableStateOf(NotificationManagerCompat.from(ctx).areNotificationsEnabled()) }
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        allowed = granted
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.rem_enable), Modifier.weight(1f))
        Switch(
            checked = cfg.enabled,
            onCheckedChange = { on ->
                onChange(cfg.copy(enabled = on))
                if (on && !allowed && Build.VERSION.SDK_INT >= 33) {
                    askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
        )
    }
    if (cfg.enabled) {
        if (!allowed) {
            Text(stringResource(R.string.rem_denied), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
        }
        stepper(stringResource(R.string.rem_days), cfg.daysBefore, 1, 10) { onChange(cfg.copy(daysBefore = it)) }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.rem_time), Modifier.weight(1f))
            OutlinedButton(onClick = { picking = true }, shape = PeonyShapes.Button) {
                Text(cfg.time.format(DateTimeFormatter.ofPattern("HH:mm")), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (picking) {
        val state = rememberTimePickerState(cfg.time.hour, cfg.time.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { picking = false },
            shape = PeonyShapes.Card,
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    onChange(cfg.copy(time = LocalTime.of(state.hour, state.minute)))
                    picking = false
                }) { Text(stringResource(R.string.rem_ok)) }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
