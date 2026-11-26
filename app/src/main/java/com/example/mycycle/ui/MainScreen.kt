package com.example.mycycle.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    var tab by remember { mutableStateOf(0) }

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
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
