package com.iqbalansyori.izzah.presentation.doa

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.BackButton
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

private data class Doa(val arabic: Int, val translit: Int, val meaning: Int, val size: Int, val lineHeight: Int)

private val DOAS = listOf(
    Doa(R.string.doa_1, R.string.doa_1_translit, R.string.doa_1_meaning, 26, 44),
    Doa(R.string.doa_2, R.string.doa_2_translit, R.string.doa_2_meaning, 24, 43),
    Doa(R.string.doa_3, R.string.doa_3_translit, R.string.doa_3_meaning, 26, 44),
)

@Composable
fun DoaScreen(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BackButton(onBack)
            Text(stringResource(R.string.doa_title), style = IzzahType.outfit(22.sp, FontWeight.Bold))
        }
        Text(
            stringResource(R.string.doa_subtitle),
            style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f),
            color = IzzahColors.TextSecondary,
        )
        DOAS.forEach { doa ->
            Column(
                Modifier.fillMaxWidth().clip(IzzahShapes.Card).background(IzzahColors.Card).border(1.dp, IzzahColors.Border, IzzahShapes.Card).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    stringResource(doa.arabic),
                    Modifier.fillMaxWidth(),
                    style = IzzahType.arabic(doa.size.sp).copy(lineHeight = doa.lineHeight.sp),
                    textAlign = TextAlign.Right,
                )
                Text(stringResource(doa.translit), style = IzzahType.outfit(15.sp, FontWeight.SemiBold, lineHeight = 1.4f))
                Text(
                    stringResource(doa.meaning),
                    style = IzzahType.outfit(14.sp, FontWeight.Normal, lineHeight = 1.5f),
                    color = IzzahColors.TextSecondary,
                )
            }
        }
    }
}
