package com.iqbalansyori.izzah.data.blocking

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.iqbalansyori.izzah.domain.repository.BlockingController
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ServiceBlockingController(
    private val context: Context,
    private val settings: SettingsRepository,
    private val permissions: PermissionChecker,
    private val scope: CoroutineScope,
) : BlockingController {

    // runs while something is locked, a slot is enabled and both special grants exist; otherwise it would just no-op silently
    override fun evaluate() {
        scope.launch {
            val s = settings.current()
            val wanted = s.lockedPackages.isNotEmpty() && s.slots.any { it.enabled } && permissions.hasAllBlockingPermissions()
            if (wanted) start() else stop()
        }
    }

    override fun stop() {
        context.stopService(Intent(context, IzzahBlockerService::class.java))
    }

    private fun start() {
        val intent = Intent(context, IzzahBlockerService::class.java)
        try {
            context.startForegroundService(intent)
        } catch (e: Exception) {
            // Android 12+ refuses a foreground-service start from the background (a boot receiver is in that state);
            // the next evaluate() from the app opening picks it up
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                Log.w(TAG, "OS denied the foreground service start; will retry when the app opens", e)
            } else {
                throw e
            }
        }
    }

    private companion object {
        const val TAG = "BlockingController"
    }
}
