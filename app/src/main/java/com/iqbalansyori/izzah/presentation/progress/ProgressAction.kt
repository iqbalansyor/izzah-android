package com.iqbalansyori.izzah.presentation.progress

sealed interface ProgressAction {
    data class TapCell(val index: Int) : ProgressAction
    data object ToggleReminder : ProgressAction
}
