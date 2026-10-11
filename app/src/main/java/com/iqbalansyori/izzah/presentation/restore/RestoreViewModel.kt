package com.iqbalansyori.izzah.presentation.restore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.data.lock.AppLockManager
import com.iqbalansyori.izzah.data.security.BackupError
import com.iqbalansyori.izzah.domain.repository.BackupRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RestoreState(
    val pinSheet: Boolean = false,
    val entry: String = "",
    val error: BackupError? = null,
)

sealed interface RestoreEvent {
    data object Restored : RestoreEvent
}

class RestoreViewModel(private val backup: BackupRepository, private val lock: AppLockManager) : ViewModel() {
    private val _state = MutableStateFlow(RestoreState())
    val state: StateFlow<RestoreState> = _state.asStateFlow()

    private val _events = Channel<RestoreEvent>(Channel.BUFFERED)
    val events: Flow<RestoreEvent> = _events.receiveAsFlow()

    private var blob: ByteArray? = null

    fun onFile(bytes: ByteArray) {
        blob = bytes
        _state.update { RestoreState(pinSheet = true) }
    }

    fun delete() = _state.update { it.copy(entry = it.entry.dropLast(1), error = null) }

    fun dismiss() {
        blob = null
        _state.value = RestoreState()
    }

    fun digit(d: Char) {
        val s = _state.value
        val data = blob
        if (!d.isDigit() || s.entry.length >= 4 || data == null) return
        val entry = s.entry + d
        _state.update { it.copy(entry = entry, error = null) }
        if (entry.length < 4) return
        viewModelScope.launch {
            val result = backup.restore(data, entry)
            if (result.isSuccess) {
                blob = null
                lock.unlock()
                _state.value = RestoreState()
                _events.send(RestoreEvent.Restored)
            } else {
                _state.update { it.copy(entry = "", error = result.exceptionOrNull() as? BackupError ?: BackupError.NotABackup) }
            }
        }
    }
}
