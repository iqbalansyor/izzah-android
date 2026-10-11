package com.iqbalansyori.izzah.data.blocking

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.data.locale.LocaleFormats
import com.iqbalansyori.izzah.data.locale.localized
import com.iqbalansyori.izzah.data.notify.NotificationChannels
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.localNow
import com.iqbalansyori.izzah.domain.logic.BlockGate
import com.iqbalansyori.izzah.domain.logic.SlotSchedule
import com.iqbalansyori.izzah.domain.repository.JourneyRepository
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import com.iqbalansyori.izzah.presentation.shield.ShieldOutcome
import com.iqbalansyori.izzah.presentation.shield.ShieldOverlay
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.minutes

// Polls the foreground app about three times a second; when it is a locked app inside an active slot, draws the shield over it.
class IzzahBlockerService : Service() {

    private val monitor: ForegroundAppMonitor by inject()
    private val overlay: OverlayController by inject()
    private val settings: SettingsRepository by inject()
    private val journey: JourneyRepository by inject()
    private val time: TimeSource by inject()
    private val formats: LocaleFormats by inject()

    private val exceptionHandler = CoroutineExceptionHandler { _, t -> Log.e(TAG, "poll loop crashed", t) }

    // Main, not Default: OverlayController touches WindowManager and a LifecycleRegistry, which both assert the main thread
    private val scope = CoroutineScope(Dispatchers.Main + Job() + exceptionHandler)

    // a granted "continue" lasts five minutes per package
    private val passes = mutableMapOf<String, Instant>()

    private var shownFor: String? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        scope.launch {
            while (true) {
                checkForegroundApp()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        overlay.hideAll()
        scope.cancel()
    }

    private suspend fun checkForegroundApp() {
        monitor.poll()
        val pkg = monitor.foregroundPackage.value ?: return
        if (pkg == packageName) return
        val s = settings.current()
        val now = time.localNow()
        if (!BlockGate.shouldBlock(pkg, now, time.now(), s.lockedPackages, s.slots, passes)) {
            if (overlay.isShowing()) {
                overlay.hideAll()
                shownFor = null
            }
            return
        }
        // Not re-asserting z-order with bringToFront(): on API 36 the remove+add it does leaves the Compose overlay
        // permanently deaf to touches after the first swap, which is worse than the bypass it defends against.
        if (overlay.isShowing()) return
        val (slot, until) = SlotSchedule.activeUntil(s.slots, now) ?: return
        val kept = journey.observeJourney().first().kept
        shownFor = pkg
        overlay.show(OverlayController.SLOT_MAIN) {
            ShieldOverlay(s.lockMode, formats.time(until.time), dayTime = slot.startMinute < 18 * 60, kept = kept) { outcome ->
                when (outcome) {
                    ShieldOutcome.GRANT_PASS -> {
                        passes[pkg] = time.now() + 5.minutes
                        overlay.hideAll()
                        shownFor = null
                    }
                    ShieldOutcome.CLOSE_TO_HOME -> dismissToHome()
                }
            }
        }
    }

    // Close and system Back both land here: hiding in place would reveal the locked app underneath, which is the bypass
    // this whole mechanism exists to prevent. The only exits are a granted pass or going home.
    private fun dismissToHome() {
        overlay.hideAll()
        shownFor = null
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun buildNotification(): Notification {
        val ctx = localized()
        return NotificationCompat.Builder(this, NotificationChannels.PROTECT)
            .setContentTitle(ctx.getString(R.string.app_name))
            .setContentText(ctx.getString(R.string.notif_protect_ongoing))
            .setSmallIcon(R.drawable.ic_notif_star)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private companion object {
        const val TAG = "IzzahBlockerService"
        const val NOTIFICATION_ID = 1
        // a cold launch of a locked app is visible for up to one interval before the shield appears
        const val POLL_INTERVAL_MS = 300L
    }
}
