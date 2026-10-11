package com.iqbalansyori.izzah.presentation.unlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
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
import com.iqbalansyori.izzah.designsystem.component.GhostButton
import com.iqbalansyori.izzah.designsystem.component.PinDots
import com.iqbalansyori.izzah.designsystem.component.PinPad
import com.iqbalansyori.izzah.designsystem.component.StarLogo
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@Composable
fun UnlockScreen(state: UnlockState, onAction: (UnlockAction) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(IzzahColors.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        StarLogo(48.dp, stroke = 2.5.dp)
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.unlock_title), style = IzzahType.outfit(28.sp, FontWeight.Bold, tracking = -.02f), textAlign = TextAlign.Center)
            Text(
                stringResource(R.string.unlock_subtitle),
                Modifier.widthIn(max = 280.dp),
                style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.45f),
                color = IzzahColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
        PinDots(state.entry.length, error = state.wrong)
        val status = when {
            state.cooldownSeconds > 0 -> pluralStringResource(R.plurals.unlock_cooldown, state.cooldownSeconds, state.cooldownSeconds)
            state.wrong -> stringResource(R.string.unlock_wrong)
            else -> ""
        }
        Text(
            status,
            Modifier.fillMaxWidth(),
            style = IzzahType.outfit(14.sp, FontWeight.Medium),
            color = if (state.cooldownSeconds > 0) IzzahColors.TextSecondary else IzzahColors.Destructive,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        PinPad({ onAction(UnlockAction.Digit(it)) }, { onAction(UnlockAction.Delete) })
        GhostButton(stringResource(R.string.unlock_forgot), { onAction(UnlockAction.ForgotTapped) }, Modifier.widthIn(max = 280.dp))
    }
}
