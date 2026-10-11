package com.iqbalansyori.izzah.presentation.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.data.locale.HijriFormatter
import kotlinx.datetime.LocalDate

private val MONTHS = intArrayOf(
    R.string.hijri_month_1, R.string.hijri_month_2, R.string.hijri_month_3, R.string.hijri_month_4,
    R.string.hijri_month_5, R.string.hijri_month_6, R.string.hijri_month_7, R.string.hijri_month_8,
    R.string.hijri_month_9, R.string.hijri_month_10, R.string.hijri_month_11, R.string.hijri_month_12,
)

fun hijriLabel(context: Context, date: LocalDate): String {
    val h = HijriFormatter.toHijri(date)
    return context.getString(R.string.hijri_suffix, h.day, context.getString(MONTHS[h.month - 1]), h.year)
}

@Composable
fun hijriLabel(date: LocalDate): String = hijriLabel(LocalContext.current, date)
