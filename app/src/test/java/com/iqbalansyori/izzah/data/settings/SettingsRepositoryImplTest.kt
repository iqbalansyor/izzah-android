package com.iqbalansyori.izzah.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.iqbalansyori.izzah.domain.model.Settings
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.File

class SettingsRepositoryImplTest {
    @Test
    fun `update persists and clear resets`() = runTest {
        val file = File.createTempFile("izzah", ".preferences_pb").also { it.delete() }
        val repo = SettingsRepositoryImpl(PreferenceDataStoreFactory.create(scope = backgroundScope) { file })
        repo.update { it.copy(target = 7, biometric = true) }
        assertThat(repo.current().target).isEqualTo(7)
        assertThat(repo.current().biometric).isEqualTo(true)
        repo.clear()
        assertThat(repo.current()).isEqualTo(Settings())
        file.delete()
    }
}
