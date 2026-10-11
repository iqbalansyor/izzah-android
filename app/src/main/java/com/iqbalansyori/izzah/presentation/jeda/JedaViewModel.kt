package com.iqbalansyori.izzah.presentation.jeda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.domain.repository.JedaRepository
import com.iqbalansyori.izzah.domain.repository.JourneyRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class JedaViewModel(
    private val jeda: JedaRepository,
    journey: JourneyRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(JedaState())
    val state: StateFlow<JedaState> = _state.asStateFlow()

    private var eventId: Long? = null
    private val completing = Mutex()

    init {
        viewModelScope.launch {
            eventId = jeda.start()
            _state.update { it.copy(kept = journey.observeJourney().first().kept) }
            launch { breathe() }
            launch { countdown() }
        }
    }

    fun calmer() {
        viewModelScope.launch { complete() }
    }

    // the shield overlay (Task 31) is hosted by a service and has no ViewModelStore to clear it
    internal fun clear() = viewModelScope.cancel()

    private suspend fun breathe() {
        delay(60)
        _state.update { it.copy(inhale = true) }
        while (true) {
            delay(4000)
            _state.update { if (it.inhale) it.copy(inhale = false) else it.copy(inhale = true, breaths = it.breaths + 1) }
        }
    }

    private suspend fun countdown() {
        while (_state.value.secondsLeft > 0) {
            delay(1000)
            _state.update { it.copy(secondsLeft = it.secondsLeft - 1) }
        }
        complete()
    }

    // tryLock never unlocks: the event may only be completed once, by whichever of calmer()/the timer comes first
    private suspend fun complete() {
        if (!completing.tryLock()) return
        eventId?.let { jeda.complete(it) }
    }
}
