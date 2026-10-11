package com.iqbalansyori.izzah.presentation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.data.locale.LocaleFormats
import com.iqbalansyori.izzah.designsystem.component.ProgressGrid
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.logic.BarKind
import com.iqbalansyori.izzah.domain.logic.ReminderDefaults
import com.iqbalansyori.izzah.presentation.common.hijriLabel
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(state: ProgressState, onAction: (ProgressAction) -> Unit) {
    val formats = koinInject<LocaleFormats>()
    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Text(stringResource(R.string.progress_title), style = IzzahType.outfit(26.sp, FontWeight.Bold, tracking = -.02f))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${state.kept}", style = IzzahType.outfit(64.sp, FontWeight.ExtraBold, tracking = -.05f, lineHeight = 1f))
                Text(
                    stringResource(R.string.progress_of_target, state.target),
                    Modifier.padding(bottom = 8.dp),
                    style = IzzahType.outfit(20.sp, FontWeight.SemiBold),
                    color = IzzahColors.Meta,
                )
            }
            Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(IzzahColors.Selected)) {
                Box(
                    Modifier.fillMaxHeight().fillMaxWidth(state.percent / 100f).clip(RoundedCornerShape(4.dp))
                        .background(Brush.horizontalGradient(listOf(IzzahColors.Lavender, IzzahColors.Blue))),
                )
            }
            Text(
                if (state.slips > 0) pluralStringResource(R.plurals.progress_summary, state.slips, state.dayCount, state.slips)
                else stringResource(R.string.progress_summary_noslip, state.dayCount),
                style = IzzahType.outfit(14.sp, FontWeight.Normal),
                color = IzzahColors.TextSecondary,
            )
        }
        Column(
            Modifier.fillMaxWidth().clip(IzzahShapes.Card).background(IzzahColors.Card).border(1.dp, IzzahColors.Border, IzzahShapes.Card)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProgressGrid(state.grid, state.selectedIndex) { onAction(ProgressAction.TapCell(it)) }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Legend(IzzahColors.BarKept, 14, stringResource(R.string.day_status_kept))
                Legend(IzzahColors.BarSlip, 8, stringResource(R.string.day_status_slip))
                Legend(IzzahColors.BarToday, 14, stringResource(R.string.day_status_today))
                Legend(IzzahColors.BarFuture, 14, stringResource(R.string.progress_legend_future))
            }
            Text(
                stringResource(R.string.progress_bar_caption),
                style = IzzahType.outfit(12.5.sp, FontWeight.Normal, lineHeight = 1.45f),
                color = IzzahColors.Footnote,
            )
        }
        state.selected?.let { day ->
            Column(
                Modifier.fillMaxWidth().clip(IzzahShapes.Button).background(IzzahColors.Selected).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(R.string.progress_sel_title, day.number, formats.longDate(day.date)),
                        Modifier.weight(1f),
                        style = IzzahType.outfit(16.sp, FontWeight.Bold),
                    )
                    Text(
                        stringResource(
                            when (day.status) {
                                BarKind.SLIP -> R.string.day_status_slip
                                BarKind.TODAY -> R.string.day_status_today
                                else -> R.string.day_status_kept
                            },
                        ),
                        style = IzzahType.outfit(13.sp, FontWeight.Bold),
                        color = if (day.status == BarKind.SLIP) IzzahColors.TextSecondary else IzzahColors.BarKept,
                    )
                }
                Text(
                    stringResource(R.string.progress_sel_sub, hijriLabel(day.date), day.prayers.size),
                    style = IzzahType.outfit(13.sp, FontWeight.Normal),
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
                    style = IzzahType.outfit(14.sp, FontWeight.Normal, lineHeight = 1.45f),
                    color = IzzahColors.Text2,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat("${state.prayerPercent}%", stringResource(R.string.progress_stat_prayer), Modifier.weight(1f))
            Stat("${state.checkinDays}", stringResource(R.string.progress_stat_checkin), Modifier.weight(1f))
            Stat("${state.jedaCompleted}×", stringResource(R.string.progress_stat_jeda), Modifier.weight(1f))
        }
        state.insight?.let { window ->
            val remindAt = formats.minuteOfDay(ReminderDefaults.CHECKIN_MINUTE)
            Column(
                Modifier.fillMaxWidth().clip(IzzahShapes.Card)
                    .background(Brush.linearGradient(listOf(IzzahColors.InsightA, IzzahColors.InsightB)))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.progress_insight_title).uppercase(),
                    style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .12f),
                    color = IzzahColors.Text2,
                )
                Text(
                    stringResource(R.string.progress_insight_body, formats.minuteOfDay(window.startHour * 60), formats.minuteOfDay(window.endHour * 60)),
                    style = IzzahType.outfit(16.sp, FontWeight.Medium, lineHeight = 1.45f),
                )
                Box(
                    Modifier.height(42.dp).clip(IzzahShapes.R14)
                        .background(if (state.reminderOn) IzzahColors.Text.copy(alpha = .14f) else IzzahColors.Text)
                        .clickable(role = Role.Switch) { onAction(ProgressAction.ToggleReminder) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(if (state.reminderOn) R.string.progress_remind_on else R.string.progress_remind_cta, remindAt),
                        style = IzzahType.outfit(14.sp, FontWeight.Bold),
                        color = if (state.reminderOn) IzzahColors.Text else IzzahColors.Bg,
                    )
                }
            }
        }
    }
}

@Composable
private fun Legend(color: Color, height: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.width(8.dp).height(height.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Text(label, style = IzzahType.outfit(12.5.sp, FontWeight.Normal), color = IzzahColors.TextSecondary)
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
    Column(
        modifier.clip(IzzahShapes.Button).background(IzzahColors.Card).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(value, style = IzzahType.outfit(26.sp, FontWeight.ExtraBold))
        Text(label, style = IzzahType.outfit(12.5.sp, FontWeight.Normal, lineHeight = 1.3f), color = IzzahColors.Meta)
    }
}
