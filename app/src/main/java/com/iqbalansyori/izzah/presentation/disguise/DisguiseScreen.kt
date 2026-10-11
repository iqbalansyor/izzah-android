package com.iqbalansyori.izzah.presentation.disguise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.BackButton
import com.iqbalansyori.izzah.designsystem.component.StarLogo
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.model.Disguise
import com.iqbalansyori.izzah.presentation.common.label
import com.iqbalansyori.izzah.presentation.settings.SettingsAction
import com.iqbalansyori.izzah.presentation.settings.SettingsUi

@Composable
fun DisguiseScreen(state: SettingsUi, onAction: (SettingsAction) -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BackButton(onBack)
            Text(stringResource(R.string.disguise_title), style = IzzahType.outfit(22.sp, FontWeight.Bold))
        }
        Text(stringResource(R.string.disguise_desc), style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f), color = IzzahColors.TextSecondary)
        Disguise.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { d -> Tile(d, d == state.disguise, Modifier.weight(1f)) { onAction(SettingsAction.SetDisguise(d)) } }
            }
        }
        Text(stringResource(R.string.disguise_footer), style = IzzahType.outfit(13.sp, FontWeight.Normal, lineHeight = 1.45f), color = IzzahColors.Footnote)
    }
}

@Composable
private fun Tile(d: Disguise, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier.clip(shape).background(IzzahColors.Card)
            .border(1.5.dp, if (selected) IzzahColors.BarKept else IzzahColors.Border, shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val (bg, fg, glyph) = when (d) {
            Disguise.IZZAH -> Triple(IzzahColors.Bg, IzzahColors.Text, null)
            Disguise.STEPS -> Triple(colorResource(R.color.disguise_steps_bg), colorResource(R.color.disguise_steps_fg), "↗")
            Disguise.FOCUS -> Triple(colorResource(R.color.disguise_focus_bg), colorResource(R.color.disguise_focus_fg), "◐")
            Disguise.NOTES -> Triple(colorResource(R.color.disguise_notes_bg), colorResource(R.color.disguise_notes_fg), "≡")
        }
        Box(Modifier.size(68.dp).clip(RoundedCornerShape(17.dp)).background(bg), contentAlignment = Alignment.Center) {
            if (glyph == null) StarLogo(40.dp, stroke = 2.dp) else Text(glyph, style = IzzahType.outfit(28.sp, FontWeight.Bold), color = fg)
        }
        Text(stringResource(d.label), style = IzzahType.outfit(15.sp, FontWeight.SemiBold))
    }
}
