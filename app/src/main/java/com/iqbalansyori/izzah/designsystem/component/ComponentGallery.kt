package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahTheme
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ComponentGallery() {
    var sw by remember { mutableStateOf(true) }
    var target by remember { mutableStateOf(40) }
    var sheet by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BackButton({})
            Column(Modifier.weight(1f)) { SegmentedProgress(total = 6, done = 3) }
            Text("3/6", style = IzzahType.outfit(13.sp, FontWeight.SemiBold), color = IzzahColors.Meta)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IzzahChip("Karena Allah", true, {})
            IzzahChip("Keluarga", false, {})
            IzzahChip("Fokus", false, {})
        }
        listOf(7 to "Satu minggu", 40 to "Empat puluh hari").forEach { (n, t) ->
            RadioCard(target == n, { target = n }) {
                Text("$n", Modifier.width(52.dp), style = IzzahType.outfit(30.sp, FontWeight.ExtraBold, tracking = -.03f))
                Column(Modifier.weight(1f)) {
                    Text(t, style = IzzahType.outfit(16.sp, FontWeight.SemiBold))
                    Text("Langkah pertama", style = IzzahType.outfit(13.sp, FontWeight.Normal), color = IzzahColors.Meta)
                }
            }
        }
        PrimaryButton("Lanjut", {})
        PrimaryButton("Lanjut (nonaktif)", {}, enabled = false)
        GradientButton("Mulai", {})
        GhostButton("Batal", {})
        OutlineButton("Ya, hapus semuanya", {}, IzzahColors.Destructive)
        GroupList("Privasi") {
            GroupRow("Kunci dengan PIN", trailing = { IzzahSwitch(sw) { sw = it } })
            GroupRow("Ikon & nama samaran", value = "Izzah", chevron = true, onClick = { sheet = true })
            GroupRow("Tentang Izzah", value = "v1.0", divider = false)
        }
        TopToast("Tersimpan")
    }
    if (sheet) IzzahSheet({ sheet = false }) {
        Text("Hari ke-13", style = IzzahType.outfit(24.sp, FontWeight.Bold))
        PrimaryButton("Tutup", { sheet = false })
    }
}

@Preview(widthDp = 390, heightDp = 1600, backgroundColor = 0xFF0E0E11, showBackground = true)
@Composable
private fun ComponentGalleryPreview() = IzzahTheme { ComponentGallery() }
