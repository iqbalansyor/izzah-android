package com.iqbalansyori.izzah.presentation.common

import androidx.annotation.StringRes
import com.iqbalansyori.izzah.R
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
