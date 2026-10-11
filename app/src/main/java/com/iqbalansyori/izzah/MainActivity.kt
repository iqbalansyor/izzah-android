package com.iqbalansyori.izzah

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.iqbalansyori.izzah.designsystem.theme.IzzahTheme
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import com.iqbalansyori.izzah.presentation.navigation.EXTRA_DEEP_LINK
import com.iqbalansyori.izzah.presentation.navigation.IzzahNavHost
import com.iqbalansyori.izzah.presentation.navigation.VolumeKeyBus
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val settings: SettingsRepository by inject()
    private var deepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        deepLink = intent.getStringExtra(EXTRA_DEEP_LINK)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                settings.settings.map { it.hideInRecents }.distinctUntilChanged().collect { hide ->
                    if (hide) window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                    else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }
        setContent { IzzahTheme { IzzahNavHost(deepLink, onDeepLinkHandled = { deepLink = null }) } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLink = intent.getStringExtra(EXTRA_DEEP_LINK)
    }

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        val volume = e.keyCode == KeyEvent.KEYCODE_VOLUME_UP || e.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (volume && e.action == KeyEvent.ACTION_DOWN && VolumeKeyBus.emit()) return true
        return super.dispatchKeyEvent(e)
    }
}
