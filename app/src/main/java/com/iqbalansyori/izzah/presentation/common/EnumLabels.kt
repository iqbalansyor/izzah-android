package com.iqbalansyori.izzah.presentation.common

import androidx.annotation.StringRes
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.domain.model.Mood
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.domain.model.Why

@get:StringRes
val Why.label: Int
    get() = when (this) {
        Why.ALLAH -> R.string.niat_opt_allah
        Why.FAMILY -> R.string.niat_opt_family
        Why.SPOUSE -> R.string.niat_opt_spouse
        Why.FOCUS -> R.string.niat_opt_focus
        Why.PEACE -> R.string.niat_opt_peace
        Why.SELF_RESPECT -> R.string.niat_opt_selfrespect
    }

@get:StringRes
val Rawan.label: Int
    get() = when (this) {
        Rawan.LATE_NIGHT -> R.string.rawan_opt_late
        Rawan.DAWN -> R.string.rawan_opt_dawn
        Rawan.AFTERNOON_ALONE -> R.string.rawan_opt_afternoon
        Rawan.AFTER_SOCIAL -> R.string.rawan_opt_social
        Rawan.STRESS -> R.string.rawan_opt_stress
        Rawan.WEEKEND -> R.string.rawan_opt_weekend
    }

@get:StringRes
val Prayer.label: Int
    get() = when (this) {
        Prayer.FAJR -> R.string.prayer_fajr
        Prayer.DHUHR -> R.string.prayer_dhuhr
        Prayer.ASR -> R.string.prayer_asr
        Prayer.MAGHRIB -> R.string.prayer_maghrib
        Prayer.ISHA -> R.string.prayer_isha
    }

@get:StringRes
val Mood.label: Int
    get() = when (this) {
        Mood.STRONG -> R.string.mood_strong
        Mood.OKAY -> R.string.mood_okay
        Mood.SHAKY -> R.string.mood_shaky
        Mood.HEAVY -> R.string.mood_heavy
    }
