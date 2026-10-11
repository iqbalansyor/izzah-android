package com.iqbalansyori.izzah.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val onboardingDone = booleanPreferencesKey("onboarding_done")
    val startDay = longPreferencesKey("start_epoch_day")
    val why = stringPreferencesKey("why")
    val target = intPreferencesKey("target")
    val rawan = stringPreferencesKey("rawan")
    val pinHash = stringPreferencesKey("pin_hash")
    val pinSalt = stringPreferencesKey("pin_salt")
    val biometric = booleanPreferencesKey("biometric")
    val pinFailCount = intPreferencesKey("pin_fail_count")
    val pinLockedUntil = longPreferencesKey("pin_locked_until_ms")
    val notifPrayer = booleanPreferencesKey("notif_prayer")
    val notifDzikir = booleanPreferencesKey("notif_dzikir")
    val notifCheckin = booleanPreferencesKey("notif_checkin")
    val slots = stringPreferencesKey("slots")
    val lockedPackages = stringPreferencesKey("locked_packages")
    val lockMode = stringPreferencesKey("lock_mode")
    val disguise = stringPreferencesKey("disguise")
    val hideInRecents = booleanPreferencesKey("hide_in_recents")
    val location = stringPreferencesKey("location")
}
