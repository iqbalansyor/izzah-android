package com.iqbalansyori.izzah.presentation.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.iqbalansyori.izzah.data.blocking.PermissionChecker
import com.iqbalansyori.izzah.data.lock.AppLockManager
import com.iqbalansyori.izzah.presentation.common.ObserveAsEvents
import com.iqbalansyori.izzah.presentation.onboarding.CreatePinScreen
import com.iqbalansyori.izzah.presentation.onboarding.DoneScreen
import com.iqbalansyori.izzah.presentation.onboarding.IzinScreen
import com.iqbalansyori.izzah.presentation.onboarding.NiatScreen
import com.iqbalansyori.izzah.presentation.onboarding.NotifScreen
import com.iqbalansyori.izzah.presentation.onboarding.OnboardingAction
import com.iqbalansyori.izzah.presentation.onboarding.OnboardingEvent
import com.iqbalansyori.izzah.presentation.onboarding.OnboardingViewModel
import com.iqbalansyori.izzah.presentation.onboarding.RawanScreen
import com.iqbalansyori.izzah.presentation.onboarding.TargetScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
private fun onboardingVm(nav: NavHostController, entry: NavBackStackEntry): OnboardingViewModel =
    koinViewModel(viewModelStoreOwner = remember(entry) { nav.getBackStackEntry(OnboardingGraph) })

fun NavGraphBuilder.onboardingGraph(nav: NavHostController, lock: AppLockManager) {
    navigation<OnboardingGraph>(startDestination = NiatRoute) {
        composable<NiatRoute> { e ->
            val vm = onboardingVm(nav, e)
            val state by vm.state.collectAsStateWithLifecycle()
            NiatScreen(state, vm::onAction, onBack = { nav.popBackStack() }, onNext = { nav.navigate(TargetRoute) })
        }
        composable<TargetRoute> { e ->
            val vm = onboardingVm(nav, e)
            val state by vm.state.collectAsStateWithLifecycle()
            TargetScreen(state, vm::onAction, onBack = { nav.popBackStack() }, onNext = { nav.navigate(RawanRoute) })
        }
        composable<RawanRoute> { e ->
            val vm = onboardingVm(nav, e)
            val state by vm.state.collectAsStateWithLifecycle()
            RawanScreen(state, vm::onAction, onBack = { nav.popBackStack() }, onNext = { nav.navigate(IzinRoute) })
        }
        composable<IzinRoute> { e ->
            val vm = onboardingVm(nav, e)
            val state by vm.state.collectAsStateWithLifecycle()
            val checker = koinInject<PermissionChecker>()
            val context = LocalContext.current
            // only advance after returning from a settings page, so Back from the PIN step doesn't bounce forward again
            var openedSettings by remember { mutableStateOf(false) }
            var advance by remember { mutableStateOf(false) }
            // navigating inside the ON_RESUME callback crashes NavController (it is iterating its back stack), so defer it
            LaunchedEffect(advance) {
                if (advance) {
                    advance = false
                    nav.navigate(CreatePinRoute)
                }
            }
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                val usage = checker.hasUsageAccess()
                val overlay = checker.hasOverlayPermission()
                vm.onAction(OnboardingAction.PermissionsChanged(usage, overlay))
                if (openedSettings && usage && overlay) {
                    openedSettings = false
                    advance = true
                }
            }
            IzinScreen(
                state,
                onGrant = {
                    val intent = if (!state.usageGranted) checker.usageAccessSettingsIntent() else checker.overlaySettingsIntent()
                    openedSettings = true
                    context.startActivity(intent)
                },
                onSkip = { nav.navigate(CreatePinRoute) },
                onBack = { nav.popBackStack() },
            )
        }
        composable<CreatePinRoute> { e ->
            val vm = onboardingVm(nav, e)
            val state by vm.state.collectAsStateWithLifecycle()
            ObserveAsEvents(vm.events) { if (it == OnboardingEvent.PinConfirmed) nav.navigate(NotifRoute) }
            CreatePinScreen(state, vm::onAction, onBack = { nav.popBackStack() })
        }
        composable<NotifRoute> { e ->
            val vm = onboardingVm(nav, e)
            val state by vm.state.collectAsStateWithLifecycle()
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { nav.navigate(DoneRoute) }
            NotifScreen(
                state, vm::onAction,
                onBack = { nav.popBackStack() },
                onNext = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else nav.navigate(DoneRoute)
                },
            )
        }
        composable<DoneRoute> { e ->
            val vm = onboardingVm(nav, e)
            val state by vm.state.collectAsStateWithLifecycle()
            ObserveAsEvents(vm.events) {
                if (it == OnboardingEvent.Finished) nav.navigate(MainRoute()) { popUpTo(nav.graph.id) { inclusive = true } }
            }
            DoneScreen(state, onEnter = {
                // unlock first: finishing flips onboardingDone, and the lock guard would otherwise bounce to Unlock
                lock.unlock()
                vm.onAction(OnboardingAction.Finish)
            })
        }
    }
}
