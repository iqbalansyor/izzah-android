package com.iqbalansyori.izzah.presentation.sholat

import com.iqbalansyori.izzah.domain.model.GeoPoint
import com.iqbalansyori.izzah.domain.model.Prayer

sealed interface SholatAction {
    data class Toggle(val p: Prayer) : SholatAction
    data object PickCity : SholatAction
    data class CityChosen(val p: GeoPoint) : SholatAction
    data object DismissPicker : SholatAction
    data class LocationFound(val p: GeoPoint) : SholatAction
}
