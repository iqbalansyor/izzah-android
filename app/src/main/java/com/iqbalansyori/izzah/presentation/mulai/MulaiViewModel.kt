package com.iqbalansyori.izzah.presentation.mulai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.domain.repository.JourneyRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MulaiViewModel(private val journey: JourneyRepository) : ViewModel() {
    private val _state = MutableStateFlow(MulaiState())
    val state: StateFlow<MulaiState> = _state.asStateFlow()

    private val _events = Channel<MulaiEvent>(Channel.BUFFERED)
    val events: Flow<MulaiEvent> = _events.receiveAsFlow()

    init {
        // read once on purpose: the screen promises the days kept so far still count, so logging the slip must not change this number
        viewModelScope.launch { _state.update { it.copy(kept = journey.observeJourney().first().kept) } }
    }

    fun onAction(a: MulaiAction) {
        when (a) {
            is MulaiAction.ToggleStep -> _state.update { s -> s.copy(steps = s.steps.mapIndexed { i, on -> if (i == a.i) !on else on }) }
            is MulaiAction.ToggleTrigger -> _state.update { it.copy(triggers = if (a.t in it.triggers) it.triggers - a.t else it.triggers + a.t) }
            MulaiAction.Confirm -> viewModelScope.launch {
                journey.logSlip(_state.value.triggers)
                _events.send(MulaiEvent.Done)
            }
        }
    }
}
