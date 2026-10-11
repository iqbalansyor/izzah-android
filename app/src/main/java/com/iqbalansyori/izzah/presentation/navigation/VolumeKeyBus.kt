package com.iqbalansyori.izzah.presentation.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object VolumeKeyBus {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    @Volatile
    var listening: Boolean = false

    // true = a screen is listening and consumed the key, so the system volume must not change
    fun emit(): Boolean {
        if (!listening) return false
        _events.tryEmit(Unit)
        return true
    }
}
