package com.iqbalansyori.izzah.presentation.onboarding

import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.domain.model.Why

data class OnboardingState(
    val why: Set<Why> = setOf(Why.ALLAH),
    val target: Int = 90,
    val rawan: Set<Rawan> = setOf(Rawan.LATE_NIGHT),
    val usageGranted: Boolean = false,
    val overlayGranted: Boolean = false,
    val pinFirst: String? = null,
    val pinEntry: String = "",
    val pinMismatch: Boolean = false,
    val biometric: Boolean = false,
    val biometricAvailable: Boolean = false,
    val notifPrayer: Boolean = true,
    val notifDzikir: Boolean = true,
    val notifCheckin: Boolean = true,
)
