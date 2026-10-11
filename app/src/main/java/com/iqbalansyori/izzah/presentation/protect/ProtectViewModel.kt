package com.iqbalansyori.izzah.presentation.protect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.domain.model.LockMode
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.domain.repository.InstalledApp
import com.iqbalansyori.izzah.domain.repository.InstalledAppsRepository
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SELF = "com.iqbalansyori.izzah"

class ProtectViewModel(
    private val settings: SettingsRepository,
    private val apps: InstalledAppsRepository,
    private val keepRunningNeeded: () -> Boolean,
) : ViewModel() {
    private class Local(val editingId: Int? = null, val usage: Boolean = true, val overlay: Boolean = true)

    private val local = MutableStateFlow(Local())

    val state: StateFlow<ProtectState> = combine(settings.settings, local) { s, l ->
        ProtectState(
            slots = s.slots,
            locked = s.lockedPackages.map { InstalledApp(it, apps.label(it) ?: it) }.sortedBy { it.label.lowercase() },
            mode = s.lockMode,
            usageGranted = l.usage,
            overlayGranted = l.overlay,
            showKeepRunning = keepRunningNeeded(),
            editing = s.slots.firstOrNull { it.id == l.editingId },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProtectState())

    private val _installed = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installed: StateFlow<List<InstalledApp>> = _installed.asStateFlow()

    // loaded on demand by the picker: querying every launchable app is not free
    fun loadInstalled() {
        viewModelScope.launch { _installed.value = apps.launchable().filter { it.packageName != SELF } }
    }

    fun onAction(a: ProtectAction) {
        when (a) {
            is ProtectAction.ToggleSlot -> update { s -> s.copy(slots = s.slots.map { if (it.id == a.id) it.copy(enabled = !it.enabled) else it }) }
            is ProtectAction.EditSlot -> local.update { Local(a.id, it.usage, it.overlay) }
            ProtectAction.DismissEdit -> local.update { Local(null, it.usage, it.overlay) }
            is ProtectAction.SaveSlot -> {
                update { s -> s.copy(slots = s.slots.map { if (it.id == a.slot.id) a.slot else it }) }
                local.update { Local(null, it.usage, it.overlay) }
            }
            is ProtectAction.SetMode -> update { it.copy(lockMode = a.m) }
            is ProtectAction.RemoveApp -> update { it.copy(lockedPackages = it.lockedPackages - a.pkg) }
            is ProtectAction.SetApps -> update { it.copy(lockedPackages = a.pkgs - SELF) }
            is ProtectAction.PermissionsChanged -> local.update { Local(it.editingId, a.usage, a.overlay) }
        }
    }

    private fun update(transform: (Settings) -> Settings) {
        viewModelScope.launch { settings.update(transform) }
    }
}
