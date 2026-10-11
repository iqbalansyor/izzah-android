package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@Composable
private fun ButtonBase(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    height: Int,
    fg: Color,
    enabled: Boolean = true,
    background: Modifier = Modifier,
    weight: FontWeight = FontWeight.Bold,
    size: Int = 16,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .fillMaxWidth()
            .height(height.dp)
            .pressScale(interaction)
            .alpha(if (enabled) 1f else .4f)
            .clip(IzzahShapes.Button)
            .then(background)
            .clickable(interaction, null, enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = IzzahType.outfit(size.sp, weight), color = fg)
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    ButtonBase(text, onClick, modifier, 58, IzzahColors.Bg, enabled, Modifier.background(IzzahColors.Text))

@Composable
fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) =
    ButtonBase(text, onClick, modifier, 58, IzzahColors.Bg, background = Modifier.background(IzzahColors.PrimaryGradient))

@Composable
fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = IzzahColors.Text) =
    ButtonBase(text, onClick, modifier, 48, color, weight = FontWeight.SemiBold, size = 15, background = Modifier.background(IzzahColors.Divider))

@Composable
fun OutlineButton(text: String, onClick: () -> Unit, color: Color, modifier: Modifier = Modifier) =
    ButtonBase(
        text, onClick, modifier, 54, color, weight = FontWeight.SemiBold, size = 15,
        background = Modifier.border(BorderStroke(1.dp, color.copy(alpha = .5f)), IzzahShapes.Button),
    )
