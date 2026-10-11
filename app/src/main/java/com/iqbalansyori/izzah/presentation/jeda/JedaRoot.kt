package com.iqbalansyori.izzah.presentation.jeda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun JedaRoot(onClose: () -> Unit, onDoa: () -> Unit, vm: JedaViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(state.inhale) {
        if (state.breaths > 1 || state.inhale) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    JedaContent(state, onCalmer = { vm.calmer(); onClose() }, onDoa = onDoa)
}
