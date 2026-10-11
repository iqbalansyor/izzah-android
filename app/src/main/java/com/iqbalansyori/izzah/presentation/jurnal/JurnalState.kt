package com.iqbalansyori.izzah.presentation.jurnal

import com.iqbalansyori.izzah.domain.model.JournalEntry
import com.iqbalansyori.izzah.domain.model.Mood

data class JurnalState(
    val mood: Mood? = null,
    val note: String = "",
    val entries: List<JournalEntry> = emptyList(),
) {
    val canSave get() = mood != null || note.isNotBlank()
}
