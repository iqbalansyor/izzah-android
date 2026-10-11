package com.iqbalansyori.izzah.data.locale

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate

// before API 33 a process woken by an alarm has not read AppCompat's stored language yet, so we keep our own copy of the tag
internal object LanguageStore {
    private const val PREFS = "izzah_language"
    private const val KEY = "tag"

    fun write(context: Context, tag: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply { if (tag == null) remove(KEY) else putString(KEY, tag) }.apply()
    }

    fun read(context: Context): String? = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
}

// services, receivers and overlays don't get the per-app language automatically; strings read through this context do
fun Context.localized(): Context {
    var tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    if (tags.isEmpty() && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) tags = LanguageStore.read(this).orEmpty()
    if (tags.isEmpty()) return this
    val config = Configuration(resources.configuration).apply { setLocales(LocaleList.forLanguageTags(tags)) }
    return createConfigurationContext(config)
}
