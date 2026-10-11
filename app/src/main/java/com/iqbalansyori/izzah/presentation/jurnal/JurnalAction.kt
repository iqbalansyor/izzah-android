package com.iqbalansyori.izzah.presentation.jurnal

import com.iqbalansyori.izzah.domain.model.Mood

sealed interface JurnalAction {
    data class PickMood(val m: Mood) : JurnalAction
    data class Edit(val t: String) : JurnalAction
    data object Save : JurnalAction
}

sealed interface JurnalEvent {
    data object Saved : JurnalEvent
}
