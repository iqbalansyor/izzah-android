package com.iqbalansyori.izzah.presentation.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.IzzahSwitch
import com.iqbalansyori.izzah.designsystem.component.PinDots
import com.iqbalansyori.izzah.designsystem.component.PinPad
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@Composable
fun CreatePinScreen(state: OnboardingState, onAction: (OnboardingAction) -> Unit, onBack: () -> Unit) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state.pinMismatch) {
        if (state.pinMismatch) {
            repeat(3) {
                shake.animateTo(8f, tween(50))
                shake.animateTo(-8f, tween(50))
            }
            shake.animateTo(0f, tween(30))
        }
    }
    Column(Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().navigationBarsPadding()) {
        OnboardingHeader(5, onBack)
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(if (state.pinFirst != null) R.string.pin_confirm_title else R.string.pin_create_title),
                    style = IzzahType.outfit(28.sp, FontWeight.Bold, tracking = -.02f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(if (state.pinMismatch) R.string.pin_mismatch else R.string.pin_create_subtitle),
                    Modifier.widthIn(max = 280.dp),
                    style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.45f),
                    color = IzzahColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            Row(Modifier.offset(x = shake.value.dp)) { PinDots(state.pinEntry.length, error = state.pinMismatch) }
            Spacer(Modifier.weight(1f))
            PinPad({ onAction(OnboardingAction.PinDigit(it)) }, { onAction(OnboardingAction.PinDelete) })
            if (state.biometricAvailable) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IzzahSwitch(state.biometric) { onAction(OnboardingAction.SetBiometric(it)) }
                    Text(stringResource(R.string.pin_biometric_toggle), style = IzzahType.outfit(14.sp, FontWeight.SemiBold))
                }
            }
        }
    }
}
