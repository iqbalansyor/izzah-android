package com.iqbalansyori.izzah.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.logic.ReminderDefaults
import org.koin.compose.koinInject

@Composable
fun ReminderToggles(
    prayer: Boolean,
    dzikir: Boolean,
    checkin: Boolean,
    onPrayer: (Boolean) -> Unit,
    onDzikir: (Boolean) -> Unit,
    onCheckin: (Boolean) -> Unit,
) {
    val formats = koinInject<LocaleFormats>()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReminderRow(stringResource(R.string.notif_opt_prayer), stringResource(R.string.notif_opt_prayer_desc), prayer, onPrayer)
        ReminderRow(stringResource(R.string.notif_opt_dzikir), stringResource(R.string.notif_opt_dzikir_desc), dzikir, onDzikir)
        ReminderRow(
            stringResource(R.string.notif_opt_checkin),
            stringResource(R.string.notif_opt_checkin_desc, formats.minuteOfDay(ReminderDefaults.CHECKIN_MINUTE)),
            checkin,
            onCheckin,
        )
    }
}

@Composable
private fun ReminderRow(title: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(IzzahColors.Card).padding(horizontal = 16.dp, vertical = 14.dp),
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
