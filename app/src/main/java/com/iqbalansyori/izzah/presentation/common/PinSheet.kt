package com.iqbalansyori.izzah.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.IzzahSheet
import com.iqbalansyori.izzah.designsystem.component.PinDots
import com.iqbalansyori.izzah.designsystem.component.PinPad
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@Composable
fun PinSheet(entry: String, errorText: String?, onDigit: (Char) -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit) {
    IzzahSheet(onDismiss) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(stringResource(R.string.unlock_title), style = IzzahType.outfit(22.sp, FontWeight.Bold), textAlign = TextAlign.Center)
            PinDots(entry.length, error = errorText != null)
            Text(
                errorText.orEmpty(),
                style = IzzahType.outfit(14.sp, FontWeight.Medium),
                color = IzzahColors.Destructive,
                textAlign = TextAlign.Center,
            )
            PinPad(onDigit, onDelete)
        }
    }
}
