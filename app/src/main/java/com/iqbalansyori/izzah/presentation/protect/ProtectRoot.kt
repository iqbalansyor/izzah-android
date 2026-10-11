package com.iqbalansyori.izzah.presentation.protect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iqbalansyori.izzah.data.blocking.KeepRunningGuide
import com.iqbalansyori.izzah.data.blocking.PermissionChecker
import com.iqbalansyori.izzah.domain.repository.BlockingController
import com.iqbalansyori.izzah.domain.repository.InstalledAppsRepository
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
private fun rememberIconLoader(): (String) -> ImageBitmap? {
    val repo = koinInject<InstalledAppsRepository>()
    val cache = remember { mutableMapOf<String, ImageBitmap?>() }
    return { pkg -> cache.getOrPut(pkg) { repo.icon(pkg)?.toBitmap(96, 96)?.asImageBitmap() } }
}

@Composable
fun ProtectRoot(onAddApp: () -> Unit, onPreview: () -> Unit, vm: ProtectViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val checker = koinInject<PermissionChecker>()
    val guide = koinInject<KeepRunningGuide>()
    val blocking = koinInject<BlockingController>()
    val context = LocalContext.current
    var keepDone by remember { mutableStateOf(false) }
    // returning from the Settings pages re-checks the grants (no navigation here, so no NavController re-entrancy)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        vm.onAction(ProtectAction.PermissionsChanged(checker.hasUsageAccess(), checker.hasOverlayPermission()))
        // a permission just granted in Settings may be what lets the service start
        blocking.evaluate()
    }
    ProtectScreen(
        state = state.copy(showKeepRunning = state.showKeepRunning && !keepDone),
        icon = rememberIconLoader(),
        onAction = vm::onAction,
        nav = ProtectNav(
            onAddApp = onAddApp,
            onPreview = onPreview,
            onFixPermission = {
                context.startActivity(if (!state.usageGranted) checker.usageAccessSettingsIntent() else checker.overlaySettingsIntent())
            },
            onKeepAutostart = guide::openAutostart,
            onKeepBattery = guide::openBattery,
            onKeepDone = {
                guide.markDone()
                keepDone = true
            },
        ),
    )
}

@Composable
fun AppPickerRoot(onBack: () -> Unit, vm: ProtectViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val installed by vm.installed.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadInstalled() }
    AppPickerScreen(
        apps = installed,
        initial = state.locked.map { it.packageName }.toSet(),
        icon = rememberIconLoader(),
        onSave = {
            vm.onAction(ProtectAction.SetApps(it))
            onBack()
        },
        onBack = onBack,
    )
}
