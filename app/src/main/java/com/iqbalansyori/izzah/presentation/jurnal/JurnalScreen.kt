package com.iqbalansyori.izzah.presentation.jurnal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.data.locale.LocaleFormats
import com.iqbalansyori.izzah.designsystem.component.BackButton
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.component.TopToast
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.model.Mood
import com.iqbalansyori.izzah.presentation.common.label
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
fun JurnalScreen(state: JurnalState, toastVisible: Boolean, onAction: (JurnalAction) -> Unit, onBack: () -> Unit) {
    val formats = koinInject<LocaleFormats>()
    val time = koinInject<TimeSource>()
    Box(Modifier.fillMaxSize().background(IzzahColors.Bg)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                BackButton(onBack)
                Text(stringResource(R.string.jurnal_title), style = IzzahType.outfit(22.sp, FontWeight.Bold))
            }
            Text(stringResource(R.string.jurnal_question), style = IzzahType.outfit(28.sp, FontWeight.Bold, tracking = -.02f, lineHeight = 1.15f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Mood.entries.forEach { m ->
                    val on = state.mood == m
                    Box(
                        Modifier.weight(1f).height(64.dp).clip(IzzahShapes.R16)
                            .background(if (on) IzzahColors.Selected else IzzahColors.Card)
                            .border(1.dp, if (on) IzzahColors.Lavender.copy(alpha = .6f) else IzzahColors.Border, IzzahShapes.R16)
                            .semantics { selected = on }
                            .clickable(role = Role.RadioButton) { onAction(JurnalAction.PickMood(m)) },
                        contentAlignment = Alignment.Center,
                    ) { Text(stringResource(m.label), style = IzzahType.outfit(14.sp, FontWeight.SemiBold)) }
                }
            }
            Box(
                Modifier.fillMaxWidth().heightIn(min = 150.dp).clip(IzzahShapes.Button).background(IzzahColors.Card)
                    .border(1.dp, IzzahColors.Border, IzzahShapes.Button).padding(16.dp),
            ) {
                if (state.note.isEmpty()) {
                    Text(stringResource(R.string.jurnal_placeholder), style = IzzahType.outfit(16.sp, FontWeight.Normal, lineHeight = 1.5f), color = IzzahColors.Footnote)
                }
                BasicTextField(
                    value = state.note,
                    onValueChange = { onAction(JurnalAction.Edit(it)) },
                    textStyle = IzzahType.outfit(16.sp, FontWeight.Normal, lineHeight = 1.5f).copy(color = IzzahColors.Text),
                    cursorBrush = SolidColor(IzzahColors.Lavender),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 118.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(IzzahColors.Lavender))
                Text(stringResource(R.string.jurnal_locked_note), style = IzzahType.outfit(13.sp, FontWeight.Normal), color = IzzahColors.Footnote)
            }
            PrimaryButton(stringResource(R.string.common_save), { onAction(JurnalAction.Save) }, enabled = state.canSave)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.jurnal_earlier).uppercase(),
                    style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .12f),
                    color = IzzahColors.Meta,
                )
                if (state.entries.isEmpty()) {
                    Text(stringResource(R.string.jurnal_empty), style = IzzahType.outfit(14.sp, FontWeight.Normal, lineHeight = 1.45f), color = IzzahColors.TextSecondary)
                }
                state.entries.forEach { e ->
                    val weekday = formats.weekday(e.createdAt.toLocalDateTime(time.zone).date)
                    Column(
                        Modifier.fillMaxWidth().clip(IzzahShapes.R16).background(IzzahColors.Card).padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            e.mood?.let { stringResource(R.string.jurnal_entry_meta, weekday, stringResource(it.label)) } ?: weekday,
                            style = IzzahType.outfit(13.sp, FontWeight.Normal),
                            color = IzzahColors.Meta,
                        )
                        if (e.note.isNotBlank()) Text(e.note, style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.45f))
                    }
                }
            }
        }
        TopToast(if (toastVisible) stringResource(R.string.jurnal_saved_toast) else null, Modifier.statusBarsPadding().padding(top = 8.dp))
    }
}
