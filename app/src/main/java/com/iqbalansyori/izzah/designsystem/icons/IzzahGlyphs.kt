package com.iqbalansyori.izzah.designsystem.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

object IzzahGlyphs {
    private fun glyph(name: String, vararg paths: String): ImageVector {
        val b = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        paths.forEach {
            b.addPath(
                pathData = addPathNodes(it),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        return b.build()
    }

    val Home = glyph("Home", "M12 3L21 12L12 21L3 12Z")
    val Progress = glyph("Progress", "M4 4h16v16H4z", "M9 16v-4M12 16V8M15 16v-6")
    val Protect = glyph("Protect", "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18z", "M12 7.5a4.5 4.5 0 1 0 0 9a4.5 4.5 0 1 0 0-9z")
    val Settings = glyph(
        "Settings",
        "M12 8.8a3.2 3.2 0 1 0 0 6.4a3.2 3.2 0 1 0 0-6.4z",
        "M12 5a7 7 0 1 0 0 14a7 7 0 1 0 0-14z",
        "M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M4.9 19.1l2.1-2.1M17 7l2.1-2.1",
    )
    val Dzikir = glyph("Dzikir", "M12 5L19 12L12 19L5 12Z")
    val Sholat = glyph("Sholat", "M20 14.5A8.5 8.5 0 1 1 9.5 4a7 7 0 0 0 10.5 10.5z")
    val Jurnal = glyph("Jurnal", "M4 20l1-4L16 5l3 3L8 19z", "M14 7l3 3")
    val Doa = glyph("Doa", "M4 14.5A8.5 8.5 0 1 0 14.5 4a7 7 0 0 1-10.5 10.5z")
    val Star = glyph("Star", "M6.5 6.5h11v11h-11z", "M12 2.5L21.5 12L12 21.5L2.5 12z")
    val Back = glyph("Back", "M19 12H5", "M11 6l-6 6l6 6")
    val Close = glyph("Close", "M6 6l12 12M18 6L6 18")
    val Reset = glyph("Reset", "M4 12a8 8 0 1 0 2.5-5.8", "M4 4v5h5")
    val Check = glyph("Check", "M5 12.5l4.5 4.5L19 7.5")
}
