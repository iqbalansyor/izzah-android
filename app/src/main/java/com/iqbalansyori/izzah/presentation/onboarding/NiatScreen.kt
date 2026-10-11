package com.iqbalansyori.izzah.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.iqbalansyori.izzah.designsystem.component.IzzahChip
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.model.Why
import com.iqbalansyori.izzah.presentation.common.label

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NiatScreen(state: OnboardingState, onAction: (OnboardingAction) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    OnboardingScaffold(
        step = 1,
        onBack = onBack,
        title = stringResource(R.string.niat_title),
        subtitle = stringResource(R.string.niat_subtitle),
        cta = stringResource(R.string.common_continue),
        onCta = onNext,
        ctaEnabled = state.why.isNotEmpty(),
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Why.entries.forEach { w ->
                IzzahChip(stringResource(w.label), w in state.why, { onAction(OnboardingAction.ToggleWhy(w)) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(IzzahColors.Lavender))
            Text(stringResource(R.string.niat_note), style = IzzahType.outfit(13.sp, FontWeight.Normal), color = IzzahColors.Footnote)
        }
    }
}
