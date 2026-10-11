package com.iqbalansyori.izzah

import android.Manifest
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test

// Runs on a fresh install (connectedDebugAndroidTest installs cleanly); the app's data cannot be cleared from inside its own process.
class SmokeTest {
    // granted up front so the notification step never opens the system dialog, which is slow and flaky on a loaded emulator
    @get:Rule(order = 0)
    val permission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private val ctx get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun s(id: Int) = ctx.getString(id)

    // before the first frame there is no compose hierarchy at all and fetching nodes throws
    private fun waitFor(text: String) = rule.waitUntil(30_000) {
        runCatching { rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
    }

    private fun tapPin(code: String) = code.forEach { rule.onNodeWithText(it.toString()).performClick() }

    @Test
    fun onboardingThroughHomeAndPrayerCheckIn() {
        waitFor(s(R.string.welcome_cta_start))
        rule.onNodeWithText(s(R.string.welcome_cta_start)).performClick()
        repeat(3) {
            waitFor(s(R.string.common_continue))
            rule.onNodeWithText(s(R.string.common_continue)).performClick()
        }
        waitFor(s(R.string.izin_skip))
        rule.onNodeWithText(s(R.string.izin_skip)).performClick()

        waitFor(s(R.string.pin_create_title))
        tapPin("1111")
        waitFor(s(R.string.pin_confirm_title))
        tapPin("1111")

        waitFor(s(R.string.notif_cta))
        rule.onNodeWithText(s(R.string.notif_cta)).performClick()

        waitFor(s(R.string.done_cta))
        rule.onNodeWithText(s(R.string.done_cta)).performClick()

        waitFor(s(R.string.home_hero_label).uppercase())
        // the hero starts at 0 until the first journey emission arrives
        waitFor("1")
        rule.onNodeWithText("1").assertExists()
        rule.onNodeWithTag("todayBar", useUnmergedTree = true).assertHeightIsEqualTo(22.dp)

        rule.onNodeWithText(s(R.string.home_shortcut_sholat).uppercase()).performClick()
        waitFor(s(R.string.sholat_title))
        rule.onNodeWithText(s(R.string.prayer_fajr)).performClick()
        rule.onNodeWithContentDescription(s(R.string.common_back_cd)).performClick()

        // one prayer ticked: 22 + 11 for the prayer + 15 for the check-in
        waitFor(s(R.string.home_hero_label).uppercase())
        rule.onNodeWithTag("todayBar", useUnmergedTree = true).assertHeightIsEqualTo(48.dp)
    }
}
