package com.iqbalansyori.izzah.di

import androidx.biometric.BiometricManager
import androidx.lifecycle.SavedStateHandle
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import com.iqbalansyori.izzah.data.prayer.CityCatalog
import com.iqbalansyori.izzah.presentation.dzikir.DzikirViewModel
import com.iqbalansyori.izzah.presentation.data.DataViewModel
import com.iqbalansyori.izzah.presentation.home.HomeViewModel
import com.iqbalansyori.izzah.presentation.restore.RestoreViewModel
import com.iqbalansyori.izzah.presentation.jeda.JedaViewModel
import com.iqbalansyori.izzah.presentation.jurnal.JurnalViewModel
import com.iqbalansyori.izzah.presentation.mulai.MulaiViewModel
import com.iqbalansyori.izzah.presentation.protect.ProtectViewModel
import com.iqbalansyori.izzah.data.blocking.KeepRunningGuide
import com.iqbalansyori.izzah.presentation.settings.SettingsViewModel
import com.iqbalansyori.izzah.BuildConfig
import com.iqbalansyori.izzah.data.disguise.DisguiseManager
import com.iqbalansyori.izzah.data.security.BiometricChecker
import com.iqbalansyori.izzah.presentation.onboarding.OnboardingViewModel
import com.iqbalansyori.izzah.presentation.progress.ProgressViewModel
import com.iqbalansyori.izzah.presentation.sholat.SholatViewModel
import com.iqbalansyori.izzah.presentation.unlock.UnlockViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { UnlockViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { ProgressViewModel(get(), get(), get(), get(), get()) }
    viewModel { JedaViewModel(get(), get()) }
    viewModel { DzikirViewModel() }
    viewModel { JurnalViewModel(get()) }
    viewModel { MulaiViewModel(get()) }
    viewModel { ProtectViewModel(get(), get()) { get<KeepRunningGuide>().shouldShow() } }
    viewModel { DataViewModel(get(), get(), get()) }
    viewModel { RestoreViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get(), BuildConfig.VERSION_NAME, { get<BiometricChecker>().isAvailable() }, { get<DisguiseManager>().apply(it) }) }
    viewModel { SholatViewModel(get(), get(), get(), get<CityCatalog>().cities, get()) }
    viewModel {
        val context = androidContext()
        OnboardingViewModel(get(), get(), get<SavedStateHandle>()) {
            BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }
}
