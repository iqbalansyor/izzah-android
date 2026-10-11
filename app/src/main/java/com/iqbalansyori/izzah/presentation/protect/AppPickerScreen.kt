package com.iqbalansyori.izzah.presentation.protect

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.BackButton
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.repository.InstalledApp
import com.iqbalansyori.izzah.domain.repository.SUGGESTED_PACKAGES

@Composable
fun AppPickerScreen(
    apps: List<InstalledApp>,
    initial: Set<String>,
    icon: (String) -> ImageBitmap?,
    onSave: (Set<String>) -> Unit,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selected by remember(initial) { mutableStateOf(initial) }
    val filtered = remember(apps, query) { apps.filter { it.label.contains(query.trim(), ignoreCase = true) } }
    val suggested = filtered.filter { it.packageName in SUGGESTED_PACKAGES }
    val rest = filtered.filter { it.packageName !in SUGGESTED_PACKAGES }

    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().navigationBarsPadding().padding(top = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BackButton(onBack)
            Text(stringResource(R.string.apppicker_title), style = IzzahType.outfit(22.sp, FontWeight.Bold))
        }
        Box(Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(IzzahShapes.R14).background(IzzahColors.Inner).padding(horizontal = 16.dp, vertical = 14.dp)) {
            if (query.isEmpty()) Text(stringResource(R.string.apppicker_search), style = IzzahType.outfit(16.sp, FontWeight.Normal), color = IzzahColors.Footnote)
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = IzzahType.outfit(16.sp, FontWeight.Normal).copy(color = IzzahColors.Text),
                cursorBrush = SolidColor(IzzahColors.Lavender),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        LazyColumn(Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)) {
            if (suggested.isNotEmpty()) {
                item("h-suggested") { Header(stringResource(R.string.apppicker_suggested)) }
                items(suggested, key = { "s-" + it.packageName }) { AppRow(it, icon(it.packageName), it.packageName in selected) { selected = toggle(selected, it.packageName) } }
            }
            // headers only exist with content: an empty first frame would otherwise anchor the list on "All apps" and push Suggested off screen
            if (rest.isNotEmpty()) item("h-all") { Header(stringResource(R.string.apppicker_all)) }
            items(rest, key = { it.packageName }) { AppRow(it, icon(it.packageName), it.packageName in selected) { selected = toggle(selected, it.packageName) } }
        }
        PrimaryButton(stringResource(R.string.common_save), { onSave(selected) }, Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp))
    }
}

private fun toggle(set: Set<String>, pkg: String) = if (pkg in set) set - pkg else set + pkg

@Composable
private fun Header(text: String) {
    Text(
        text.uppercase(),
        Modifier.padding(top = 14.dp, bottom = 6.dp),
        style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .12f),
        color = IzzahColors.Meta,
    )
}

@Composable
private fun AppRow(app: InstalledApp, icon: ImageBitmap?, checked: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth().clickable(role = Role.Checkbox, onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (icon != null) Image(icon, null, Modifier.size(36.dp)) else Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(IzzahColors.Inner))
        Text(app.label, Modifier.weight(1f), style = IzzahType.outfit(16.sp, FontWeight.Medium))
        Box(
            Modifier.size(24.dp).clip(CircleShape)
                .then(if (checked) Modifier.background(IzzahColors.Lavender) else Modifier.border(2.dp, IzzahColors.Text.copy(alpha = .35f), CircleShape)),
            contentAlignment = Alignment.Center,
        ) { if (checked) Icon(IzzahGlyphs.Check, null, Modifier.size(14.dp), tint = IzzahColors.Bg) }
    }
}
