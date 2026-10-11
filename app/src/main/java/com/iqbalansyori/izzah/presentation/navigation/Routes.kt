package com.iqbalansyori.izzah.presentation.navigation

import com.iqbalansyori.izzah.designsystem.component.TabDest
import kotlinx.serialization.Serializable

const val EXTRA_DEEP_LINK = "izzah.deeplink"

@Serializable data object SplashRoute
@Serializable data object WelcomeRoute
@Serializable data object RestoreRoute
@Serializable data object OnboardingGraph
@Serializable data object NiatRoute
@Serializable data object TargetRoute
@Serializable data object RawanRoute
@Serializable data object IzinRoute
@Serializable data object CreatePinRoute
@Serializable data object NotifRoute
@Serializable data object DoneRoute
@Serializable data object UnlockRoute
@Serializable data class MainRoute(val tab: TabDest = TabDest.HOME)
@Serializable data object SholatRoute
@Serializable data object DzikirRoute
@Serializable data object DoaRoute
@Serializable data object JurnalRoute
@Serializable data object DisguiseRoute
@Serializable data object DataRoute
@Serializable data object LanguageRoute
@Serializable data object TargetEditRoute
@Serializable data object RemindersRoute
@Serializable data object AppPickerRoute
@Serializable data object JedaRoute
@Serializable data object MulaiLagiRoute
@Serializable data object ShieldPreviewRoute
