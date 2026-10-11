package com.iqbalansyori.izzah.di

import androidx.biometric.BiometricManager
import androidx.lifecycle.SavedStateHandle
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import com.iqbalansyori.izzah.presentation.onboarding.OnboardingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        val context = androidContext()
        OnboardingViewModel(get(), get(), get<SavedStateHandle>()) {
            BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }
}
