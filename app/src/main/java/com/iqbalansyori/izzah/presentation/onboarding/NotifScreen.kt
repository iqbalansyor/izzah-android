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
import com.iqbalansyori.izzah.data.locale.LocaleFormats
import com.iqbalansyori.izzah.designsystem.component.IzzahSwitch
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.logic.ReminderDefaults
import org.koin.compose.koinInject

@Composable
fun NotifScreen(state: OnboardingState, onAction: (OnboardingAction) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    val formats = koinInject<LocaleFormats>()
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NotifRow(stringResource(R.string.notif_opt_prayer), stringResource(R.string.notif_opt_prayer_desc), state.notifPrayer) {
                onAction(OnboardingAction.SetNotif(prayer = it))
            }
            NotifRow(stringResource(R.string.notif_opt_dzikir), stringResource(R.string.notif_opt_dzikir_desc), state.notifDzikir) {
                onAction(OnboardingAction.SetNotif(dzikir = it))
            }
            NotifRow(
                stringResource(R.string.notif_opt_checkin),
                stringResource(R.string.notif_opt_checkin_desc, formats.minuteOfDay(ReminderDefaults.CHECKIN_MINUTE)),
                state.notifCheckin,
            ) { onAction(OnboardingAction.SetNotif(checkin = it)) }
        }
    }
}

private val RoundedCorner20 = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)

@Composable
private fun NotifRow(title: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp)).background(IzzahColors.Card)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = IzzahType.outfit(16.sp, FontWeight.SemiBold))
            Text(desc, style = IzzahType.outfit(13.sp, FontWeight.Normal), color = IzzahColors.Meta)
        }
        IzzahSwitch(checked, onChange)
    }
}
