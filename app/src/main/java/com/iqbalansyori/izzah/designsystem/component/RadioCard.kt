package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes

@Composable
fun RadioCard(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val line = if (selected) IzzahColors.Lavender.copy(alpha = .6f) else IzzahColors.Border
    Row(
        modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clip(IzzahShapes.Button)
            .background(if (selected) IzzahColors.Selected else IzzahColors.Card)
            .border(1.dp, line, IzzahShapes.Button)
            .clickable(interaction, null, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        content()
        Box(
            Modifier.size(20.dp).border(2.dp, if (selected) IzzahColors.Lavender else IzzahColors.Text.copy(alpha = .35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(9.dp).clip(CircleShape).background(IzzahColors.Lavender))
        }
    }
}
