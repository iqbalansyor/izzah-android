package com.iqbalansyori.izzah.data.blocking

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.iqbalansyori.izzah.data.locale.localized
import com.iqbalansyori.izzah.designsystem.theme.IzzahTheme

// Draws Compose over other apps via TYPE_APPLICATION_OVERLAY (needs SYSTEM_ALERT_WINDOW).
// Windows are keyed by slot: the touchable flag applies to a whole window, so a click-through full-screen layer
// and a tappable pill on top of it can't be one window. A bare WindowManager view has no Activity to take a
// lifecycle/saved-state owner from, which Compose requires, so this class supplies minimal ones.
// An overlay can't take focus from the foreground app, so it can't intercept Back; if that app restacks its windows
// the overlay can lose z-order silently, which is what bringToFront is for. Call it sparingly: remove + add briefly
// breaks in-flight taps (dispatchTouchEvent returns false mid-swap).
class OverlayController(private val context: Context) {

    private val windows = mutableMapOf<String, OverlayWindow>()

    fun isShowing(slot: String = SLOT_MAIN): Boolean = windows.containsKey(slot)

    // touchable = false for informational layers so taps pass through to the app underneath;
    // WRAP_CONTENT sizes make the window's touchable bounds hug what is drawn; gravity only matters for those
    fun show(
        slot: String = SLOT_MAIN,
        touchable: Boolean = true,
        widthPx: Int = WindowManager.LayoutParams.MATCH_PARENT,
        heightPx: Int = WindowManager.LayoutParams.MATCH_PARENT,
        gravity: Int = Gravity.TOP or Gravity.CENTER_HORIZONTAL,
        content: @Composable () -> Unit,
    ) {
        if (windows.containsKey(slot)) return

        val owner = OverlayLifecycleOwner().apply { onCreate() }

        // the localized context makes overlay strings follow the in-app language, which a service context would not
        val view = ComposeView(context.localized()).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { IzzahTheme { content() } }
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        // do not add LAYOUT_NO_LIMITS or fitInsetsTypes = 0 to cover the system-bar strips: both make the overlay ignore touches
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        if (!touchable) flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        val layoutParams = WindowManager.LayoutParams(widthPx, heightPx, overlayType, flags, PixelFormat.TRANSLUCENT).apply {
            this.gravity = gravity
        }

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(view, layoutParams)
        windows[slot] = OverlayWindow(view, owner, layoutParams)
        owner.onStart()
    }

    fun bringToFront(slot: String = SLOT_MAIN) {
        val window = windows[slot] ?: return
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.removeView(window.view)
        windowManager.addView(window.view, window.params)
    }

    fun hide(slot: String = SLOT_MAIN) {
        val window = windows.remove(slot) ?: return
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.removeView(window.view)
        window.owner.onDestroy()
    }

    fun hideAll() {
        windows.keys.toList().forEach { hide(it) }
    }

    companion object {
        const val SLOT_MAIN = "main"
        const val SLOT_ISLAND = "island"
    }
}

private class OverlayWindow(
    val view: ComposeView,
    val owner: OverlayLifecycleOwner,
    val params: WindowManager.LayoutParams,
)

private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    fun onStart() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}
