package com.iqbalansyori.izzah.presentation.data

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iqbalansyori.izzah.presentation.common.ObserveAsEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun DataRoot(onBack: () -> Unit, vm: DataViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri == null) vm.discardPending() else scope.launch { vm.writePending { context.contentResolver.openOutputStream(uri) } }
    }
    ObserveAsEvents(vm.events) { if (it is DataEvent.CreateFile) launcher.launch(it.name) }
    LaunchedEffect(state.exportDone) {
        if (state.exportDone) {
            delay(1800)
            vm.onAction(DataAction.ToastShown)
        }
    }
    DataScreen(state, vm::onAction, onBack)
}
