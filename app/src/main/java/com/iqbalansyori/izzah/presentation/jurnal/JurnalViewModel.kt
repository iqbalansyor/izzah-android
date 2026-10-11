package com.iqbalansyori.izzah.presentation.jurnal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.domain.model.Mood
import com.iqbalansyori.izzah.domain.repository.JournalRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JurnalViewModel(private val journal: JournalRepository) : ViewModel() {
    private data class Form(val mood: Mood? = null, val note: String = "")

    private val form = MutableStateFlow(Form())

    private val _events = Channel<JurnalEvent>(Channel.BUFFERED)
    val events: Flow<JurnalEvent> = _events.receiveAsFlow()

    val state: StateFlow<JurnalState> = combine(form, journal.observeEntries()) { f, entries ->
        JurnalState(f.mood, f.note, entries.sortedByDescending { it.createdAt })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JurnalState())

    fun onAction(a: JurnalAction) {
        when (a) {
            is JurnalAction.PickMood -> form.update { it.copy(mood = if (it.mood == a.m) null else a.m) }
            is JurnalAction.Edit -> form.update { it.copy(note = a.t) }
            JurnalAction.Save -> save()
        }
    }

    private fun save() {
        val f = form.value
        if (f.mood == null && f.note.isBlank()) return
        viewModelScope.launch {
            journal.add(f.mood, f.note.trim())
            form.value = Form()
            _events.send(JurnalEvent.Saved)
        }
    }
}
