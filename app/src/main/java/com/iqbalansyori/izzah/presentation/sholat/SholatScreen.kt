package com.iqbalansyori.izzah.presentation.sholat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.data.locale.LocaleFormats
import com.iqbalansyori.izzah.designsystem.component.BackButton
import com.iqbalansyori.izzah.designsystem.component.IzzahChip
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.presentation.common.hijriLabel
import com.iqbalansyori.izzah.presentation.common.label
import org.koin.compose.koinInject

@Composable
fun SholatScreen(state: SholatState, onAction: (SholatAction) -> Unit, onBack: () -> Unit, onUseLocation: () -> Unit) {
    val formats = koinInject<LocaleFormats>()
    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BackButton(onBack)
            Text(stringResource(R.string.sholat_title), style = IzzahType.outfit(22.sp, FontWeight.Bold))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${state.done.size}", style = IzzahType.outfit(48.sp, FontWeight.ExtraBold, tracking = -.04f, lineHeight = 1f))
                Text(" " + stringResource(R.string.sholat_of_five), Modifier.padding(bottom = 6.dp), style = IzzahType.outfit(22.sp, FontWeight.SemiBold), color = IzzahColors.Meta)
            }
            val today = state.today
            if (state.city != null && today != null) {
                Text(
                    stringResource(R.string.sholat_location, state.city, hijriLabel(today)),
                    style = IzzahType.outfit(14.sp, FontWeight.Normal),
                    color = IzzahColors.TextSecondary,
                )
            }
        }
        if (state.city == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IzzahChip(stringResource(R.string.sholat_use_location), false, onUseLocation)
                IzzahChip(stringResource(R.string.sholat_location_pick), false, { onAction(SholatAction.PickCity) })
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Prayer.entries.forEach { p ->
                val done = p in state.done
                Row(
                    Modifier.fillMaxWidth().clip(IzzahShapes.Button).background(IzzahColors.Card)
                        .border(1.dp, if (done) IzzahColors.Lavender.copy(alpha = .5f) else IzzahColors.Border, IzzahShapes.Button)
                        .clickable(role = Role.Checkbox) { onAction(SholatAction.Toggle(p)) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(IzzahGlyphs.Star, null, Modifier.size(26.dp), tint = if (done) IzzahColors.Star else IzzahColors.Text.copy(alpha = .4f))
                    Text(stringResource(p.label), Modifier.weight(1f), style = IzzahType.outfit(17.sp, FontWeight.SemiBold))
                    Text(state.times?.get(p)?.let { formats.time(it) } ?: "–", style = IzzahType.outfit(15.sp, FontWeight.Normal), color = IzzahColors.Meta)
                }
            }
        }
        Text(
            stringResource(R.string.sholat_footer),
            style = IzzahType.outfit(13.sp, FontWeight.Normal, lineHeight = 1.45f),
            color = IzzahColors.Footnote,
        )
    }
    if (state.showCityPicker) {
        CityPickerSheet(state.cities, { onAction(SholatAction.CityChosen(it)) }, { onAction(SholatAction.DismissPicker) })
    }
}
