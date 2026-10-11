package com.iqbalansyori.izzah.presentation.jeda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.BreathingOrb
import com.iqbalansyori.izzah.designsystem.component.GhostButton
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

// shared with the shield overlay (Task 31), where onDoa is null
@Composable
fun JedaContent(state: JedaState, onCalmer: () -> Unit, onDoa: (() -> Unit)?, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 40.dp, bottom = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                pluralStringResource(R.plurals.jeda_header, state.secondsLeft, state.secondsLeft).uppercase(),
                style = IzzahType.outfit(12.5.sp, FontWeight.SemiBold, tracking = .18f),
                color = IzzahColors.TextSecondary,
            )
            Text(
                stringResource(R.string.jeda_reminder, state.kept),
                style = IzzahType.outfit(28.sp, FontWeight.Bold, tracking = -.02f, lineHeight = 1.15f),
                textAlign = TextAlign.Center,
            )
        }
        Box(contentAlignment = Alignment.Center) {
            BreathingOrb(state.inhale)
            Text(
                stringResource(if (state.inhale) R.string.jeda_inhale else R.string.jeda_exhale),
                style = IzzahType.outfit(19.sp, FontWeight.Bold),
                color = IzzahColors.Bg,
            )
        }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(R.string.jeda_istighfar),
                Modifier.fillMaxWidth(),
                style = IzzahType.arabic(24.sp),
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.jeda_translit, state.breaths),
                Modifier.fillMaxWidth(),
                style = IzzahType.outfit(14.sp, FontWeight.Normal),
                color = IzzahColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            PrimaryButton(stringResource(R.string.jeda_calmer), onCalmer)
            if (onDoa != null) GhostButton(stringResource(R.string.jeda_read_doa), onDoa)
        }
    }
}
