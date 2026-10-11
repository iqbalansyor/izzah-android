package com.iqbalansyori.izzah.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.iqbalansyori.izzah.data.lock.AppLockManager
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import com.iqbalansyori.izzah.presentation.splash.SplashScreen
import com.iqbalansyori.izzah.presentation.welcome.WelcomeScreen
import com.iqbalansyori.izzah.presentation.onboarding.NiatScreen
import com.iqbalansyori.izzah.presentation.onboarding.OnboardingViewModel
import com.iqbalansyori.izzah.presentation.onboarding.RawanScreen
import com.iqbalansyori.izzah.presentation.onboarding.TargetScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
private fun Placeholder(name: String) {
    Box(Modifier.fillMaxSize().background(IzzahColors.Bg), contentAlignment = Alignment.Center) { Text(name) }
}

private inline fun <reified T : Any> NavGraphBuilder.stub(name: String) = composable<T> { Placeholder(name) }

@Composable
private fun onboardingVm(nav: NavHostController, entry: NavBackStackEntry): OnboardingViewModel =
    koinViewModel(viewModelStoreOwner = remember(entry) { nav.getBackStackEntry(OnboardingGraph) })

@Composable
fun IzzahNavHost(deepLink: String?, onDeepLinkHandled: () -> Unit = {}) {
    val nav = rememberNavController()
    val lock = koinInject<AppLockManager>()
    val settings = koinInject<SettingsRepository>()
    val locked by lock.locked.collectAsStateWithLifecycle()
    val s by settings.settings.collectAsStateWithLifecycle(initialValue = null)
    val entry by nav.currentBackStackEntryAsState()
    val onboardingDone = s?.onboardingDone == true

    // no popUpTo: back from Unlock is blocked, and a successful unlock pops back to where the user was
    LaunchedEffect(locked, onboardingDone, entry) {
        val dest = entry?.destination
        val atUnlockOrSplash = dest?.hasRoute<UnlockRoute>() == true || dest?.hasRoute<SplashRoute>() == true
        if (locked && onboardingDone && dest != null && !atUnlockOrSplash) nav.navigate(UnlockRoute) { launchSingleTop = true }
    }

    LaunchedEffect(deepLink, locked, onboardingDone) {
        if (deepLink == null || locked || !onboardingDone) return@LaunchedEffect
        when (deepLink) {
            "sholat" -> nav.navigate(SholatRoute)
            "dzikir" -> nav.navigate(DzikirRoute)
            "jurnal" -> nav.navigate(JurnalRoute)
        }
        onDeepLinkHandled()
    }

    NavHost(nav, startDestination = SplashRoute) {
        composable<SplashRoute> {
            SplashScreen(onDone = {
                nav.navigate(if (onboardingDone) UnlockRoute else WelcomeRoute) { popUpTo(SplashRoute) { inclusive = true } }
            })
        }
        composable<WelcomeRoute> { WelcomeScreen(onStart = { nav.navigate(OnboardingGraph) }, onPin = { nav.navigate(RestoreRoute) }) }
        stub<RestoreRoute>("Restore")
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
            stub<IzinRoute>("Izin")
            stub<CreatePinRoute>("CreatePin")
            stub<NotifRoute>("Notif")
            stub<DoneRoute>("Done")
        }
        stub<UnlockRoute>("Unlock")
        composable<MainRoute> { e ->
            MainScaffold(initialTab = e.toRoute<MainRoute>().tab, onJeda = { nav.navigate(JedaRoute) })
        }
        stub<SholatRoute>("Sholat")
        stub<DzikirRoute>("Dzikir")
        stub<DoaRoute>("Doa")
        stub<JurnalRoute>("Jurnal")
        stub<DisguiseRoute>("Disguise")
        stub<DataRoute>("Data")
        stub<LanguageRoute>("Language")
        stub<TargetEditRoute>("TargetEdit")
        stub<RemindersRoute>("Reminders")
        stub<AppPickerRoute>("AppPicker")
        stub<JedaRoute>("Jeda")
        stub<MulaiLagiRoute>("MulaiLagi")
        stub<ShieldPreviewRoute>("ShieldPreview")
    }
}
