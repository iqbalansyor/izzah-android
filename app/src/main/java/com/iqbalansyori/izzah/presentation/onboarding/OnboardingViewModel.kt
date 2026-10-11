package com.iqbalansyori.izzah.presentation.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.data.security.PinHasher
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.logic.SlotSchedule
import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.domain.model.Why
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import com.iqbalansyori.izzah.domain.today
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnboardingViewModel(
    private val settings: SettingsRepository,
    private val time: TimeSource,
    private val saved: SavedStateHandle,
    biometricAvailable: () -> Boolean,
) : ViewModel() {
    private val _state = MutableStateFlow(restore(biometricAvailable()))
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _events = Channel<OnboardingEvent>(Channel.BUFFERED)
    val events: Flow<OnboardingEvent> = _events.receiveAsFlow()

    fun onAction(a: OnboardingAction) {
        when (a) {
            is OnboardingAction.ToggleWhy -> { _state.update { it.copy(why = it.why.toggle(a.w)) }; persist() }
            is OnboardingAction.PickTarget -> { _state.update { it.copy(target = a.n) }; persist() }
            is OnboardingAction.ToggleRawan -> { _state.update { it.copy(rawan = it.rawan.toggle(a.r)) }; persist() }
            is OnboardingAction.PermissionsChanged -> _state.update { it.copy(usageGranted = a.usage, overlayGranted = a.overlay) }
            is OnboardingAction.PinDigit -> pinDigit(a.d)
            OnboardingAction.PinDelete -> _state.update { it.copy(pinEntry = it.pinEntry.dropLast(1), pinMismatch = false) }
            is OnboardingAction.SetBiometric -> _state.update { it.copy(biometric = a.on && it.biometricAvailable) }
            is OnboardingAction.SetNotif -> _state.update {
                it.copy(
                    notifPrayer = a.prayer ?: it.notifPrayer,
                    notifDzikir = a.dzikir ?: it.notifDzikir,
                    notifCheckin = a.checkin ?: it.notifCheckin,
                )
            }
            OnboardingAction.Finish -> finish()
        }
    }

    // the permission steps send the user to system Settings, where the process can be killed; the PIN is deliberately not saved
    private fun persist() {
        val s = _state.value
        saved[KEY_WHY] = ArrayList(s.why.map { it.name })
        saved[KEY_TARGET] = s.target
        saved[KEY_RAWAN] = ArrayList(s.rawan.map { it.name })
    }

    private fun restore(biometricAvailable: Boolean): OnboardingState {
        val defaults = OnboardingState(biometricAvailable = biometricAvailable)
        return defaults.copy(
            why = saved.get<ArrayList<String>>(KEY_WHY)?.mapNotNull { n -> Why.entries.firstOrNull { it.name == n } }?.toSet() ?: defaults.why,
            target = saved.get<Int>(KEY_TARGET) ?: defaults.target,
            rawan = saved.get<ArrayList<String>>(KEY_RAWAN)?.mapNotNull { n -> Rawan.entries.firstOrNull { it.name == n } }?.toSet() ?: defaults.rawan,
        )
    }

    private fun pinDigit(d: Char) {
        val s = _state.value
        if (!d.isDigit() || s.pinEntry.length >= 4) return
        val entry = s.pinEntry + d
        _state.update { it.copy(pinEntry = entry, pinMismatch = false) }
        if (entry.length < 4) return
        val first = s.pinFirst
        viewModelScope.launch {
            if (first == null) {
                delay(300)
                _state.update { it.copy(pinFirst = entry, pinEntry = "") }
                _events.send(OnboardingEvent.PinCreated)
            } else if (entry == first) {
                _state.update { it.copy(pinEntry = "") }
                _events.send(OnboardingEvent.PinConfirmed)
            } else {
                _state.update { it.copy(pinFirst = null, pinEntry = "", pinMismatch = true) }
            }
        }
    }

    private fun finish() {
        val s = _state.value
        viewModelScope.launch {
            val salt = s.pinFirst?.let { PinHasher.newSalt() }
            val hash = s.pinFirst?.let { pin -> withContext(Dispatchers.Default) { PinHasher.hash(pin, salt!!) } }
            val today = time.today()
            settings.update {
                it.copy(
                    onboardingDone = true,
                    startDate = today,
                    why = s.why,
                    target = s.target,
                    rawan = s.rawan,
                    pinHash = hash,
                    pinSalt = salt,
                    biometric = s.biometric && hash != null,
                    notifPrayer = s.notifPrayer,
                    notifDzikir = s.notifDzikir,
                    notifCheckin = s.notifCheckin,
                    slots = SlotSchedule.defaults(s.rawan),
                )
            }
            _events.send(OnboardingEvent.Finished)
        }
    }

    private fun <T> Set<T>.toggle(x: T): Set<T> = if (x in this) this - x else this + x

    private companion object {
        const val KEY_WHY = "why"
        const val KEY_TARGET = "target"
        const val KEY_RAWAN = "rawan"
    }
}
