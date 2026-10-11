package com.iqbalansyori.izzah.data.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

interface LocaleSetter {
    // null = follow the system language
    fun set(tag: String?)
    fun current(): String?
}

class AppCompatLocaleSetter : LocaleSetter {
    override fun set(tag: String?) {
        AppCompatDelegate.setApplicationLocales(if (tag == null) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag))
    }

    override fun current(): String? = AppCompatDelegate.getApplicationLocales()[0]?.language?.let { if (it == "id") "in" else it }
}
