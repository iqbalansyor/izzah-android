package com.iqbalansyori.izzah.presentation.sholat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iqbalansyori.izzah.data.prayer.LocationProvider
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SholatRoot(onBack: () -> Unit, vm: SholatViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val provider = koinInject<LocationProvider>()
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        scope.launch {
            val found = if (granted) provider.current() else null
            // no permission or no fix: fall back to choosing a city
            if (found != null) vm.onAction(SholatAction.LocationFound(found)) else vm.onAction(SholatAction.PickCity)
        }
    }
    SholatScreen(state, vm::onAction, onBack, onUseLocation = { launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) })
}
