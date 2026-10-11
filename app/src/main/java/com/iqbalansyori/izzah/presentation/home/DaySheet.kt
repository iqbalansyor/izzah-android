package com.iqbalansyori.izzah.presentation.home

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.data.locale.LocaleFormats
import com.iqbalansyori.izzah.designsystem.component.IzzahSheet
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.logic.BarKind
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.presentation.common.hijriLabel
import com.iqbalansyori.izzah.presentation.common.label
import org.koin.compose.koinInject

@Composable
fun DaySheet(day: DayUi, onDismiss: () -> Unit) {
    val formats = koinInject<LocaleFormats>()
    IzzahSheet(onDismiss) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.day_title, day.number).uppercase(),
                    style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .14f),
                    color = IzzahColors.Meta,
                )
                Text(formats.longDate(day.date), style = IzzahType.outfit(24.sp, FontWeight.Bold, tracking = -.02f))
                Text(hijriLabel(day.date), style = IzzahType.outfit(14.sp, FontWeight.Normal), color = IzzahColors.TextSecondary)
            }
            StatusBadge(day.status)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Prayer.entries.forEach { p ->
                val done = p in day.prayers
                Column(
                    Modifier.weight(1f).clip(IzzahShapes.R14).background(IzzahColors.Inner).padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Icon(IzzahGlyphs.Star, null, Modifier.size(18.dp), tint = if (done) IzzahColors.Star else IzzahColors.Text.copy(alpha = .3f))
                    Text(stringResource(p.label), style = IzzahType.outfit(11.5.sp, FontWeight.Normal), color = IzzahColors.Text2)
                }
            }
        }
        Column(
            Modifier.fillMaxWidth().clip(IzzahShapes.R16).background(IzzahColors.Inner).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                stringResource(if (day.checkin) R.string.day_checkin_yes else R.string.day_checkin_no).uppercase(),
                style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .08f),
                color = IzzahColors.Meta,
            )
            Text(
                day.note ?: stringResource(
                    when (day.status) {
                        BarKind.SLIP -> R.string.day_note_slip
                        BarKind.TODAY -> R.string.day_note_today
                        else -> R.string.day_note_empty
                    },
                ),
                style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.45f),
            )
        }
        PrimaryButton(stringResource(R.string.common_close), onDismiss)
    }
}

@Composable
private fun StatusBadge(status: BarKind) {
    val (bg, fg, label) = when (status) {
        BarKind.SLIP -> Triple(IzzahColors.Text.copy(alpha = .16f), IzzahColors.Text, R.string.day_status_slip)
        BarKind.TODAY -> Triple(IzzahColors.BarToday, IzzahColors.Bg, R.string.day_status_today)
        else -> Triple(IzzahColors.BarKept, IzzahColors.Bg, R.string.day_status_kept)
    }
    Box(Modifier.clip(IzzahShapes.R14).background(bg).padding(horizontal = 12.dp, vertical = 7.dp)) {
        Text(stringResource(label), style = IzzahType.outfit(13.sp, FontWeight.Bold), color = fg)
    }
}
