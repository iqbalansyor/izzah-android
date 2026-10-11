package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors

@Composable
fun ConicRing(progress: Float, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.size(240.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(240.dp)) {
            val w = 10.dp.toPx()
            val inset = Offset(w / 2, w / 2)
            val s = Size(size.width - w, size.height - w)
            drawArc(IzzahColors.Selected, 0f, 360f, false, inset, s, style = Stroke(w))
            drawArc(IzzahColors.Lavender, -90f, 360f * progress.coerceIn(0f, 1f), false, inset, s, style = Stroke(w))
        }
        content()
    }
}
