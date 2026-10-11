package com.iqbalansyori.izzah.di

import androidx.biometric.BiometricManager
import androidx.lifecycle.SavedStateHandle
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import com.iqbalansyori.izzah.presentation.home.HomeViewModel
import com.iqbalansyori.izzah.presentation.jeda.JedaViewModel
import com.iqbalansyori.izzah.presentation.onboarding.OnboardingViewModel
import com.iqbalansyori.izzah.presentation.progress.ProgressViewModel
import com.iqbalansyori.izzah.presentation.unlock.UnlockViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { UnlockViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { ProgressViewModel(get(), get(), get(), get(), get()) }
    viewModel { JedaViewModel(get(), get()) }
    viewModel {
        val context = androidContext()
        OnboardingViewModel(get(), get(), get<SavedStateHandle>()) {
            BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }
}
