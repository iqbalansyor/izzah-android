package com.iqbalansyori.izzah.presentation.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors

enum class Illustration { SUNRISE, HIKER, TREE }

// Gradient placeholders until the watercolor art arrives; swap the body for a painterResource
@Composable
fun IllustrationCard(kind: Illustration, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        when (kind) {
            Illustration.SUNRISE -> {
                drawRect(Brush.verticalGradient(listOf(IzzahColors.InsightA, IzzahColors.Lavender)))
                drawCircle(IzzahColors.Blue, radius = w * .22f, center = Offset(w * .5f, h * .62f))
            }
            Illustration.HIKER -> {
                drawRect(Brush.verticalGradient(listOf(IzzahColors.Bg, IzzahColors.Blue)))
                val mountain = Path().apply {
                    moveTo(0f, h)
                    lineTo(w * .55f, h * .38f)
                    lineTo(w, h)
                    close()
                }
                drawPath(mountain, IzzahColors.InsightA)
            }
            Illustration.TREE -> {
                drawRect(Brush.verticalGradient(listOf(IzzahColors.Card, IzzahColors.Lavender)))
                drawRect(IzzahColors.InsightA, Offset(w * .46f, h * .55f), Size(w * .08f, h * .3f))
                drawCircle(IzzahColors.BarKept, radius = w * .26f, center = Offset(w * .5f, h * .45f))
            }
        }
    }
}
