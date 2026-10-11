package com.iqbalansyori.izzah.presentation.jeda

data class JedaState(
    val secondsLeft: Int = 60,
    val inhale: Boolean = false,
    val breaths: Int = 1,
    val kept: Int = 0,
)
