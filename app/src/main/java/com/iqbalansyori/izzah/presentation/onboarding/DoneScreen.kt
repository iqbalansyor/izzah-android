package com.iqbalansyori.izzah.presentation.onboarding

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.GradientButton
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.today
import com.iqbalansyori.izzah.presentation.common.hijriLabel
import org.koin.compose.koinInject

@Composable
fun DoneScreen(state: OnboardingState, onEnter: () -> Unit) {
    val today = koinInject<TimeSource>().today()
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(0f to IzzahColors.SplashCenter, .6f to IzzahColors.Bg, center = Offset.Unspecified))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 70.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(stringResource(R.string.done_bismillah), Modifier.padding(top = 20.dp), style = IzzahType.arabic(30.sp), color = IzzahColors.Text.copy(alpha = .85f))
        Text("1", style = IzzahType.outfit(150.sp, FontWeight.ExtraBold, tracking = -.06f, lineHeight = 1f))
        Text(
            stringResource(R.string.done_day_label, hijriLabel(today)).uppercase(),
            style = IzzahType.outfit(12.5.sp, FontWeight.SemiBold, tracking = .18f),
            color = IzzahColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.done_journey, state.target),
            Modifier.widthIn(max = 300.dp),
            style = IzzahType.outfit(26.sp, FontWeight.Bold, tracking = -.02f, lineHeight = 1.2f),
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.done_hint),
            Modifier.widthIn(max = 300.dp),
            style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f),
            color = IzzahColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        GradientButton(stringResource(R.string.done_cta), onEnter, Modifier.fillMaxWidth())
    }
}
