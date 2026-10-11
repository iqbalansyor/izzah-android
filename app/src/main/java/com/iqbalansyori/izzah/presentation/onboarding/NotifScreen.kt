package com.iqbalansyori.izzah.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.presentation.common.ReminderToggles

@Composable
fun NotifScreen(state: OnboardingState, onAction: (OnboardingAction) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    OnboardingScaffold(
        step = 6,
        onBack = onBack,
        title = stringResource(R.string.notif_title),
        subtitle = stringResource(R.string.notif_subtitle),
        cta = stringResource(R.string.notif_cta),
        onCta = onNext,
    ) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCorner20).background(IzzahColors.Text.copy(alpha = .1f)).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(38.dp).clip(IzzahShapes.R12).background(IzzahColors.Bg), contentAlignment = Alignment.Center) {
                Icon(IzzahGlyphs.Star, null, Modifier.size(18.dp), tint = IzzahColors.Star)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.app_name), style = IzzahType.outfit(14.sp, FontWeight.SemiBold))
                    Text(stringResource(R.string.notif_preview_time), style = IzzahType.outfit(12.5.sp, FontWeight.Normal), color = IzzahColors.Meta)
                }
                Text(stringResource(R.string.notif_preview_body), style = IzzahType.outfit(14.sp, FontWeight.Normal), color = IzzahColors.Text2)
            }
        }
        ReminderToggles(
            prayer = state.notifPrayer, dzikir = state.notifDzikir, checkin = state.notifCheckin,
            onPrayer = { onAction(OnboardingAction.SetNotif(prayer = it)) },
            onDzikir = { onAction(OnboardingAction.SetNotif(dzikir = it)) },
            onCheckin = { onAction(OnboardingAction.SetNotif(checkin = it)) },
        )
    }
}

private val RoundedCorner20 = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
