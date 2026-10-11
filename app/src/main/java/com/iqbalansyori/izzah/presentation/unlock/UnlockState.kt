package com.iqbalansyori.izzah.presentation.unlock

data class UnlockState(
    val entry: String = "",
    val wrong: Boolean = false,
    val cooldownSeconds: Int = 0,
    val biometricEnabled: Boolean = false,
    val showForgot: Boolean = false,
)
