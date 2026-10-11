package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IzzahSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = IzzahShapes.Sheet,
        containerColor = IzzahColors.Tile,
        scrimColor = Color.Black.copy(alpha = .55f),
        dragHandle = {
            Box(Modifier.padding(top = 14.dp).size(40.dp, 5.dp).clip(RoundedCornerShape(3.dp)).background(IzzahColors.Text.copy(alpha = .25f)))
        },
    ) {
        Column(
            Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 40.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start,
            content = content,
        )
    }
}
