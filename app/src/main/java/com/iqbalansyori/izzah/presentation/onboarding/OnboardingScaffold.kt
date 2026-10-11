package com.iqbalansyori.izzah.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.BackButton
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.component.SegmentedProgress
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@Composable
fun OnboardingHeader(step: Int, onBack: () -> Unit) {
    Row(
        Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BackButton(onBack)
        Column(Modifier.weight(1f)) { SegmentedProgress(total = 6, done = step) }
        Text(stringResource(R.string.onboarding_progress, step), style = IzzahType.outfit(13.sp, FontWeight.SemiBold), color = IzzahColors.Meta)
    }
}

@Composable
fun OnboardingScaffold(
    step: Int,
    onBack: () -> Unit,
    title: String,
    subtitle: String,
    cta: String?,
    onCta: () -> Unit,
    ctaEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().navigationBarsPadding()) {
        OnboardingHeader(step, onBack)
        Spacer(Modifier.height(26.dp))
        Column(Modifier.weight(1f).fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = if (cta == null) 40.dp else 0.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, style = IzzahType.outfit(32.sp, FontWeight.Bold, tracking = -.03f, lineHeight = 1.1f))
                Text(subtitle, style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f), color = IzzahColors.TextSecondary)
            }
            content()
        }
        if (cta != null) PrimaryButton(cta, onCta, Modifier.padding(start = 24.dp, end = 24.dp, bottom = 40.dp), enabled = ctaEnabled)
    }
}
