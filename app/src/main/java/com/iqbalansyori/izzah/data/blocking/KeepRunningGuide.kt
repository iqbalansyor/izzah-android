package com.iqbalansyori.izzah.data.blocking

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

// Xiaomi/Redmi/POCO phones stop background apps and refuse boot-time starts unless Autostart is allowed and the battery
// restriction removed. No API can grant either, so all we can do is send the user to those screens.
class KeepRunningGuide(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun shouldShow(): Boolean = isXiaomiFamily && !prefs.getBoolean(KEY_DONE, false)

    fun markDone() = prefs.edit().putBoolean(KEY_DONE, true).apply()

    fun openAutostart() = open(
        Intent().setComponent(
            ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
        ),
    )

    fun openBattery() = open(
        Intent().setComponent(
            ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"),
        )
            .putExtra("package_name", context.packageName)
            .putExtra("package_label", context.applicationInfo.loadLabel(context.packageManager).toString()),
    )

    // the vendor screens are undocumented and move between MIUI/HyperOS versions, so a miss falls back to our App info page
    private fun open(target: Intent) {
        try {
            context.startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            openAppInfo()
        } catch (_: SecurityException) {
            openAppInfo()
        }
    }

    private fun openAppInfo() = context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )

    private val isXiaomiFamily: Boolean
        get() = Build.MANUFACTURER.lowercase() in setOf("xiaomi", "redmi", "poco")

    private companion object {
        const val PREFS = "izzah_keep_running"
        const val KEY_DONE = "done"
    }
}
