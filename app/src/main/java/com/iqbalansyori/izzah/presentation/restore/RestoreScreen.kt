package com.iqbalansyori.izzah.presentation.restore

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.data.security.BackupError
import com.iqbalansyori.izzah.designsystem.component.BackButton
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.presentation.common.ObserveAsEvents
import com.iqbalansyori.izzah.presentation.common.PinSheet
import org.koin.androidx.compose.koinViewModel

@Composable
fun RestoreRoot(onBack: () -> Unit, onRestored: () -> Unit, vm: RestoreViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val bytes = uri?.let { runCatching { context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() } }.getOrNull() }
        if (bytes != null) vm.onFile(bytes)
    }
    ObserveAsEvents(vm.events) { if (it == RestoreEvent.Restored) onRestored() }

    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BackButton(onBack)
            Text(stringResource(R.string.data_import_cta), style = IzzahType.outfit(22.sp, FontWeight.Bold))
        }
        Text(stringResource(R.string.data_export_desc), style = IzzahType.outfit(15.sp, FontWeight.Normal, lineHeight = 1.5f), color = IzzahColors.TextSecondary)
        PrimaryButton(stringResource(R.string.data_import_cta), { picker.launch(arrayOf("*/*")) })
    }
    if (state.pinSheet) {
        PinSheet(
            entry = state.entry,
            errorText = when (state.error) {
                null -> null
                BackupError.WrongPin -> stringResource(R.string.data_import_wrong_pin)
                else -> stringResource(R.string.unlock_wrong)
            },
            onDigit = vm::digit,
            onDelete = vm::delete,
            onDismiss = vm::dismiss,
        )
    }
}
