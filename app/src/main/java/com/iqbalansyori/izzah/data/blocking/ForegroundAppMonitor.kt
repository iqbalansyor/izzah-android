package com.iqbalansyori.izzah.data.blocking

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

// UsageStats instead of an AccessibilityService: it needs only the usage-access grant, which keeps Play review risk low
class ForegroundAppMonitor(private val context: Context) {

    private val _foregroundPackage = MutableStateFlow<String?>(null)
    val foregroundPackage: StateFlow<String?> = _foregroundPackage.asStateFlow()

    // call about once a second from the foreground service; queryEvents is a blocking IPC, hence the IO dispatcher
    suspend fun poll() = withContext(Dispatchers.IO) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - POLL_WINDOW_MS

        val events = usageStatsManager.queryEvents(start, end)
        var lastForegroundPackage: String? = null
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForegroundPackage = event.packageName
            }
        }

        if (lastForegroundPackage != null) {
            _foregroundPackage.value = lastForegroundPackage
        }
    }

    companion object {
        private const val POLL_WINDOW_MS = 10_000L
    }
}
