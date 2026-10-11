package com.iqbalansyori.izzah.data.prayer

import com.iqbalansyori.izzah.domain.model.GeoPoint
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class CityCatalog(json: String) {
    val cities: List<GeoPoint> = Json { ignoreUnknownKeys = true }.decodeFromString(ListSerializer(GeoPoint.serializer()), json)
}
