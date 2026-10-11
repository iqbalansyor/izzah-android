package com.iqbalansyori.izzah.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@Composable
fun PermissionRows(usage: Boolean, overlay: Boolean, onFix: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PermissionRow(stringResource(R.string.izin_perm_usage_title), stringResource(R.string.izin_perm_usage_desc), usage, onFix)
        PermissionRow(stringResource(R.string.izin_perm_overlay_title), stringResource(R.string.izin_perm_overlay_desc), overlay, onFix)
    }
}

@Composable
private fun PermissionRow(title: String, desc: String, on: Boolean, onFix: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(IzzahShapes.R16)
            .background(IzzahColors.Card)
            .then(if (on) Modifier else Modifier.clickable(role = Role.Button, onClick = onFix))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = IzzahType.outfit(15.sp, FontWeight.SemiBold))
            Text(desc, style = IzzahType.outfit(13.sp, FontWeight.Normal), color = IzzahColors.Meta)
        }
        Text(
            stringResource(if (on) R.string.izin_perm_on else R.string.izin_perm_off),
            style = IzzahType.outfit(13.sp, FontWeight.SemiBold),
            color = if (on) IzzahColors.Lavender else IzzahColors.Meta,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
