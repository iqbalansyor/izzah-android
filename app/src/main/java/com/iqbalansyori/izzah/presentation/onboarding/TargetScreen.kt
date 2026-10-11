package com.iqbalansyori.izzah.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.RadioCard
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

private data class TargetOption(val n: Int, val title: Int, val desc: Int)

private val OPTIONS = listOf(
    TargetOption(7, R.string.target_7_title, R.string.target_7_desc),
    TargetOption(30, R.string.target_30_title, R.string.target_30_desc),
    TargetOption(40, R.string.target_40_title, R.string.target_40_desc),
    TargetOption(90, R.string.target_90_title, R.string.target_90_desc),
)

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
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OPTIONS.forEach { o ->
                RadioCard(state.target == o.n, { onAction(OnboardingAction.PickTarget(o.n)) }) {
                    Text("${o.n}", Modifier.width(52.dp), style = IzzahType.outfit(30.sp, FontWeight.ExtraBold, tracking = -.03f))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(stringResource(o.title), style = IzzahType.outfit(16.sp, FontWeight.SemiBold))
                        Text(stringResource(o.desc), style = IzzahType.outfit(13.sp, FontWeight.Normal), color = IzzahColors.Meta)
                    }
                }
            }
        }
    }
}
