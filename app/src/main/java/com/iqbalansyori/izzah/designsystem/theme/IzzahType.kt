package com.iqbalansyori.izzah.designsystem.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R

@OptIn(ExperimentalTextApi::class)
object IzzahType {
    val Outfit = FontFamily(
        listOf(400, 500, 600, 700, 800).map { w ->
            Font(R.font.outfit, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))
        }
    )

    val Amiri = FontFamily(
        Font(R.font.amiri_regular, FontWeight.Normal),
        Font(R.font.amiri_bold, FontWeight.Bold),
    )

    fun outfit(size: TextUnit, weight: FontWeight, tracking: Float = 0f, lineHeight: Float? = null) = TextStyle(
        fontFamily = Outfit,
        fontSize = size,
        fontWeight = weight,
        letterSpacing = tracking.em,
        lineHeight = lineHeight?.let { (size.value * it).sp } ?: TextUnit.Unspecified,
    )

    fun arabic(size: TextUnit, bold: Boolean = false) = TextStyle(
        fontFamily = Amiri,
        fontSize = size,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textDirection = TextDirection.Rtl,
        lineHeight = (size.value * 1.6f).sp,
    )

    val label = outfit(11.sp, FontWeight.SemiBold, tracking = .12f)
    val kicker = outfit(12.5.sp, FontWeight.SemiBold, tracking = .18f)
}
