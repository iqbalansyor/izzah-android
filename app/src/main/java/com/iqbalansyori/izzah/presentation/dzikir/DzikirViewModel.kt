package com.iqbalansyori.izzah.presentation.dzikir

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DzikirViewModel : ViewModel() {
    private val _state = MutableStateFlow(DzikirState())
    val state: StateFlow<DzikirState> = _state.asStateFlow()

    private val _haptic = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    val haptic: SharedFlow<Unit> = _haptic.asSharedFlow()

    fun tap() {
        val s = _state.value
        if (s.finished) return
        if (s.count + 1 >= 33) {
            _state.update { it.copy(set = it.set + 1, count = 0) }
            _haptic.tryEmit(Unit)
        } else {
            _state.update { it.copy(count = it.count + 1) }
        }
    }

    fun reset() {
        _state.value = DzikirState()
    }
}
