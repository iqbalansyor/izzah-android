package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors

@Composable
fun IzzahSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val track by animateColorAsState(if (checked) IzzahColors.Lavender else IzzahColors.SwitchOff, tween(200), label = "track")
    val knob by animateDpAsState(if (checked) 23.dp else 3.dp, tween(200), label = "knob")
    Box(
        Modifier
            .size(48.dp, 28.dp)
            .clip(CircleShape)
            .background(track)
            .toggleable(checked, role = Role.Switch, onValueChange = onCheckedChange),
    ) {
        Box(Modifier.offset(knob, 3.dp).size(22.dp).clip(CircleShape).background(IzzahColors.Text))
    }
}
