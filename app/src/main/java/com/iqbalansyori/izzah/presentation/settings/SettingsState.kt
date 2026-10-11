package com.iqbalansyori.izzah.presentation.settings

import com.iqbalansyori.izzah.domain.model.Disguise

data class SettingsUi(
    val biometric: Boolean = false,
    val biometricAvailable: Boolean = false,
    val hideInRecents: Boolean = false,
    val target: Int = 90,
    val disguise: Disguise = Disguise.IZZAH,
    val languageTag: String? = null,
    val versionName: String = "",
    val notifPrayer: Boolean = true,
    val notifDzikir: Boolean = true,
    val notifCheckin: Boolean = true,
)

sealed interface SettingsAction {
    data class SetBiometric(val on: Boolean) : SettingsAction
    data class SetHideInRecents(val on: Boolean) : SettingsAction
    data class SetTarget(val n: Int) : SettingsAction
    data class SetLanguage(val tag: String?) : SettingsAction
    data class SetDisguise(val d: Disguise) : SettingsAction
    data class SetNotif(val prayer: Boolean? = null, val dzikir: Boolean? = null, val checkin: Boolean? = null) : SettingsAction
}
