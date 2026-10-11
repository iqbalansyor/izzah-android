package com.iqbalansyori.izzah.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import com.iqbalansyori.izzah.domain.repository.InstalledApp
import com.iqbalansyori.izzah.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsRepositoryImpl(private val context: Context) : InstalledAppsRepository {
    private val pm get() = context.packageManager

    override suspend fun launchable(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .map { InstalledApp(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    override fun icon(pkg: String): Drawable? = runCatching { pm.getApplicationIcon(pkg) }.getOrNull()

    override fun label(pkg: String): String? = runCatching { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() }.getOrNull()
}
