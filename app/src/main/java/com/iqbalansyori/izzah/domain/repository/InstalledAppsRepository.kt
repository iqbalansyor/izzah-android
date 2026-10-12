package com.iqbalansyori.izzah.domain.repository

import android.graphics.drawable.Drawable

data class InstalledApp(val packageName: String, val label: String)

interface InstalledAppsRepository {
    suspend fun launchable(): List<InstalledApp>
    fun icon(pkg: String): Drawable?
    fun label(pkg: String): String?
}

val SUGGESTED_PACKAGES = listOf(
    "com.instagram.android", "com.instagram.lite", "com.instagram.barcelona",
    "com.facebook.katana", "com.facebook.lite",
    "com.google.android.youtube",
    "com.zhiliaoapp.musically", "com.zhiliaoapp.musically.go", "com.ss.android.ugc.trill",
    "com.twitter.android", "com.twitter.android.lite",
    "com.snapchat.android", "com.reddit.frontpage", "com.pinterest", "com.tumblr",
    "org.telegram.messenger", "org.telegram.messenger.web",
    "video.like", "com.kwai.video", "com.kwai.bulldog", "sg.bigo.live", "tv.twitch.android.app",
    "com.vidio.android", "com.netflix.mediaclient",
    "com.android.chrome", "org.mozilla.firefox", "com.sec.android.app.sbrowser", "com.opera.browser", "com.opera.mini.native",
    "com.brave.browser", "com.microsoft.emmx", "com.UCMobile.intl", "mark.via.gp",
)
