package com.iqbalansyori.izzah.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object IzzahColors {
    val Bg = Color(0xFF0E0E11)
    val Card = Color(0xFF17171B)
    val Tile = Color(0xFF1A1A1F)
    val Selected = Color(0xFF1F1F24)
    val Inner = Color(0xFF24242A)
    val SwitchOff = Color(0xFF2A2A30)

    val Text = Color(0xFFF2F1EE)
    val Text2 = Color(0xFFC9C7C1)
    val TextSecondary = Color(0xFFB4B2AC)
    val Meta = Color(0xFF9E9C96)
    val Footnote = Color(0xFF8E8C86)

    val Border = Text.copy(alpha = .08f)
    val Divider = Text.copy(alpha = .06f)
    val ChipOutline = Text.copy(alpha = .2f)

    val Lavender = Color(0xFFC3BBFB)
    val Blue = Color(0xFF7ED2ED)
    val Star = Color(0xFFCCC7F9)
    val StarB = Color(0xFF9AD9EE)
    val BarKept = Color(0xFFBCB4F4)
    val BarToday = Color(0xFFB2E8FA)
    val TodayGlow = Color(0xFF6CCDEA).copy(alpha = .7f)
    val TagFg = Color(0xFFD6D2FD)
    val SplashCenter = Color(0xFF1B1C36)
    val InsightA = Color(0xFF242136)
    val InsightB = Color(0xFF07232F)
    val Destructive = Color(0xFFDF7F78)
    val JedaShadow = Color(0xFF5480C7).copy(alpha = .4f)
    val BarSlip = Text.copy(alpha = .28f)
    val BarFuture = Text.copy(alpha = .08f)

    val PrimaryGradient get() = Brush.linearGradient(listOf(Lavender, Blue))
}
