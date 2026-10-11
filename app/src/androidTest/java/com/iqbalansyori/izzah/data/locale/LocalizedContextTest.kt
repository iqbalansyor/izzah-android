package com.iqbalansyori.izzah.data.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.iqbalansyori.izzah.MainActivity
import com.iqbalansyori.izzah.R
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalizedContextTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @After
    fun reset() = instrumentation.runOnMainSync { AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList()) }

    private fun setLocale(tag: String) {
        instrumentation.runOnMainSync { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag)) }
        // the system applies per-app locales asynchronously
        repeat(25) {
            if (AppCompatDelegate.getApplicationLocales().toLanguageTags().startsWith(tag.take(2))) return
            Thread.sleep(200)
        }
    }

    @Test
    fun overlayAndNotificationsFollowAppLanguage() {
        val ctx = instrumentation.targetContext
        // locales only apply to an app that is in the foreground
        ActivityScenario.launch(MainActivity::class.java).use {
            setLocale("in")
            assertThat(ctx.localized().getString(R.string.tab_home)).isEqualTo("Beranda")
            setLocale("en")
            assertThat(ctx.localized().getString(R.string.tab_home)).isEqualTo("Home")
        }
    }
}
