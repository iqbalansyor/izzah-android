package com.iqbalansyori.izzah.presentation.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.data.DataWiper
import com.iqbalansyori.izzah.data.security.BackupError
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.repository.BackupRepository
import com.iqbalansyori.izzah.domain.today
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

data class DataState(
    val pinSheet: Boolean = false,
    val entry: String = "",
    val pinError: Boolean = false,
    val confirmDelete: Boolean = false,
    val exportDone: Boolean = false,
)

sealed interface DataAction {
    data object StartExport : DataAction
    data class Digit(val d: Char) : DataAction
    data object Delete : DataAction
    data object DismissPin : DataAction
    data object AskDelete : DataAction
    data object DismissDelete : DataAction
    data object ConfirmDelete : DataAction
    data object ToastShown : DataAction
}

sealed interface DataEvent {
    data class CreateFile(val name: String) : DataEvent
}

class DataViewModel(
    private val backup: BackupRepository,
    private val wiper: DataWiper,
    private val time: TimeSource,
) : ViewModel() {
    private val _state = MutableStateFlow(DataState())
    val state: StateFlow<DataState> = _state.asStateFlow()

    private val _events = Channel<DataEvent>(Channel.BUFFERED)
    val events: Flow<DataEvent> = _events.receiveAsFlow()

    private var pending: ByteArray? = null

    fun onAction(a: DataAction) {
        when (a) {
            DataAction.StartExport -> _state.update { it.copy(pinSheet = true, entry = "", pinError = false) }
            DataAction.DismissPin -> _state.update { it.copy(pinSheet = false, entry = "", pinError = false) }
            DataAction.Delete -> _state.update { it.copy(entry = it.entry.dropLast(1), pinError = false) }
            is DataAction.Digit -> digit(a.d)
            DataAction.AskDelete -> _state.update { it.copy(confirmDelete = true) }
            DataAction.DismissDelete -> _state.update { it.copy(confirmDelete = false) }
            DataAction.ConfirmDelete -> viewModelScope.launch { wiper.wipeAll() }
            DataAction.ToastShown -> _state.update { it.copy(exportDone = false) }
        }
    }

    private fun digit(d: Char) {
        val s = _state.value
        if (!d.isDigit() || s.entry.length >= 4) return
        val entry = s.entry + d
        _state.update { it.copy(entry = entry, pinError = false) }
        if (entry.length < 4) return
        viewModelScope.launch {
            try {
                pending = backup.export(entry)
                _state.update { it.copy(pinSheet = false, entry = "") }
                val c = time.today()
                _events.send(DataEvent.CreateFile("backup-%04d%02d%02d.bin".format(c.year, c.monthNumber, c.dayOfMonth)))
            } catch (e: BackupError) {
                _state.update { it.copy(entry = "", pinError = true) }
            }
        }
    }

    // called once the user picked a file; the bytes only leave memory through this stream
    suspend fun writePending(open: () -> OutputStream?) {
        val bytes = pending ?: return
        val ok = withContext(Dispatchers.IO) { open()?.use { it.write(bytes) } != null }
        pending = null
        if (ok) _state.update { it.copy(exportDone = true) }
    }

    fun discardPending() {
        pending = null
    }
}
