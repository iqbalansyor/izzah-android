package com.iqbalansyori.izzah.presentation.sholat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.model.GeoPoint
import com.iqbalansyori.izzah.domain.repository.JourneyRepository
import com.iqbalansyori.izzah.domain.repository.PrayerTimesRepository
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import com.iqbalansyori.izzah.domain.today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SholatViewModel(
    private val journey: JourneyRepository,
    private val prayers: PrayerTimesRepository,
    settings: SettingsRepository,
    private val cities: List<GeoPoint>,
    time: TimeSource,
) : ViewModel() {
    private val showPicker = MutableStateFlow(false)
    private val today = time.today()

    val state: StateFlow<SholatState> = combine(
        journey.observeToday(), prayers.observeToday(), settings.settings, showPicker,
    ) { log, times, s, picker ->
        SholatState(
            done = log.prayers,
            times = times,
            city = s.location?.label?.takeIf { it.isNotBlank() },
            today = today,
            showCityPicker = picker,
            cities = cities,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SholatState(cities = cities))

    fun onAction(a: SholatAction) {
        when (a) {
            is SholatAction.Toggle -> viewModelScope.launch { journey.togglePrayer(a.p) }
            SholatAction.PickCity -> showPicker.value = true
            SholatAction.DismissPicker -> showPicker.value = false
            is SholatAction.CityChosen -> {
                showPicker.value = false
                viewModelScope.launch { prayers.setLocation(a.p) }
            }
            is SholatAction.LocationFound -> viewModelScope.launch { prayers.setLocation(a.p) }
        }
    }
}
