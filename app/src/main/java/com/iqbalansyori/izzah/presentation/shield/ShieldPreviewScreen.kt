package com.iqbalansyori.izzah.presentation.shield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iqbalansyori.izzah.data.locale.LocaleFormats
import com.iqbalansyori.izzah.presentation.protect.ProtectViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ShieldPreviewScreen(onJeda: () -> Unit, onClose: () -> Unit, vm: ProtectViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val formats = koinInject<LocaleFormats>()
    val slot = state.slots.firstOrNull { it.enabled } ?: state.slots.firstOrNull()
    val end = slot?.endMinute ?: 60
    // night slots start in the evening or after midnight; anything else gets the daytime wording
    val night = slot == null || slot.startMinute >= 18 * 60 || slot.startMinute < 6 * 60
    ShieldContent(
        untilLabel = formats.minuteOfDay(end),
        dayTime = !night,
        mode = state.mode,
        onJeda = onJeda,
        onClose = onClose,
        onContinue = onClose,
    )
}
