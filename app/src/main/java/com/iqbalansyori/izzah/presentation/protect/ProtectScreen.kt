package com.iqbalansyori.izzah.presentation.protect

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.data.locale.LocaleFormats
import com.iqbalansyori.izzah.designsystem.component.GhostButton
import com.iqbalansyori.izzah.designsystem.component.IzzahSwitch
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.component.RadioCard
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.model.LockMode
import com.iqbalansyori.izzah.domain.model.Slot
import com.iqbalansyori.izzah.presentation.common.PermissionRows
import com.iqbalansyori.izzah.presentation.common.description
import com.iqbalansyori.izzah.presentation.common.label
import org.koin.compose.koinInject

data class ProtectNav(
    val onAddApp: () -> Unit,
    val onPreview: () -> Unit,
    val onFixPermission: () -> Unit,
    val onKeepAutostart: () -> Unit,
    val onKeepBattery: () -> Unit,
    val onKeepDone: () -> Unit,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProtectScreen(state: ProtectState, icon: (String) -> ImageBitmap?, onAction: (ProtectAction) -> Unit, nav: ProtectNav) {
    val formats = koinInject<LocaleFormats>()
    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Text(stringResource(R.string.protect_title), style = IzzahType.outfit(26.sp, FontWeight.Bold, tracking = -.02f))
        Text(stringResource(R.string.protect_desc), style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f), color = IzzahColors.TextSecondary)
        if (!state.usageGranted || !state.overlayGranted) PermissionRows(state.usageGranted, state.overlayGranted, nav.onFixPermission)
        if (state.showKeepRunning) KeepRunningCard(nav)
        Section(stringResource(R.string.protect_schedule)) {
            state.slots.forEach { slot -> SlotCard(slot, formats, onAction) }
        }
        Section(pluralStringResource(R.plurals.protect_locked_apps, state.locked.size, state.locked.size)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.locked.forEach { app ->
                    AppChip(app.label, icon(app.packageName), selected = true) { onAction(ProtectAction.RemoveApp(app.packageName)) }
                }
                AppChip(stringResource(R.string.protect_add_app), null, selected = false, onClick = nav.onAddApp)
            }
        }
        Section(stringResource(R.string.protect_when_opened)) {
            LockMode.entries.forEach { m ->
                RadioCard(state.mode == m, { onAction(ProtectAction.SetMode(m)) }) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(stringResource(m.label), style = IzzahType.outfit(15.sp, FontWeight.SemiBold))
                        Text(stringResource(m.description), style = IzzahType.outfit(12.5.sp, FontWeight.Normal), color = IzzahColors.Meta)
                    }
                }
            }
        }
        GhostButton(stringResource(R.string.protect_preview_shield), nav.onPreview)
    }
    state.editing?.let { TimeRangeDialog(it, { s -> onAction(ProtectAction.SaveSlot(s)) }, { onAction(ProtectAction.DismissEdit) }) }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title.uppercase(), style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .12f), color = IzzahColors.Meta)
        content()
    }
}

@Composable
private fun SlotCard(slot: Slot, formats: LocaleFormats, onAction: (ProtectAction) -> Unit) {
    val shape = IzzahShapes.Button
    Row(
        Modifier.fillMaxWidth().clip(shape).background(IzzahColors.Card)
            .border(1.dp, if (slot.enabled) IzzahColors.Lavender.copy(alpha = .35f) else IzzahColors.Border, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.weight(1f).clickable(role = Role.Button) { onAction(ProtectAction.EditSlot(slot.id)) }, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${formats.minuteOfDay(slot.startMinute)} – ${formats.minuteOfDay(slot.endMinute)}",
                    style = IzzahType.outfit(17.sp, FontWeight.Bold),
                )
                val risky = slot.id == 1
                Text(
                    stringResource(if (risky) R.string.slot_tag_risky else R.string.slot_tag_optional).uppercase(),
                    Modifier.clip(RoundedCornerShape(7.dp))
                        .background(if (risky) IzzahColors.BarKept.copy(alpha = .18f) else IzzahColors.Text.copy(alpha = .08f))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    style = IzzahType.outfit(11.sp, FontWeight.Bold, tracking = .06f),
                    color = if (risky) IzzahColors.TagFg else IzzahColors.TextSecondary,
                )
            }
            Text(
                stringResource(if (slot.id == 1) R.string.slot_1_desc else R.string.slot_2_desc),
                style = IzzahType.outfit(13.sp, FontWeight.Normal),
                color = IzzahColors.Meta,
            )
        }
        IzzahSwitch(slot.enabled) { onAction(ProtectAction.ToggleSlot(slot.id)) }
    }
}

@Composable
private fun AppChip(label: String, icon: ImageBitmap?, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(19.dp)
    Row(
        Modifier.height(38.dp).clip(shape)
            .then(if (selected) Modifier.background(IzzahColors.Text) else Modifier.border(1.dp, IzzahColors.ChipOutline, shape))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icon != null) Image(icon, null, Modifier.size(20.dp))
        Text(label, style = IzzahType.outfit(14.sp, FontWeight.SemiBold), color = if (selected) IzzahColors.Bg else IzzahColors.Text)
        if (selected) Text("✓", style = IzzahType.outfit(14.sp, FontWeight.Bold), color = IzzahColors.Bg)
    }
}

@Composable
private fun KeepRunningCard(nav: ProtectNav) {
    Column(
        Modifier.fillMaxWidth().clip(IzzahShapes.Card).background(IzzahColors.Card).border(1.dp, IzzahColors.Border, IzzahShapes.Card).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(stringResource(R.string.keeprun_title), style = IzzahType.outfit(16.sp, FontWeight.SemiBold))
        Text(stringResource(R.string.keeprun_desc), style = IzzahType.outfit(14.sp, FontWeight.Normal, lineHeight = 1.5f), color = IzzahColors.TextSecondary)
        PrimaryButton(stringResource(R.string.keeprun_autostart), nav.onKeepAutostart)
        GhostButton(stringResource(R.string.keeprun_battery), nav.onKeepBattery)
        GhostButton(stringResource(R.string.keeprun_done), nav.onKeepDone, color = IzzahColors.TextSecondary)
    }
}
