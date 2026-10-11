package com.iqbalansyori.izzah.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.GroupList
import com.iqbalansyori.izzah.designsystem.component.GroupRow
import com.iqbalansyori.izzah.designsystem.component.IzzahSwitch
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.presentation.common.label

data class SettingsNav(
    val onDisguise: () -> Unit,
    val onTarget: () -> Unit,
    val onReminders: () -> Unit,
    val onSchedule: () -> Unit,
    val onData: () -> Unit,
    val onCounselor: () -> Unit,
    val onLanguage: () -> Unit,
)

@Composable
fun SettingsScreen(state: SettingsUi, onAction: (SettingsAction) -> Unit, nav: SettingsNav) {
    val languageName = when (state.languageTag) {
        "in" -> stringResource(R.string.language_id)
        "en" -> stringResource(R.string.language_en)
        else -> stringResource(R.string.language_system)
    }
    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Text(stringResource(R.string.settings_title), style = IzzahType.outfit(26.sp, FontWeight.Bold, tracking = -.02f))
        Row(
            Modifier.fillMaxWidth().clip(IzzahShapes.Group).background(IzzahColors.Card).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(48.dp).border(2.dp, IzzahColors.Text, CircleShape), contentAlignment = Alignment.Center) {
                Text("I", style = IzzahType.outfit(18.sp, FontWeight.Bold))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.settings_profile_title), style = IzzahType.outfit(17.sp, FontWeight.SemiBold))
                Text(stringResource(R.string.settings_profile_sub), style = IzzahType.outfit(13.sp, FontWeight.Normal), color = IzzahColors.Meta)
            }
        }
        GroupList(stringResource(R.string.settings_group_privacy)) {
            GroupRow(
                stringResource(R.string.settings_pin_lock),
                trailing = { IzzahSwitch(state.biometric) { onAction(SettingsAction.SetBiometric(it)) } },
            )
            GroupRow(stringResource(R.string.settings_disguise), value = stringResource(state.disguise.label), chevron = true, onClick = nav.onDisguise)
            GroupRow(
                stringResource(R.string.settings_hide_recents),
                trailing = { IzzahSwitch(state.hideInRecents) { onAction(SettingsAction.SetHideInRecents(it)) } },
                divider = false,
            )
        }
        GroupList(stringResource(R.string.settings_group_journey)) {
            GroupRow(
                stringResource(R.string.settings_target),
                value = pluralStringResource(R.plurals.settings_target_value, state.target, state.target),
                chevron = true,
                onClick = nav.onTarget,
            )
            GroupRow(stringResource(R.string.settings_reminders), chevron = true, onClick = nav.onReminders)
            GroupRow(stringResource(R.string.settings_schedule), chevron = true, onClick = nav.onSchedule, divider = false)
        }
        GroupList(stringResource(R.string.settings_group_data)) {
            GroupRow(stringResource(R.string.settings_export_delete), chevron = true, onClick = nav.onData)
            GroupRow(stringResource(R.string.settings_counselor), chevron = true, onClick = nav.onCounselor)
            GroupRow(stringResource(R.string.settings_language), value = languageName, chevron = true, onClick = nav.onLanguage)
            GroupRow(stringResource(R.string.settings_about), value = "v${state.versionName}", divider = false)
        }
    }
}
