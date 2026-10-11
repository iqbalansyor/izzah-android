package com.iqbalansyori.izzah.presentation.home

sealed interface HomeAction {
    data class TapBar(val index: Int) : HomeAction
    data object CloseSheet : HomeAction
}
