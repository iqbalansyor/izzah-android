package com.iqbalansyori.izzah.presentation.shield

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.GhostButton
import com.iqbalansyori.izzah.designsystem.component.GradientButton
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.component.StarLogo
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.model.LockMode

// shared by the Protect preview and the overlay service, so it takes plain callbacks and no navigation
@Composable
fun ShieldContent(
    untilLabel: String,
    dayTime: Boolean,
    mode: LockMode,
    onJeda: () -> Unit,
    onClose: () -> Unit,
    onContinue: (() -> Unit)?,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(0f to IzzahColors.InsightA, .7f to IzzahColors.Bg))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 70.dp, bottom = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            StarLogo(64.dp, stroke = 2.5.dp)
            Text(
                stringResource(R.string.shield_label, untilLabel).uppercase(),
                style = IzzahType.outfit(12.5.sp, FontWeight.SemiBold, tracking = .18f),
                color = IzzahColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(if (dayTime) R.string.shield_title_day else R.string.shield_title),
                style = IzzahType.outfit(30.sp, FontWeight.Bold, tracking = -.02f, lineHeight = 1.12f),
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.shield_body),
                Modifier.widthIn(max = 290.dp),
                style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f),
                color = IzzahColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            GradientButton(stringResource(R.string.shield_take_jeda), onJeda)
            if (onContinue != null && mode == LockMode.GENTLE) GhostButton(stringResource(R.string.shield_continue), onContinue)
            PrimaryButton(stringResource(R.string.shield_close_app), onClose)
            Text(
                when (mode) {
                    LockMode.GENTLE -> stringResource(R.string.shield_note_gentle)
                    LockMode.PAUSE -> stringResource(R.string.shield_note_pause)
                    LockMode.FULL -> stringResource(R.string.shield_note_full, untilLabel)
                },
                Modifier.padding(top = 4.dp),
                style = IzzahType.outfit(13.sp, FontWeight.Normal),
                color = IzzahColors.Footnote,
                textAlign = TextAlign.Center,
            )
        }
    }
}
