package com.iqbalansyori.izzah.data.prayer

import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.model.GeoPoint
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.repository.PrayerTimesRepository
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import com.iqbalansyori.izzah.domain.todayFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class PrayerTimesRepositoryImpl(
    private val settings: SettingsRepository,
    private val time: TimeSource,
    scope: CoroutineScope,
) : PrayerTimesRepository {
    // timesFor() is synchronous (alarm scheduling), so the location is mirrored from DataStore
    private val location = settings.settings.map { it.location }.stateIn(scope, SharingStarted.Eagerly, null)

    override fun observeToday(): Flow<Map<Prayer, LocalTime>?> =
        combine(time.todayFlow(), settings.settings.map { it.location }) { day, loc ->
            loc?.let { PrayerTimeCalculator.times(day, it, time.zone) }
        }

    override fun timesFor(date: LocalDate): Map<Prayer, LocalTime>? =
        location.value?.let { PrayerTimeCalculator.times(date, it, time.zone) }

    override suspend fun setLocation(p: GeoPoint) {
        settings.update { it.copy(location = p) }
    }
}
