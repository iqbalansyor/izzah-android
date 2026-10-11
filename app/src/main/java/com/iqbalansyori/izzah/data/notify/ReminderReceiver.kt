package com.iqbalansyori.izzah.data.notify

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.iqbalansyori.izzah.MainActivity
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.data.locale.localized
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.presentation.common.label
import com.iqbalansyori.izzah.presentation.navigation.EXTRA_DEEP_LINK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderReceiver : BroadcastReceiver(), KoinComponent {
    private val scheduler: AlarmReminderScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val kind = intent.getStringExtra(AlarmReminderScheduler.EXTRA_KIND)?.let { runCatching { ReminderKind.valueOf(it) }.getOrNull() } ?: return
        val prayer = intent.getStringExtra(AlarmReminderScheduler.EXTRA_PRAYER)?.let { runCatching { Prayer.valueOf(it) }.getOrNull() }
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                notify(context, kind, prayer)
                scheduler.reschedule()
            } finally {
                pending.finish()
            }
        }
    }

    private fun notify(context: Context, kind: ReminderKind, prayer: Prayer?) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!granted) return
        val ctx = context.localized()
        val body = when (kind) {
            ReminderKind.PRAYER -> ctx.getString(R.string.notif_prayer_body, ctx.getString((prayer ?: Prayer.FAJR).label))
            ReminderKind.DZIKIR_MORNING -> ctx.getString(R.string.notif_dzikir_morning)
            ReminderKind.DZIKIR_EVENING -> ctx.getString(R.string.notif_dzikir_evening)
            ReminderKind.CHECKIN -> ctx.getString(R.string.notif_checkin)
        }
        val deepLink = when (kind) {
            ReminderKind.PRAYER -> "sholat"
            ReminderKind.DZIKIR_MORNING, ReminderKind.DZIKIR_EVENING -> "dzikir"
            ReminderKind.CHECKIN -> "jurnal"
        }
        val open = PendingIntent.getActivity(
            context,
            kind.ordinal * 10 + (prayer?.ordinal ?: 0),
            Intent(context, MainActivity::class.java).putExtra(EXTRA_DEEP_LINK, deepLink).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        // on a secure lock screen only the app name shows; the wording is never exposed there
        val publicVersion = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
            .setSmallIcon(R.drawable.ic_notif_star)
            .setContentTitle(ctx.getString(R.string.app_name))
            .build()
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
            .setSmallIcon(R.drawable.ic_notif_star)
            .setContentTitle(ctx.getString(R.string.app_name))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(kind.ordinal * 10 + (prayer?.ordinal ?: 0), notification)
    }
}
