package com.iqbalansyori.izzah.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import com.iqbalansyori.izzah.designsystem.component.StarLogo
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs

@Composable
fun IzzahTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = IzzahColors.Bg,
            surface = IzzahColors.Card,
            primary = IzzahColors.Lavender,
            onPrimary = IzzahColors.Bg,
            onBackground = IzzahColors.Text,
            onSurface = IzzahColors.Text,
        ),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides IzzahColors.Text,
            LocalTextStyle provides IzzahType.outfit(16.sp, FontWeight.Normal),
            content = content,
        )
    }
}

@Composable
fun ThemeSheetPreview() {
    val swatches = listOf(
        "Bg" to IzzahColors.Bg, "Card" to IzzahColors.Card, "Tile" to IzzahColors.Tile, "Selected" to IzzahColors.Selected,
        "Inner" to IzzahColors.Inner, "SwitchOff" to IzzahColors.SwitchOff, "Text" to IzzahColors.Text, "Lavender" to IzzahColors.Lavender,
        "Blue" to IzzahColors.Blue, "Star" to IzzahColors.Star, "StarB" to IzzahColors.StarB, "BarKept" to IzzahColors.BarKept,
        "BarToday" to IzzahColors.BarToday, "SplashCenter" to IzzahColors.SplashCenter, "InsightA" to IzzahColors.InsightA,
        "InsightB" to IzzahColors.InsightB, "Destructive" to IzzahColors.Destructive, "BarSlip" to IzzahColors.BarSlip,
    )
    val glyphs = with(IzzahGlyphs) { listOf(Home, Progress, Protect, Settings, Dzikir, Sholat, Jurnal, Doa, Star, Back, Close, Reset, Check) }
    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        StarLogo(96.dp)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Izzah", style = IzzahType.outfit(34.sp, FontWeight.Bold, tracking = -.02f))
            Text("عِزَّة", style = IzzahType.arabic(22.sp), color = IzzahColors.TextSecondary)
        }
        swatches.chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { (_, c) -> Row(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(c)) {} }
            }
        }
        glyphs.chunked(7).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEach { Icon(it, null, Modifier.size(28.dp), tint = IzzahColors.Star) }
            }
        }
        Text("Whoever desires honor…", style = IzzahType.outfit(16.sp, FontWeight.Medium), color = Color.Unspecified)
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF0E0E11)
@Composable
private fun ThemeSheetPreviewPreview() = IzzahTheme { ThemeSheetPreview() }
