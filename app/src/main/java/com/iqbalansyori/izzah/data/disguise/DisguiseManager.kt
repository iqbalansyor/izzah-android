package com.iqbalansyori.izzah.data.disguise

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.iqbalansyori.izzah.domain.model.Disguise

class DisguiseManager(private val context: Context) {
    // alias classes live in the namespace, which differs from packageName on builds with an applicationId suffix
    private fun alias(d: Disguise) = ComponentName(
        context.packageName,
        "com.iqbalansyori.izzah.Launcher${d.name.lowercase().replaceFirstChar { it.uppercase() }}",
    )

    fun apply(d: Disguise) {
        val pm = context.packageManager
        // enable the new alias before disabling the others, so the launcher never has zero entries
        pm.setComponentEnabledSetting(alias(d), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        Disguise.entries.filter { it != d }.forEach {
            pm.setComponentEnabledSetting(alias(it), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }
    }

    fun current(): Disguise {
        val pm = context.packageManager
        return Disguise.entries.firstOrNull {
            when (pm.getComponentEnabledSetting(alias(it))) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> it == Disguise.IZZAH
                else -> false
            }
        } ?: Disguise.IZZAH
    }
}
