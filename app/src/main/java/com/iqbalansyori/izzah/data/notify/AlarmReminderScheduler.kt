package com.iqbalansyori.izzah.data.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.iqbalansyori.izzah.data.prayer.PrayerTimeCalculator
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.localNow
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.repository.ReminderScheduler
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.toInstant

class AlarmReminderScheduler(
    private val context: Context,
    private val settings: SettingsRepository,
    private val time: TimeSource,
    private val scope: CoroutineScope,
) : ReminderScheduler {
    private val alarms get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun rescheduleAll() {
        scope.launch { reschedule() }
    }

    override fun cancelAll() {
        ALL_CODES.forEach { alarms.cancel(pending(it, null, null)) }
    }

    suspend fun reschedule() {
        cancelAll()
        // times come from the same settings snapshot as the toggles, not from PrayerTimesRepository's mirrored location, which can lag at start
        val s = settings.current()
        val plan = ReminderPlanner.next(time.localNow(), s) { date -> s.location?.let { PrayerTimeCalculator.times(date, it, time.zone) } }
        plan.forEach { schedule(it) }
    }

    fun schedule(r: PlannedReminder) {
        val at = r.at.toInstant(time.zone).toEpochMilliseconds()
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending(r.requestCode, r.kind, r.prayer))
    }

    private fun pending(code: Int, kind: ReminderKind?, prayer: Prayer?): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_KIND, kind?.name).putExtra(EXTRA_PRAYER, prayer?.name)
        return PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        const val EXTRA_KIND = "kind"
        const val EXTRA_PRAYER = "prayer"
        private val ALL_CODES = Prayer.entries.map { 100 + it.ordinal } + listOf(200, 201, 300)
    }
}
