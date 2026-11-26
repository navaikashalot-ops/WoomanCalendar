@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mycycle.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mycycle.MainViewModel
import com.example.mycycle.R
import com.example.mycycle.UiState
import com.example.mycycle.data.BackupInfo
import com.example.mycycle.ui.AVATAR_KEYS
import com.example.mycycle.ui.AvatarPicker
import com.example.mycycle.ui.theme.PeonyShapes
import java.text.DateFormat
import java.util.Date

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
fun SettingsScreen(state: UiState, info: BackupInfo, vm: MainViewModel) {
    var name by remember(state.profile) { mutableStateOf(state.profile?.name ?: "") }
    var avatar by remember(state.profile) { mutableStateOf(state.profile?.avatar ?: AVATAR_KEYS.first()) }
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsCard(stringResource(R.string.set_profile_title)) {
            AvatarPicker(selected = avatar, onSelect = { avatar = it })
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(40) },
                label = { Text(stringResource(R.string.ob_name)) },
                singleLine = true,
                shape = PeonyShapes.Button,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { vm.saveProfile(name, avatar) },
                shape = PeonyShapes.Button,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.set_profile_save), fontWeight = FontWeight.Bold) }
        }

        SettingsCard(stringResource(R.string.set_defaults_title)) {
            Text(stringResource(R.string.set_defaults_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Stepper(stringResource(R.string.set_cycle_len), cycleLen, 20, 45) { cycleLen = it }
            Stepper(stringResource(R.string.set_period_len), periodLen, 2, 10) { periodLen = it }
            Button(
                onClick = { vm.saveSettings(cycleLen, periodLen) },
                shape = PeonyShapes.Button,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.set_save), fontWeight = FontWeight.Bold) }
        }

        SettingsCard(stringResource(R.string.bk_title)) {
            Text(stringResource(R.string.bk_desc), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Button(
                onClick = { createLauncher.launch("cycle-backup.json") },
                shape = PeonyShapes.Button,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.bk_create)) }
            OutlinedButton(
                onClick = { vm.backupNow() },
                shape = PeonyShapes.Button,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.bk_now)) }
            OutlinedButton(
                onClick = { openLauncher.launch(Unit) },
                shape = PeonyShapes.Button,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.bk_restore)) }
            Text(stringResource(R.string.bk_restore_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    pendingRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            shape = PeonyShapes.Card,
            title = { Text(stringResource(R.string.restore_confirm_title)) },
            text = { Text(stringResource(R.string.restore_confirm_text)) },
            confirmButton = { TextButton(onClick = { vm.restore(uri); pendingRestore = null }) { Text(stringResource(R.string.replace)) } },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = PeonyShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            content()
        }
    }
}

@Composable
private fun Stepper(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f))
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
