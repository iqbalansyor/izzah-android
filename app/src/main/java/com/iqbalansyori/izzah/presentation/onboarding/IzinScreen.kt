package com.iqbalansyori.izzah.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.GhostButton
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.presentation.common.PermissionRows

@Composable
fun IzinScreen(state: OnboardingState, onGrant: () -> Unit, onSkip: () -> Unit, onBack: () -> Unit) {
    OnboardingScaffold(
        step = 4,
        onBack = onBack,
        title = stringResource(R.string.izin_title),
        subtitle = stringResource(R.string.izin_subtitle),
        cta = null,
        onCta = {},
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf(R.string.izin_promise_1, R.string.izin_promise_2, R.string.izin_promise_3).forEachIndexed { i, res ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(24.dp).clip(IzzahShapes.R12).background(IzzahColors.Selected), contentAlignment = Alignment.Center) {
                        Text("${i + 1}", style = IzzahType.outfit(12.sp, FontWeight.ExtraBold))
                    }
                    Text(stringResource(res), style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.45f))
                }
            }
        }
        PermissionRows(state.usageGranted, state.overlayGranted, onGrant)
        Spacer(Modifier.weight(1f))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton(stringResource(R.string.izin_cta), onGrant)
            GhostButton(stringResource(R.string.izin_skip), onSkip, color = IzzahColors.TextSecondary)
        }
    }
}
