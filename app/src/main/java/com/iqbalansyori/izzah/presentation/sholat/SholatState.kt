package com.iqbalansyori.izzah.presentation.sholat

import com.iqbalansyori.izzah.domain.model.GeoPoint
import com.iqbalansyori.izzah.domain.model.Prayer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class SholatState(
    val done: Set<Prayer> = emptySet(),
    val times: Map<Prayer, LocalTime>? = null,
    val city: String? = null,
    val today: LocalDate? = null,
    val showCityPicker: Boolean = false,
    val cities: List<GeoPoint> = emptyList(),
)
