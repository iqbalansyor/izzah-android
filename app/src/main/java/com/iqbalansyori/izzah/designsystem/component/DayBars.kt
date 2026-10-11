package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.domain.logic.Bar
import com.iqbalansyori.izzah.domain.logic.BarKind

fun BarKind.color(): Color = when (this) {
    BarKind.KEPT -> IzzahColors.BarKept
    BarKind.SLIP -> IzzahColors.BarSlip
    BarKind.TODAY -> IzzahColors.BarToday
    BarKind.FUTURE -> IzzahColors.BarFuture
}

internal fun Modifier.barPaint(kind: BarKind, selected: Boolean, width: Dp, height: Dp): Modifier = drawBehind {
    val r = CornerRadius(7.dp.toPx())
    if (kind == BarKind.TODAY) {
        drawIntoCanvas { c: Canvas ->
            val paint = Paint().apply {
                color = kind.color()
                asFrameworkPaint().setShadowLayer(20.dp.toPx(), 0f, 0f, IzzahColors.TodayGlow.toArgb())
            }
            c.drawRoundRect(0f, 0f, size.width, size.height, r.x, r.y, paint)
        }
    }
    if (selected) {
        drawRoundRect(IzzahColors.Bg, Offset(-1.dp.toPx(), -1.dp.toPx()), Size(size.width + 2.dp.toPx(), size.height + 2.dp.toPx()), CornerRadius(r.x + 1.dp.toPx()), Stroke(2.dp.toPx()))
        drawRoundRect(IzzahColors.Text, Offset(-3.dp.toPx(), -3.dp.toPx()), Size(size.width + 6.dp.toPx(), size.height + 6.dp.toPx()), CornerRadius(r.x + 3.dp.toPx()), Stroke(2.dp.toPx()))
    }
    drawRoundRect(kind.color(), size = size, cornerRadius = r)
}

@Composable
fun DayBars(bars: List<Bar>, selectedIndex: Int?, onTap: (Int) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(96.dp)
            .clipToBounds()
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                drawRect(Brush.horizontalGradient(0.62f to Color.Black, 1f to Color.Transparent), blendMode = BlendMode.DstIn)
            },
    ) {
        Row(Modifier.wrapContentWidth(Alignment.Start, unbounded = true), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            bars.forEachIndexed { i, bar ->
                val clickable = bar.kind != BarKind.FUTURE
                Box(
                    Modifier
                        .width(14.dp)
                        .height(96.dp)
                        .then(if (clickable) Modifier.clickable(role = Role.Button) { onTap(i) } else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.width(14.dp).height(bar.heightDp.dp).barPaint(bar.kind, selectedIndex == i, 14.dp, bar.heightDp.dp))
                    if (bar.kind == BarKind.TODAY) {
                        Icon(IzzahGlyphs.Star, null, Modifier.size(10.dp).offset(y = -(bar.heightDp / 2 - 9).dp), tint = IzzahColors.Bg)
                    }
                }
            }
        }
    }
}
