package com.iqbalansyori.izzah.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoPoint(val lat: Double, val lng: Double, val label: String)
