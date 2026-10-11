package com.iqbalansyori.izzah.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.BackButton
import com.iqbalansyori.izzah.designsystem.component.RadioCard
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.presentation.common.ReminderToggles
import com.iqbalansyori.izzah.presentation.common.TargetOptions

@Composable
private fun SubScreen(title: String, onBack: () -> Unit, subtitle: String? = null, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BackButton(onBack)
            Text(title, style = IzzahType.outfit(22.sp, FontWeight.Bold))
        }
        if (subtitle != null) {
            Text(subtitle, style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f), color = IzzahColors.TextSecondary)
        }
        content()
    }
}

@Composable
fun LanguageScreen(state: SettingsUi, onAction: (SettingsAction) -> Unit, onBack: () -> Unit) {
    SubScreen(stringResource(R.string.settings_language), onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf<Pair<String?, Int>>(null to R.string.language_system, "in" to R.string.language_id, "en" to R.string.language_en).forEach { (tag, label) ->
                RadioCard(state.languageTag == tag, { onAction(SettingsAction.SetLanguage(tag)) }) {
                    Text(stringResource(label), Modifier.weight(1f), style = IzzahType.outfit(16.sp, FontWeight.SemiBold))
                }
            }
        }
    }
}

@Composable
fun TargetEditScreen(state: SettingsUi, onAction: (SettingsAction) -> Unit, onBack: () -> Unit) {
    SubScreen(stringResource(R.string.target_title), onBack, stringResource(R.string.target_subtitle)) {
        TargetOptions(state.target) { onAction(SettingsAction.SetTarget(it)) }
    }
}

@Composable
fun RemindersScreen(state: SettingsUi, onAction: (SettingsAction) -> Unit, onBack: () -> Unit) {
    SubScreen(stringResource(R.string.settings_reminders), onBack, stringResource(R.string.notif_subtitle)) {
        ReminderToggles(
            prayer = state.notifPrayer, dzikir = state.notifDzikir, checkin = state.notifCheckin,
            onPrayer = { onAction(SettingsAction.SetNotif(prayer = it)) },
            onDzikir = { onAction(SettingsAction.SetNotif(dzikir = it)) },
            onCheckin = { onAction(SettingsAction.SetNotif(checkin = it)) },
        )
    }
}
