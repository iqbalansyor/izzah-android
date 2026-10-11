package com.iqbalansyori.izzah.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.iqbalansyori.izzah.data.DataWiper
import com.iqbalansyori.izzah.data.lock.AppLockManager
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import com.iqbalansyori.izzah.designsystem.component.TabDest
import com.iqbalansyori.izzah.presentation.data.DataRoot
import com.iqbalansyori.izzah.presentation.doa.DoaScreen
import com.iqbalansyori.izzah.presentation.dzikir.DzikirRoot
import com.iqbalansyori.izzah.presentation.home.HomeNav
import com.iqbalansyori.izzah.presentation.home.HomeRoot
import com.iqbalansyori.izzah.presentation.jeda.JedaRoot
import com.iqbalansyori.izzah.presentation.jurnal.JurnalRoot
import com.iqbalansyori.izzah.presentation.mulai.MulaiRoot
import com.iqbalansyori.izzah.presentation.progress.ProgressRoot
import com.iqbalansyori.izzah.presentation.restore.RestoreRoot
import com.iqbalansyori.izzah.presentation.settings.DisguiseRoot
import com.iqbalansyori.izzah.presentation.settings.LanguageRoot
import com.iqbalansyori.izzah.presentation.settings.RemindersRoot
import com.iqbalansyori.izzah.presentation.settings.SettingsNav
import com.iqbalansyori.izzah.presentation.settings.TargetEditRoot
import com.iqbalansyori.izzah.presentation.settings.SettingsRoot
import com.iqbalansyori.izzah.presentation.sholat.SholatRoot
import com.iqbalansyori.izzah.presentation.splash.SplashScreen
import com.iqbalansyori.izzah.presentation.welcome.WelcomeScreen
import com.iqbalansyori.izzah.presentation.unlock.UnlockRoot
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
private fun Placeholder(name: String) {
    Box(Modifier.fillMaxSize().background(IzzahColors.Bg), contentAlignment = Alignment.Center) { Text(name) }
}

private inline fun <reified T : Any> NavGraphBuilder.stub(name: String) = composable<T> { Placeholder(name) }

@Composable
fun IzzahNavHost(deepLink: String?, onDeepLinkHandled: () -> Unit = {}) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val lock = koinInject<AppLockManager>()
    val settings = koinInject<SettingsRepository>()
    val wiper = koinInject<DataWiper>()
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
        composable<RestoreRoute> {
            RestoreRoot(
                onBack = { nav.popBackStack() },
                onRestored = { nav.navigate(MainRoute()) { popUpTo(nav.graph.id) { inclusive = true } } },
            )
        }
        onboardingGraph(nav, lock)
        composable<UnlockRoute> {
            UnlockRoot(
                onUnlocked = {
                    // a successful unlock returns to where the user was; on a cold start there is nothing below Unlock
                    if (nav.previousBackStackEntry != null) nav.popBackStack()
                    else nav.navigate(MainRoute()) { popUpTo<UnlockRoute> { inclusive = true } }
                },
                onRestore = { nav.navigate(RestoreRoute) },
                onWipe = { scope.launch { wiper.wipeAll() } },
            )
        }
        composable<MainRoute> { e ->
            MainScaffold(initialTab = e.toRoute<MainRoute>().tab, onJeda = { nav.navigate(JedaRoute) }) { tab, selectTab ->
                when (tab) {
                    TabDest.HOME -> HomeRoot(
                        HomeNav(
                            onDzikir = { nav.navigate(DzikirRoute) },
                            onSholat = { nav.navigate(SholatRoute) },
                            onJurnal = { nav.navigate(JurnalRoute) },
                            onDoa = { nav.navigate(DoaRoute) },
                            onProgress = { selectTab(TabDest.PROGRESS) },
                            onSettings = { selectTab(TabDest.SETTINGS) },
                            onMulaiLagi = { nav.navigate(MulaiLagiRoute) },
                        ),
                    )
                    TabDest.PROGRESS -> ProgressRoot()
                    TabDest.SETTINGS -> SettingsRoot(
                        SettingsNav(
                            onDisguise = { nav.navigate(DisguiseRoute) },
                            onTarget = { nav.navigate(TargetEditRoute) },
                            onReminders = { nav.navigate(RemindersRoute) },
                            onSchedule = { selectTab(TabDest.PROTECT) },
                            onData = { nav.navigate(DataRoute) },
                            onCounselor = {},
                            onLanguage = { nav.navigate(LanguageRoute) },
                        ),
                    )
                    else -> Placeholder(tab.name)
                }
            }
        }
        composable<SholatRoute> { SholatRoot(onBack = { nav.popBackStack() }) }
        composable<DzikirRoute> { DzikirRoot(onBack = { nav.popBackStack() }) }
        composable<DoaRoute> { DoaScreen(onBack = { nav.popBackStack() }) }
        composable<JurnalRoute> { JurnalRoot(onBack = { nav.popBackStack() }) }
        composable<DisguiseRoute> { DisguiseRoot(onBack = { nav.popBackStack() }) }
        composable<DataRoute> { DataRoot(onBack = { nav.popBackStack() }) }
        composable<LanguageRoute> { LanguageRoot(onBack = { nav.popBackStack() }) }
        composable<TargetEditRoute> { TargetEditRoot(onBack = { nav.popBackStack() }) }
        composable<RemindersRoute> { RemindersRoot(onBack = { nav.popBackStack() }) }
        stub<AppPickerRoute>("AppPicker")
        composable<JedaRoute> { JedaRoot(onClose = { nav.popBackStack() }, onDoa = { nav.navigate(DoaRoute) }) }
        composable<MulaiLagiRoute> {
            MulaiRoot(
                onClose = { nav.popBackStack() },
                onDone = { nav.navigate(MainRoute(TabDest.HOME)) { popUpTo(nav.graph.id) { inclusive = true } } },
            )
        }
        stub<ShieldPreviewRoute>("ShieldPreview")
    }
}
