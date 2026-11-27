package com.example.mycycle.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mycycle.MainViewModel
import com.example.mycycle.R
import com.example.mycycle.data.OnboardingMode
import com.example.mycycle.ui.calendar.CalendarScreen
import com.example.mycycle.ui.onboarding.OnboardingScreen
import com.example.mycycle.ui.settings.SettingsScreen

@Composable
fun MainScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val info by vm.backupInfo.collectAsStateWithLifecycle()
    val reminder by vm.reminder.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }

    if (!state.loaded) return
    if (state.onboarding != OnboardingMode.NONE) {
        OnboardingScreen(mode = state.onboarding) { name, avatar, lastStart, cycleLen, periodLen ->
            if (state.onboarding == OnboardingMode.PROFILE_ONLY) {
                vm.saveProfile(name, avatar)
            } else {
                vm.completeOnboarding(name, avatar, lastStart, cycleLen, periodLen)
            }
        }
        return
    }

    // Напоминание включено по умолчанию: на Android 13+ один раз за запуск спрашиваем разрешение на уведомления.
    val ctx = LocalContext.current
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(reminder.enabled) {
        if (reminder.enabled && Build.VERSION.SDK_INT >= 33 &&
            !NotificationManagerCompat.from(ctx).areNotificationsEnabled()
        ) {
            askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { PeonyTabBar(tab, onSelect = { tab = it }) },
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

/** Нижние вкладки: белая «таблетка» с двумя сегментами, выбранный залит акцентом. */
@Composable
private fun PeonyTabBar(selected: Int, onSelect: (Int) -> Unit) {
    val pill = RoundedCornerShape(28.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(8.dp, pill, ambientColor = MaterialTheme.colorScheme.primary, spotColor = MaterialTheme.colorScheme.primary)
                .clip(pill)
                .background(MaterialTheme.colorScheme.surface)
                .padding(5.dp),
        ) {
            TabSegment(R.drawable.ic_tab_calendar, R.string.nav_calendar, selected == 0) { onSelect(0) }
            TabSegment(R.drawable.ic_tab_settings, R.string.nav_settings, selected == 1) { onSelect(1) }
        }
    }
}

@Composable
private fun RowScope.TabSegment(icon: Int, label: Int, on: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (on) MaterialTheme.colorScheme.primary else Color.Transparent, label = "tabBg")
    val fg by animateColorAsState(
        if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabFg",
    )
    Row(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(label), color = fg, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
