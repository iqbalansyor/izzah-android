package com.iqbalansyori.izzah.presentation.mulai

import com.iqbalansyori.izzah.domain.model.Trigger

data class MulaiState(
    val kept: Int = 0,
    val steps: List<Boolean> = listOf(false, false, false),
    val triggers: Set<Trigger> = emptySet(),
)
