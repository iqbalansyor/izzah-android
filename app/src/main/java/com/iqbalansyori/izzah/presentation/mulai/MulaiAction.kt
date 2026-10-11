package com.iqbalansyori.izzah.presentation.mulai

import com.iqbalansyori.izzah.domain.model.Trigger

sealed interface MulaiAction {
    data class ToggleStep(val i: Int) : MulaiAction
    data class ToggleTrigger(val t: Trigger) : MulaiAction
    data object Confirm : MulaiAction
}

sealed interface MulaiEvent {
    data object Done : MulaiEvent
}
