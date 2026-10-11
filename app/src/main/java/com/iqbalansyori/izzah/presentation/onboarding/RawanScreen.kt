package com.iqbalansyori.izzah.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.IzzahChip
import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.presentation.common.label

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RawanScreen(state: OnboardingState, onAction: (OnboardingAction) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    OnboardingScaffold(
        step = 3,
        onBack = onBack,
        title = stringResource(R.string.rawan_title),
        subtitle = stringResource(R.string.rawan_subtitle),
        cta = stringResource(R.string.common_continue),
        onCta = onNext,
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Rawan.entries.forEach { r ->
                IzzahChip(stringResource(r.label), r in state.rawan, { onAction(OnboardingAction.ToggleRawan(r)) })
            }
        }
    }
}
