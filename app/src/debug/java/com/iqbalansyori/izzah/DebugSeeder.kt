package com.iqbalansyori.izzah

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iqbalansyori.izzah.data.db.dao.DayLogDao
import com.iqbalansyori.izzah.data.db.dao.JedaDao
import com.iqbalansyori.izzah.data.db.dao.JournalDao
import com.iqbalansyori.izzah.data.db.entities.DayLogEntity
import com.iqbalansyori.izzah.data.db.entities.JedaEntity
import com.iqbalansyori.izzah.data.db.entities.JournalEntity
import com.iqbalansyori.izzah.data.db.toMask
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import com.iqbalansyori.izzah.domain.today
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// adb shell am broadcast -a com.iqbalansyori.izzah.SEED -p com.iqbalansyori.izzah
class DebugSeeder : BroadcastReceiver(), KoinComponent {
    private val dayLogs: DayLogDao by inject()
    private val journal: JournalDao by inject()
    private val jeda: JedaDao by inject()
    private val settings: SettingsRepository by inject()
    private val time: TimeSource by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                seed()
            } finally {
                pending.finish()
            }
        }
    }

    // the prototype's 45-day model: slips on days 9/21/38, 3..5 prayers, a journal entry unless i % 5 == 0
    private suspend fun seed() {
        val today = time.today()
        val start = today.minus(44, DateTimeUnit.DAY)
        settings.update { it.copy(startDate = start) }
        journal.deleteAll()
        jeda.deleteAll()
        for (i in 0 until 45) {
            val date = start.plus(i, DateTimeUnit.DAY)
            val slip = (i + 1) in setOf(9, 21, 38)
            val count = if (slip) 1 + (i % 2) else 3 + (i * 7) % 3
            dayLogs.upsert(DayLogEntity(date.toEpochDays().toLong(), Prayer.entries.take(count).toSet().toMask(), slip, 0))
            if (i % 5 != 0) {
                journal.insert(JournalEntry(date, if (slip) "A hard night, but I started again." else "A calm day. Alhamdulillah."))
            }
        }
        listOf(10, 14, 17, 22, 25, 29, 33, 36, 39, 41, 43).forEachIndexed { k, daysAgo ->
            val date = today.minus(daysAgo, DateTimeUnit.DAY)
            val minute = 22 * 60 + 10 + k * 16
            val at = LocalDateTime(date, LocalTime(minute / 60 % 24, minute % 60)).toInstant(time.zone)
            jeda.insert(JedaEntity(startedAtMs = at.toEpochMilliseconds(), completed = true))
        }
    }

    private fun JournalEntry(date: LocalDate, note: String) = JournalEntity(
        createdAtMs = LocalDateTime(date, LocalTime(21, 30)).toInstant(time.zone).toEpochMilliseconds(),
        epochDay = date.toEpochDays().toLong(),
        mood = 1,
        note = note,
    )
}
