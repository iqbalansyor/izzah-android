package com.iqbalansyori.izzah.presentation.unlock

import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.presentation.common.ObserveAsEvents
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun UnlockRoot(onUnlocked: () -> Unit, onRestore: () -> Unit, onWipe: () -> Unit, vm: UnlockViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as FragmentActivity

    BackHandler { activity.finish() }

    LaunchedEffect(state.cooldownSeconds > 0) {
        while (state.cooldownSeconds > 0) {
            delay(1000)
            vm.onAction(UnlockAction.Tick)
        }
    }

    ObserveAsEvents(vm.events) { event ->
        when (event) {
            UnlockEvent.Unlocked -> onUnlocked()
            UnlockEvent.PromptBiometric -> {
                val prompt = BiometricPrompt(
                    activity,
                    ContextCompat.getMainExecutor(activity),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            vm.onAction(UnlockAction.BiometricSucceeded)
                        }
                    },
                )
                prompt.authenticate(
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle(activity.getString(R.string.biometric_title))
                        .setNegativeButtonText(activity.getString(R.string.biometric_use_pin))
                        .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
                        .build(),
                )
            }
        }
    }

    UnlockScreen(state, vm::onAction)
    if (state.showForgot) {
        ForgotPinSheet(
            onDismiss = { vm.onAction(UnlockAction.ForgotDismissed) },
            onRestore = {
                vm.onAction(UnlockAction.ForgotDismissed)
                onRestore()
            },
            onDelete = {
                vm.onAction(UnlockAction.ForgotDismissed)
                onWipe()
            },
        )
    }
}
