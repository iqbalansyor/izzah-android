package com.iqbalansyori.izzah.presentation.protect

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.model.Slot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeDialog(slot: Slot, onSave: (Slot) -> Unit, onDismiss: () -> Unit) {
    val is24 = DateFormat.is24HourFormat(LocalContext.current)
    val start = rememberTimePickerState(slot.startMinute / 60, slot.startMinute % 60, is24)
    val end = rememberTimePickerState(slot.endMinute / 60, slot.endMinute % 60, is24)
    val colors = TimePickerDefaults.colors(
        timeSelectorSelectedContainerColor = IzzahColors.Selected,
        timeSelectorUnselectedContainerColor = IzzahColors.Inner,
        timeSelectorSelectedContentColor = IzzahColors.Text,
        timeSelectorUnselectedContentColor = IzzahColors.Text,
        periodSelectorSelectedContainerColor = IzzahColors.Lavender,
        periodSelectorUnselectedContainerColor = IzzahColors.Inner,
        periodSelectorSelectedContentColor = IzzahColors.Bg,
        periodSelectorUnselectedContentColor = IzzahColors.Text,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IzzahColors.Tile,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        text = {
            Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.slot_edit_from).uppercase(), style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .12f), color = IzzahColors.Meta)
                TimeInput(start, colors = colors)
                Text(stringResource(R.string.slot_edit_to).uppercase(), style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .12f), color = IzzahColors.Meta)
                TimeInput(end, colors = colors)
            }
        },
        confirmButton = {
            TextButton({ onSave(slot.copy(startMinute = start.hour * 60 + start.minute, endMinute = end.hour * 60 + end.minute)) }) {
                Text(stringResource(R.string.common_save), color = IzzahColors.Lavender, style = IzzahType.outfit(15.sp, FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(onDismiss) { Text(stringResource(R.string.common_cancel), color = IzzahColors.TextSecondary, style = IzzahType.outfit(15.sp, FontWeight.SemiBold)) }
        },
    )
}
