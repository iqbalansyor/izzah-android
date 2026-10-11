package com.iqbalansyori.izzah.presentation.protect

import com.iqbalansyori.izzah.domain.model.LockMode
import com.iqbalansyori.izzah.domain.model.Slot
import com.iqbalansyori.izzah.domain.repository.InstalledApp

data class ProtectState(
    val slots: List<Slot> = emptyList(),
    val locked: List<InstalledApp> = emptyList(),
    val mode: LockMode = LockMode.PAUSE,
    val usageGranted: Boolean = true,
    val overlayGranted: Boolean = true,
    val showKeepRunning: Boolean = false,
    val editing: Slot? = null,
)

sealed interface ProtectAction {
    data class ToggleSlot(val id: Int) : ProtectAction
    data class EditSlot(val id: Int) : ProtectAction
    data class SaveSlot(val slot: Slot) : ProtectAction
    data object DismissEdit : ProtectAction
    data class SetMode(val m: LockMode) : ProtectAction
    data class RemoveApp(val pkg: String) : ProtectAction
    data class SetApps(val pkgs: Set<String>) : ProtectAction
    data class PermissionsChanged(val usage: Boolean, val overlay: Boolean) : ProtectAction
}
