package com.iqbalansyori.izzah.presentation.unlock

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.GhostButton
import com.iqbalansyori.izzah.designsystem.component.IzzahSheet
import com.iqbalansyori.izzah.designsystem.component.OutlineButton
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@Composable
fun ForgotPinSheet(onDismiss: () -> Unit, onRestore: () -> Unit, onDelete: () -> Unit) {
    IzzahSheet(onDismiss) {
        Text(stringResource(R.string.unlock_forgot), style = IzzahType.outfit(22.sp, FontWeight.Bold))
        Text(
            stringResource(R.string.unlock_forgot_body),
            style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f),
            color = IzzahColors.TextSecondary,
        )
        PrimaryButton(stringResource(R.string.data_import_cta), onRestore)
        OutlineButton(stringResource(R.string.delete_sheet_confirm), onDelete, IzzahColors.Destructive)
        GhostButton(stringResource(R.string.common_cancel), onDismiss)
    }
}
