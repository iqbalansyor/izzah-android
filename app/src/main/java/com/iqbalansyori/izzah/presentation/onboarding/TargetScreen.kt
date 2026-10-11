package com.iqbalansyori.izzah.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.presentation.common.TargetOptions

@Composable
fun TargetScreen(state: OnboardingState, onAction: (OnboardingAction) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    OnboardingScaffold(
        step = 2,
        onBack = onBack,
        title = stringResource(R.string.target_title),
        subtitle = stringResource(R.string.target_subtitle),
        cta = stringResource(R.string.common_continue),
        onCta = onNext,
    ) {
        TargetOptions(state.target) { onAction(OnboardingAction.PickTarget(it)) }
    }
}
