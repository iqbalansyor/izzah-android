package com.iqbalansyori.izzah.presentation.unlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.data.lock.AppLockManager
import com.iqbalansyori.izzah.data.security.PinHasher
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.time.Duration.Companion.seconds

class UnlockViewModel(
    private val settings: SettingsRepository,
    private val lock: AppLockManager,
    private val time: TimeSource,
    private val iterations: Int = PinHasher.ITERATIONS,
    private val hashDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val _state = MutableStateFlow(UnlockState())
    val state: StateFlow<UnlockState> = _state.asStateFlow()

    private val _events = Channel<UnlockEvent>(Channel.BUFFERED)
    val events: Flow<UnlockEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val s = settings.current()
            _state.update { it.copy(biometricEnabled = s.biometric) }
            refreshCooldown()
            if (s.biometric) _events.send(UnlockEvent.PromptBiometric)
        }
    }

    fun onAction(a: UnlockAction) {
        when (a) {
            is UnlockAction.Digit -> digit(a.d)
            UnlockAction.Delete -> _state.update { it.copy(entry = it.entry.dropLast(1), wrong = false) }
            UnlockAction.BiometricSucceeded -> viewModelScope.launch { succeed() }
            UnlockAction.ForgotTapped -> _state.update { it.copy(showForgot = true) }
            UnlockAction.ForgotDismissed -> _state.update { it.copy(showForgot = false) }
            UnlockAction.Tick -> viewModelScope.launch { refreshCooldown() }
        }
    }

    private fun digit(d: Char) {
        if (!d.isDigit() || _state.value.cooldownSeconds > 0 || _state.value.entry.length >= 4) return
        val entry = _state.value.entry + d
        _state.update { it.copy(entry = entry, wrong = false) }
        if (entry.length == 4) viewModelScope.launch { verify(entry) }
    }

    private suspend fun verify(entry: String) {
        val s = settings.current()
        val salt = s.pinSalt
        val hash = s.pinHash
        val ok = salt != null && hash != null && withContext(hashDispatcher) { PinHasher.verify(entry, salt, hash, iterations) }
        if (ok) {
            succeed()
            return
        }
        val fails = s.pinFailCount + 1
        if (fails >= MAX_FAILS) {
            settings.update { it.copy(pinFailCount = 0, pinLockedUntil = time.now() + COOLDOWN) }
            _state.update { it.copy(entry = "", wrong = false) }
            refreshCooldown()
        } else {
            settings.update { it.copy(pinFailCount = fails) }
            _state.update { it.copy(entry = "", wrong = true) }
        }
    }

    private suspend fun succeed() {
        settings.update { it.copy(pinFailCount = 0, pinLockedUntil = null) }
        lock.unlock()
        _state.update { it.copy(entry = "", wrong = false, cooldownSeconds = 0) }
        _events.send(UnlockEvent.Unlocked)
    }

    private suspend fun refreshCooldown() {
        val until = settings.current().pinLockedUntil
        val left = if (until == null) 0 else ceil((until - time.now()).inWholeMilliseconds / 1000.0).toInt().coerceAtLeast(0)
        _state.update { it.copy(cooldownSeconds = left) }
    }

    private companion object {
        const val MAX_FAILS = 5
        val COOLDOWN = 30.seconds
    }
}
