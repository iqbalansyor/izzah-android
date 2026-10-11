package com.iqbalansyori.izzah.presentation.dzikir

data class DzikirState(val set: Int = 0, val count: Int = 0) {
    val finished get() = set > 2
    val progress get() = if (finished) 1f else count / 33f
}
