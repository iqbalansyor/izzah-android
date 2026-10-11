package com.iqbalansyori.izzah.data.lock

import com.iqbalansyori.izzah.domain.TimeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

class AppLockManager(private val time: TimeSource, private val graceSeconds: Long = 30) {
    private val _locked = MutableStateFlow(true)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()
    private var backgroundedAt: Instant? = null

    fun onBackground() {
        backgroundedAt = time.now()
    }

    fun onForeground(hasPin: Boolean) {
        val away = backgroundedAt?.let { time.now() - it }
        backgroundedAt = null
        when {
            !hasPin -> _locked.value = false
            away != null && away > graceSeconds.seconds -> _locked.value = true
        }
    }

    fun unlock() {
        _locked.value = false
    }

    fun lockNow() {
        _locked.value = true
    }
}
