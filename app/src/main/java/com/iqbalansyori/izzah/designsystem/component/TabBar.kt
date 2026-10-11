package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

enum class TabDest { HOME, PROGRESS, PROTECT, SETTINGS }

private fun TabDest.icon(): ImageVector = when (this) {
    TabDest.HOME -> IzzahGlyphs.Home
    TabDest.PROGRESS -> IzzahGlyphs.Progress
    TabDest.PROTECT -> IzzahGlyphs.Protect
    TabDest.SETTINGS -> IzzahGlyphs.Settings
}

private fun TabDest.label(): Int = when (this) {
    TabDest.HOME -> R.string.tab_home
    TabDest.PROGRESS -> R.string.tab_progress
    TabDest.PROTECT -> R.string.tab_protect
    TabDest.SETTINGS -> R.string.tab_settings
}

@Composable
private fun RowScope.Tab(dest: TabDest, current: TabDest, onSelect: (TabDest) -> Unit) {
    val color = if (dest == current) IzzahColors.Text else IzzahColors.Footnote
    Column(
        Modifier.weight(1f).clickable(role = Role.Tab) { onSelect(dest) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(dest.icon(), null, Modifier.height(22.dp).size(22.dp), tint = color)
        Text(stringResource(dest.label()), style = IzzahType.outfit(11.sp, FontWeight.SemiBold), color = color)
    }
}

@Composable
fun IzzahTabBar(current: TabDest, onSelect: (TabDest) -> Unit, onJeda: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(Color(0xF0121216))
            .drawBehind { drawLine(IzzahColors.Text.copy(alpha = .07f), Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
            .padding(start = 4.dp, end = 4.dp, top = 10.dp),
    ) {
        Tab(TabDest.HOME, current, onSelect)
        Tab(TabDest.PROGRESS, current, onSelect)
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(
                Modifier
                    .offset(y = (-30).dp)
                    .size(62.dp)
                    .pressScale(interaction, .95f)
                    .shadow(24.dp, CircleShape, ambientColor = IzzahColors.JedaShadow, spotColor = IzzahColors.JedaShadow)
                    .clip(CircleShape)
                    .background(IzzahColors.PrimaryGradient)
                    .border(4.dp, IzzahColors.Bg, CircleShape)
                    .clickable(interaction, null, role = Role.Button, onClick = onJeda),
                contentAlignment = Alignment.Center,
            ) { Icon(IzzahGlyphs.Star, null, Modifier.size(24.dp), tint = IzzahColors.Bg) }
            Text(stringResource(R.string.tab_jeda), Modifier.offset(y = (-30).dp), style = IzzahType.outfit(11.sp, FontWeight.Bold), color = IzzahColors.Text)
        }
        Tab(TabDest.PROTECT, current, onSelect)
        Tab(TabDest.SETTINGS, current, onSelect)
    }
}
