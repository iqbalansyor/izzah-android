package com.iqbalansyori.izzah.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(private val store: DataStore<Preferences>) : SettingsRepository {
    override val settings: Flow<Settings> = store.data.map(SettingsMapper::read).distinctUntilChanged()

    override suspend fun current(): Settings = settings.first()

    override suspend fun update(transform: (Settings) -> Settings) {
        store.edit {
            val next = transform(SettingsMapper.read(it))
            it.clear()
            SettingsMapper.write(it, next)
        }
    }

    override suspend fun clear() {
        store.edit { it.clear() }
    }
}
