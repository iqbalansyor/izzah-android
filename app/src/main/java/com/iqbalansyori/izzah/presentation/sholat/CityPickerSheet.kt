package com.iqbalansyori.izzah.presentation.sholat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.IzzahSheet
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.model.GeoPoint

@Composable
fun CityPickerSheet(cities: List<GeoPoint>, onChosen: (GeoPoint) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val shown = remember(query, cities) { cities.filter { it.label.contains(query.trim(), ignoreCase = true) } }
    IzzahSheet(onDismiss) {
        Text(stringResource(R.string.sholat_location_pick), style = IzzahType.outfit(22.sp, FontWeight.Bold))
        Box(Modifier.fillMaxWidth().clip(IzzahShapes.R14).background(IzzahColors.Inner).padding(horizontal = 16.dp, vertical = 14.dp)) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = IzzahType.outfit(16.sp, FontWeight.Normal).copy(color = IzzahColors.Text),
                cursorBrush = SolidColor(IzzahColors.Lavender),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        LazyColumn(Modifier.heightIn(max = 380.dp)) {
            items(shown, key = { it.label }) { city ->
                Text(
                    city.label,
                    Modifier.fillMaxWidth().clickable(role = Role.Button) { onChosen(city) }.padding(vertical = 14.dp),
                    style = IzzahType.outfit(16.sp, FontWeight.Medium),
                )
            }
        }
    }
}
