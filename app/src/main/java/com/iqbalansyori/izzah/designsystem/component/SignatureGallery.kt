package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahTheme
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.logic.DayMath
import com.iqbalansyori.izzah.domain.logic.DayStat
import com.iqbalansyori.izzah.domain.logic.Journey
import com.iqbalansyori.izzah.domain.model.Prayer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

// The prototype's 45-day model: slips on days 9/21/38, 3..5 prayers, check-in unless i % 5 == 0
private fun fakeJourney(): Journey {
    val start = LocalDate(2026, 8, 28)
    val days = (0 until 45).map { i ->
        val n = i + 1
        val count = 3 + (i * 7) % 3
        DayStat(start.plus(i, DateTimeUnit.DAY), n, count, Prayer.entries.take(count).toSet(), i % 5 != 0, n in setOf(9, 21, 38), i == 44)
    }
    return Journey(days, 90)
}

@Composable
fun SignatureGallery() {
    val j = remember { fakeJourney() }
    var sel by remember { mutableStateOf<Int?>(12) }
    var gridSel by remember { mutableStateOf<Int?>(20) }
    var tab by remember { mutableStateOf(TabDest.HOME) }
    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DayBars(DayMath.homeBars(j), sel, { sel = it })
        ProgressGrid(DayMath.gridBars(j), gridSel, { gridSel = it })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            PinDots(2)
            PinDots(4, error = true)
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { PinPad({}, {}) }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BreathingOrb(inhale = true)
            Text("Tarik napas", style = IzzahType.outfit(19.sp, FontWeight.Bold), color = IzzahColors.Bg)
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ConicRing(.62f) { Text("62%", style = IzzahType.outfit(44.sp, FontWeight.Bold)) }
        }
        Box(Modifier.padding(top = 30.dp)) { IzzahTabBar(tab, { tab = it }, {}) }
    }
}

@Preview(widthDp = 390, heightDp = 2600, backgroundColor = 0xFF0E0E11, showBackground = true)
@Composable
private fun SignatureGalleryPreview() = IzzahTheme { SignatureGallery() }
