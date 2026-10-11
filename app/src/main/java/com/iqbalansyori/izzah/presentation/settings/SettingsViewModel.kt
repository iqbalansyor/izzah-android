package com.iqbalansyori.izzah.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.data.locale.LocaleSetter
import com.iqbalansyori.izzah.domain.model.Disguise
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val locale: LocaleSetter,
    private val versionName: String,
    private val biometricAvailable: () -> Boolean,
    private val applyDisguise: (Disguise) -> Unit = {},
) : ViewModel() {
    private val languageTag = MutableStateFlow(locale.current())

    val state: StateFlow<SettingsUi> = combine(settings.settings, languageTag) { s, tag ->
        SettingsUi(
            biometric = s.biometric,
            biometricAvailable = biometricAvailable(),
            hideInRecents = s.hideInRecents,
            target = s.target,
            disguise = s.disguise,
            languageTag = tag,
            versionName = versionName,
            notifPrayer = s.notifPrayer,
            notifDzikir = s.notifDzikir,
            notifCheckin = s.notifCheckin,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUi(versionName = versionName))

    fun onAction(a: SettingsAction) {
        when (a) {
            is SettingsAction.SetBiometric -> update { it.copy(biometric = a.on && biometricAvailable()) }
            is SettingsAction.SetHideInRecents -> update { it.copy(hideInRecents = a.on) }
            is SettingsAction.SetTarget -> update { it.copy(target = a.n) }
            is SettingsAction.SetNotif -> update {
                it.copy(
                    notifPrayer = a.prayer ?: it.notifPrayer,
                    notifDzikir = a.dzikir ?: it.notifDzikir,
                    notifCheckin = a.checkin ?: it.notifCheckin,
                )
            }
            is SettingsAction.SetDisguise -> {
                applyDisguise(a.d)
                update { it.copy(disguise = a.d) }
            }
            is SettingsAction.SetLanguage -> {
                languageTag.value = a.tag
                locale.set(a.tag)
            }
        }
    }

    private fun update(transform: (com.iqbalansyori.izzah.domain.model.Settings) -> com.iqbalansyori.izzah.domain.model.Settings) {
        viewModelScope.launch { settings.update(transform) }
    }
}
