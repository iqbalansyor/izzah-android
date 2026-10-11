package com.iqbalansyori.izzah.presentation.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.BackButton
import com.iqbalansyori.izzah.designsystem.component.GhostButton
import com.iqbalansyori.izzah.designsystem.component.IzzahSheet
import com.iqbalansyori.izzah.designsystem.component.OutlineButton
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.component.TopToast
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.presentation.common.PinSheet

@Composable
fun DataScreen(state: DataState, onAction: (DataAction) -> Unit, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(IzzahColors.Bg)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                BackButton(onBack)
                Text(stringResource(R.string.data_title), style = IzzahType.outfit(22.sp, FontWeight.Bold))
            }
            Text(stringResource(R.string.data_desc), style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f), color = IzzahColors.TextSecondary)
            Card(stringResource(R.string.data_export_title), stringResource(R.string.data_export_desc)) {
                PrimaryButton(stringResource(R.string.data_export_cta), { onAction(DataAction.StartExport) })
            }
            Card(stringResource(R.string.data_delete_title), stringResource(R.string.data_delete_desc)) {
                OutlineButton(stringResource(R.string.data_delete_cta), { onAction(DataAction.AskDelete) }, IzzahColors.Destructive)
            }
        }
        TopToast(if (state.exportDone) stringResource(R.string.data_export_done) else null, Modifier.statusBarsPadding().padding(top = 8.dp))
    }
    if (state.pinSheet) {
        PinSheet(
            entry = state.entry,
            errorText = if (state.pinError) stringResource(R.string.unlock_wrong) else null,
            onDigit = { onAction(DataAction.Digit(it)) },
            onDelete = { onAction(DataAction.Delete) },
            onDismiss = { onAction(DataAction.DismissPin) },
        )
    }
    if (state.confirmDelete) {
        IzzahSheet({ onAction(DataAction.DismissDelete) }) {
            Text(stringResource(R.string.delete_sheet_title), style = IzzahType.outfit(22.sp, FontWeight.Bold))
            Text(stringResource(R.string.delete_sheet_body), style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f), color = IzzahColors.TextSecondary)
            OutlineButton(stringResource(R.string.delete_sheet_confirm), { onAction(DataAction.ConfirmDelete) }, IzzahColors.Destructive)
            GhostButton(stringResource(R.string.common_cancel), { onAction(DataAction.DismissDelete) })
        }
    }
}

@Composable
private fun Card(title: String, desc: String, action: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(IzzahShapes.Card).background(IzzahColors.Card).border(1.dp, IzzahColors.Border, IzzahShapes.Card).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = IzzahType.outfit(17.sp, FontWeight.SemiBold))
        Text(desc, style = IzzahType.outfit(14.sp, FontWeight.Normal, lineHeight = 1.5f), color = IzzahColors.TextSecondary)
        action()
    }
}
