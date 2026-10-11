package com.iqbalansyori.izzah.presentation.unlock

sealed interface UnlockAction {
    data class Digit(val d: Char) : UnlockAction
    data object Delete : UnlockAction
    data object BiometricSucceeded : UnlockAction
    data object ForgotTapped : UnlockAction
    data object ForgotDismissed : UnlockAction
    data object Tick : UnlockAction
}

sealed interface UnlockEvent {
    data object Unlocked : UnlockEvent
    data object PromptBiometric : UnlockEvent
}
