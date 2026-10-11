package com.iqbalansyori.izzah.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.data.locale.LocaleFormats
import com.iqbalansyori.izzah.designsystem.component.DayBars
import com.iqbalansyori.izzah.designsystem.component.StarLogo
import com.iqbalansyori.izzah.designsystem.component.pressScale
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.localNow
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.presentation.common.hijriLabel
import com.iqbalansyori.izzah.presentation.common.label
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalTime
import org.koin.compose.koinInject

data class HomeNav(
    val onDzikir: () -> Unit,
    val onSholat: () -> Unit,
    val onJurnal: () -> Unit,
    val onDoa: () -> Unit,
    val onProgress: () -> Unit,
    val onSettings: () -> Unit,
    val onMulaiLagi: () -> Unit,
)

@Composable
fun HomeScreen(state: HomeState, onAction: (HomeAction) -> Unit, nav: HomeNav) {
    val formats = koinInject<LocaleFormats>()
    val time = koinInject<TimeSource>()
    var clock by remember { mutableStateOf(time.localNow().time) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                clock = time.localNow().time
                delay(1000)
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(top = 28.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Header(state, formats, nav.onSettings)
        Hero(state.kept, formats.clock(clock))
        Shortcuts(nav)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DayBars(state.bars, state.selected, { onAction(HomeAction.TapBar(it)) }, Modifier.padding(start = 20.dp))
            Column(
                Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = nav.onProgress).padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(stringResource(R.string.home_bars_caption, state.kept, state.target), style = IzzahType.outfit(15.sp, FontWeight.SemiBold))
                Text(stringResource(R.string.home_bars_hint), style = IzzahType.outfit(13.5.sp, FontWeight.Normal), color = IzzahColors.Meta)
            }
        }
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PrayerCard(state, formats, Modifier.weight(1f), nav.onSholat)
            DzikirCard(state.morning, Modifier.weight(1f), nav.onDzikir)
        }
        Row(
            Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = nav.onMulaiLagi).padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.home_slip_prompt),
                Modifier.weight(1f),
                style = IzzahType.outfit(14.sp, FontWeight.Normal, lineHeight = 1.45f),
                color = IzzahColors.TextSecondary,
            )
            Text(stringResource(R.string.home_slip_cta), style = IzzahType.outfit(14.sp, FontWeight.Bold))
        }
    }
    state.sheetDay?.let { DaySheet(it) { onAction(HomeAction.CloseSheet) } }
}

@Composable
private fun Header(state: HomeState, formats: LocaleFormats, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StarLogo(24.dp, stroke = 2.dp)
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(stringResource(R.string.home_greeting), style = IzzahType.outfit(18.sp, FontWeight.Bold, lineHeight = 1.1f))
                state.today?.let {
                    Text(
                        stringResource(R.string.home_date_line, formats.weekday(it), hijriLabel(it)),
                        style = IzzahType.outfit(12.5.sp, FontWeight.Normal),
                        color = IzzahColors.Meta,
                    )
                }
            }
        }
        Box(
            Modifier.size(34.dp).border(2.dp, IzzahColors.Text, CircleShape).clip(CircleShape).clickable(role = Role.Button, onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) { Text("I", style = IzzahType.outfit(13.sp, FontWeight.Bold)) }
    }
}

@Composable
private fun Hero(kept: Int, clock: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.home_hero_label).uppercase(),
            style = IzzahType.outfit(12.5.sp, FontWeight.SemiBold, tracking = .18f),
            color = IzzahColors.TextSecondary,
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("$kept", style = IzzahType.outfit(112.sp, FontWeight.ExtraBold, tracking = -.06f, lineHeight = 1f))
            Text(pluralStringResource(R.plurals.home_hero_unit, kept), Modifier.padding(bottom = 18.dp), style = IzzahType.outfit(30.sp, FontWeight.Bold))
        }
        Row(
            Modifier.border(1.dp, IzzahColors.Text.copy(alpha = .16f), androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(IzzahColors.Blue))
            Text(stringResource(R.string.home_today_clock, clock), style = IzzahType.outfit(16.sp, FontWeight.SemiBold))
        }
    }
}

@Composable
private fun Shortcuts(nav: HomeNav) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Shortcut(IzzahGlyphs.Dzikir, R.string.home_shortcut_dzikir, IzzahColors.Lavender, Modifier.weight(1f), nav.onDzikir)
        Shortcut(IzzahGlyphs.Sholat, R.string.home_shortcut_sholat, IzzahColors.Blue, Modifier.weight(1f), nav.onSholat)
        Shortcut(IzzahGlyphs.Jurnal, R.string.home_shortcut_jurnal, IzzahColors.Lavender, Modifier.weight(1f), nav.onJurnal)
        Shortcut(IzzahGlyphs.Doa, R.string.home_shortcut_doa, IzzahColors.Blue, Modifier.weight(1f), nav.onDoa)
    }
}

@Composable
private fun Shortcut(icon: ImageVector, label: Int, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Column(
        modifier.pressScale(interaction, .96f).clickable(interaction, null, role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier.size(60.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp)).background(IzzahColors.Tile)
                .border(1.dp, IzzahColors.Border, androidx.compose.foundation.shape.RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, Modifier.size(24.dp), tint = tint) }
        Text(
            stringResource(label).uppercase(),
            style = IzzahType.outfit(11.sp, FontWeight.SemiBold, tracking = .08f),
            color = IzzahColors.Text2,
        )
    }
}

@Composable
private fun CardBase(modifier: Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier.heightIn(min = 150.dp).clip(IzzahShapes.Card).background(IzzahColors.Card).border(1.dp, IzzahColors.Border, IzzahShapes.Card)
            .clickable(role = Role.Button, onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}

@Composable
private fun PrayerCard(state: HomeState, formats: LocaleFormats, modifier: Modifier, onClick: () -> Unit) {
    CardBase(modifier, onClick) {
        Text(
            stringResource(R.string.home_prayer_card, state.prayersToday.size).uppercase(),
            style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .08f),
            color = IzzahColors.Meta,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Prayer.entries.forEach { p ->
                Icon(
                    IzzahGlyphs.Star, null, Modifier.size(26.dp),
                    tint = if (p in state.prayersToday) IzzahColors.Star else IzzahColors.Text.copy(alpha = .4f),
                )
            }
        }
        val next = state.nextPrayer
        Text(
            if (next == null) {
                stringResource(R.string.home_prayers_complete)
            } else {
                val name = stringResource(next.first.label)
                next.second?.let { stringResource(R.string.home_next_prayer, name, formats.time(it)) } ?: name
            },
            style = IzzahType.outfit(17.sp, FontWeight.SemiBold, lineHeight = 1.2f),
        )
    }
}

@Composable
private fun DzikirCard(morning: Boolean, modifier: Modifier, onClick: () -> Unit) {
    CardBase(modifier, onClick) {
        Text(
            stringResource(R.string.home_dzikir_tag).uppercase(),
            style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .08f),
            color = IzzahColors.Meta,
        )
        Text(stringResource(R.string.home_dzikir_arabic), style = IzzahType.arabic(22.sp), color = IzzahColors.Lavender)
        Text(
            stringResource(if (morning) R.string.home_dzikir_card_morning else R.string.home_dzikir_card),
            style = IzzahType.outfit(17.sp, FontWeight.SemiBold, lineHeight = 1.2f),
        )
    }
}
