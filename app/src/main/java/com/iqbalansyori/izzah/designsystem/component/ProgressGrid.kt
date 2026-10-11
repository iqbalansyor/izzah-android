package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.iqbalansyori.izzah.domain.logic.Bar
import com.iqbalansyori.izzah.domain.logic.BarKind

@Composable
fun ProgressGrid(bars: List<Bar>, selectedIndex: Int?, onTap: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        bars.chunked(15).forEachIndexed { row, cells ->
            Row(Modifier.fillMaxWidth()) {
                cells.forEachIndexed { col, bar ->
                    val i = row * 15 + col
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .then(if (bar.kind != BarKind.FUTURE) Modifier.clickable(role = Role.Button) { onTap(i) } else Modifier),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(Modifier.width(14.dp).height(bar.heightDp.dp).barPaint(bar.kind, selectedIndex == i, 14.dp, bar.heightDp.dp))
                    }
                }
            }
        }
    }
}
