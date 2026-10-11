package com.iqbalansyori.izzah.presentation.onboarding

import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.domain.model.Why

sealed interface OnboardingAction {
    data class ToggleWhy(val w: Why) : OnboardingAction
    data class PickTarget(val n: Int) : OnboardingAction
    data class ToggleRawan(val r: Rawan) : OnboardingAction
    data class PermissionsChanged(val usage: Boolean, val overlay: Boolean) : OnboardingAction
    data class PinDigit(val d: Char) : OnboardingAction
    data object PinDelete : OnboardingAction
    data class SetBiometric(val on: Boolean) : OnboardingAction
    data class SetNotif(val prayer: Boolean? = null, val dzikir: Boolean? = null, val checkin: Boolean? = null) : OnboardingAction
    data object Finish : OnboardingAction
}

sealed interface OnboardingEvent {
    // first entry accepted: the screen switches to the confirm step
    data object PinCreated : OnboardingEvent
    data object PinConfirmed : OnboardingEvent
    data object Finished : OnboardingEvent
}
