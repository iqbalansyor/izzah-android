package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@Composable
fun GroupList(title: String?, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title != null) {
            Text(
                title.uppercase(),
                Modifier.padding(horizontal = 4.dp),
                style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .12f),
                color = IzzahColors.Meta,
            )
        }
        Column(Modifier.fillMaxWidth().clip(IzzahShapes.Group).background(IzzahColors.Card), content = content)
    }
}

@Composable
fun ColumnScope.GroupRow(
    title: String,
    value: String? = null,
    chevron: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    divider: Boolean = true,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, Modifier.weight(1f), style = IzzahType.outfit(16.sp, FontWeight.Medium))
        if (value != null || chevron) {
            Text(listOfNotNull(value, if (chevron) "›" else null).joinToString(" "), style = IzzahType.outfit(15.sp, FontWeight.Normal), color = IzzahColors.Meta)
        }
        trailing?.invoke()
    }
    if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(IzzahColors.Divider))
}
