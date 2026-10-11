package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@Composable
fun PinDots(filled: Int, error: Boolean = false) {
    val line = if (error) IzzahColors.Destructive else IzzahColors.Text
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        repeat(4) { i ->
            Box(Modifier.size(16.dp).clip(CircleShape).border(2.dp, line, CircleShape).background(if (i < filled) line else androidx.compose.ui.graphics.Color.Transparent))
        }
    }
}

@Composable
private fun Key(onClick: () -> Unit, content: @Composable () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val bg by animateColorAsState(if (pressed) IzzahColors.Text.copy(alpha = .2f) else IzzahColors.Text.copy(alpha = .08f), label = "key")
    Box(
        Modifier.size(76.dp).clip(CircleShape).background(bg).clickable(interaction, null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
fun PinPad(onDigit: (Char) -> Unit, onDelete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        listOf("123", "456", "789").forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                row.forEach { d -> Key({ onDigit(d) }) { Text(d.toString(), style = IzzahType.outfit(30.sp, FontWeight.Medium)) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Box(Modifier.size(76.dp))
            Key({ onDigit('0') }) { Text("0", style = IzzahType.outfit(30.sp, FontWeight.Medium)) }
            Key(onDelete) { Icon(IzzahGlyphs.Close, stringResource(R.string.pin_delete_cd), Modifier.size(26.dp), tint = IzzahColors.Text) }
        }
    }
}
