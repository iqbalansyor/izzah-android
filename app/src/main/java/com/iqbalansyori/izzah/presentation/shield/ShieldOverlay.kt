package com.iqbalansyori.izzah.presentation.shield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import com.iqbalansyori.izzah.domain.model.LockMode
import com.iqbalansyori.izzah.domain.repository.JedaRepository
import com.iqbalansyori.izzah.domain.repository.JourneyRepository
import com.iqbalansyori.izzah.presentation.jeda.JedaContent
import com.iqbalansyori.izzah.presentation.jeda.JedaViewModel
import org.koin.compose.koinInject

enum class ShieldOutcome { CLOSE_TO_HOME, GRANT_PASS }

private enum class Phase { SHIELD, JEDA }

@Composable
fun ShieldOverlay(mode: LockMode, untilLabel: String, dayTime: Boolean, kept: Int, onFinished: (ShieldOutcome) -> Unit) {
    var phase by remember { mutableStateOf(Phase.SHIELD) }
    // a full lock never grants a pass: finishing the pause just sends the user home
    val afterJeda = if (mode == LockMode.FULL) ShieldOutcome.CLOSE_TO_HOME else ShieldOutcome.GRANT_PASS
    when (phase) {
        Phase.SHIELD -> ShieldContent(
            untilLabel = untilLabel,
            dayTime = dayTime,
            mode = mode,
            onJeda = { phase = Phase.JEDA },
            onClose = { onFinished(ShieldOutcome.CLOSE_TO_HOME) },
            onContinue = if (mode == LockMode.GENTLE) ({ onFinished(ShieldOutcome.GRANT_PASS) }) else null,
        )
        Phase.JEDA -> {
            val jeda = koinInject<JedaRepository>()
            val journey = koinInject<JourneyRepository>()
            // a service has no ViewModelStoreOwner, so the view model is built by hand and cleared when the pause leaves composition
            val vm = remember { JedaViewModel(jeda, journey) }
            DisposableEffect(vm) { onDispose { vm.clear() } }
            val state by vm.state.collectAsState()
            LaunchedEffect(state.secondsLeft) { if (state.secondsLeft == 0) onFinished(afterJeda) }
            JedaContent(state.copy(kept = kept), onCalmer = { vm.calmer(); onFinished(afterJeda) }, onDoa = null)
        }
    }
}
