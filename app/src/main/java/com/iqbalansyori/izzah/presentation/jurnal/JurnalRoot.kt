package com.iqbalansyori.izzah.presentation.jurnal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iqbalansyori.izzah.presentation.common.ObserveAsEvents
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun JurnalRoot(onBack: () -> Unit, vm: JurnalViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var toastTicket by remember { mutableIntStateOf(0) }
    var toastVisible by remember { mutableStateOf(false) }
    ObserveAsEvents(vm.events) {
        toastTicket++
        toastVisible = true
    }
    // each save restarts the 1.8 s window
    LaunchedEffect(toastTicket) {
        if (toastTicket > 0) {
            delay(1800)
            toastVisible = false
        }
    }
    JurnalScreen(state, toastVisible, vm::onAction, onBack)
}
