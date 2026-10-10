package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors

@Composable
fun StarLogo(size: Dp, modifier: Modifier = Modifier, stroke: Dp = 3.dp) {
    Canvas(modifier.size(size)) {
        val inset = this.size.width * 16f / 96f
        val side = this.size.width - inset * 2
        val corner = CornerRadius(4.dp.toPx())
        val style = Stroke(stroke.toPx())
        drawRoundRect(IzzahColors.Star, Offset(inset, inset), Size(side, side), corner, style)
        rotate(45f) {
            drawRoundRect(IzzahColors.StarB, Offset(inset, inset), Size(side, side), corner, style)
        }
    }
}
