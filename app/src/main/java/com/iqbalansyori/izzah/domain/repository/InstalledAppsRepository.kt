package com.iqbalansyori.izzah.domain.repository

import android.graphics.drawable.Drawable

data class InstalledApp(val packageName: String, val label: String)

interface InstalledAppsRepository {
    suspend fun launchable(): List<InstalledApp>
    fun icon(pkg: String): Drawable?
    fun label(pkg: String): String?
}

val SUGGESTED_PACKAGES = listOf(
    "com.android.chrome", "org.mozilla.firefox", "com.sec.android.app.sbrowser", "com.opera.browser", "com.instagram.android",
    "com.twitter.android", "com.zhiliaoapp.musically", "com.ss.android.ugc.trill", "org.telegram.messenger", "com.google.android.youtube",
    "com.reddit.frontpage",
)
