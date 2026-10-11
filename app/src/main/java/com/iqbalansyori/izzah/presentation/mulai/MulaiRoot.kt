package com.iqbalansyori.izzah.presentation.mulai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iqbalansyori.izzah.presentation.common.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun MulaiRoot(onClose: () -> Unit, onDone: () -> Unit, vm: MulaiViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    ObserveAsEvents(vm.events) { if (it == MulaiEvent.Done) onDone() }
    MulaiScreen(state, vm::onAction, onClose)
}
