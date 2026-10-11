package com.iqbalansyori.izzah.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.iqbalansyori.izzah.designsystem.component.IzzahTabBar
import com.iqbalansyori.izzah.designsystem.component.TabDest
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors

@Composable
fun MainScaffold(
    initialTab: TabDest,
    onJeda: () -> Unit,
    tabContent: @Composable (TabDest) -> Unit = { Text(it.name) },
) {
    var tab by rememberSaveable { mutableStateOf(initialTab) }
    val holder = rememberSaveableStateHolder()
    Box(Modifier.fillMaxSize().background(IzzahColors.Bg)) {
        holder.SaveableStateProvider(tab.name) { tabContent(tab) }
        Box(Modifier.align(Alignment.BottomCenter)) { IzzahTabBar(tab, { tab = it }, onJeda) }
    }
}
