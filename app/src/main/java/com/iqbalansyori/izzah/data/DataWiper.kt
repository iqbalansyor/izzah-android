package com.iqbalansyori.izzah.data

import android.content.Context
import android.content.Intent
import com.iqbalansyori.izzah.data.db.IzzahDatabase
import com.iqbalansyori.izzah.data.disguise.DisguiseManager
import com.iqbalansyori.izzah.data.security.KeystorePassphraseProvider
import com.iqbalansyori.izzah.domain.model.Disguise
import com.iqbalansyori.izzah.domain.repository.BlockingController
import com.iqbalansyori.izzah.domain.repository.ReminderScheduler
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataWiper(
    private val context: Context,
    private val db: IzzahDatabase,
    private val settings: SettingsRepository,
    private val keys: KeystorePassphraseProvider,
    private val disguise: DisguiseManager,
    private val reminders: ReminderScheduler,
    private val blocking: BlockingController,
) {
    suspend fun wipeAll() {
        blocking.stop()
        reminders.cancelAll()
        disguise.apply(Disguise.IZZAH)
        withContext(Dispatchers.IO) { db.clearAllTables() }
        settings.clear()
        keys.destroy()
        withContext(Dispatchers.IO) {
            db.close()
            context.deleteDatabase("izzah.db")
        }
        // the Room singleton still holds the old passphrase, so the process has to restart
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        if (launch != null) context.startActivity(launch)
        Runtime.getRuntime().exit(0)
    }
}
