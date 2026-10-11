package com.iqbalansyori.izzah.presentation.progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProgressRoot(vm: ProgressViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    ProgressScreen(state, vm::onAction)
}
