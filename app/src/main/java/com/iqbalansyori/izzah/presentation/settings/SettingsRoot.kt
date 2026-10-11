package com.iqbalansyori.izzah.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoot(nav: SettingsNav, vm: SettingsViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var counselor by remember { mutableStateOf(false) }
    SettingsScreen(state, vm::onAction, nav.copy(onCounselor = { counselor = true }))
    if (counselor) CounselorSheet { counselor = false }
}

@Composable
fun LanguageRoot(onBack: () -> Unit, vm: SettingsViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    LanguageScreen(state, vm::onAction, onBack)
}

@Composable
fun TargetEditRoot(onBack: () -> Unit, vm: SettingsViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    TargetEditScreen(state, vm::onAction, onBack)
}

@Composable
fun RemindersRoot(onBack: () -> Unit, vm: SettingsViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    RemindersScreen(state, vm::onAction, onBack)
}
