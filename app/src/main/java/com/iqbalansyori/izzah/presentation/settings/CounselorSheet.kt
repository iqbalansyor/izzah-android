package com.iqbalansyori.izzah.presentation.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.IzzahSheet
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@Composable
fun CounselorSheet(onDismiss: () -> Unit) {
    IzzahSheet(onDismiss) {
        Text(stringResource(R.string.settings_counselor), style = IzzahType.outfit(22.sp, FontWeight.Bold))
        Text(
            stringResource(R.string.counselor_placeholder),
            style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f),
            color = IzzahColors.TextSecondary,
        )
    }
}
