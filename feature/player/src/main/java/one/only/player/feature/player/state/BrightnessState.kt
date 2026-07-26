package one.only.player.feature.player.state

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.DisposableEffectScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.util.Consumer
import one.only.player.feature.player.PlayerActivity
import one.only.player.feature.player.extensions.brightnessPercentage
import one.only.player.feature.player.extensions.currentBrightness

@Composable
fun rememberBrightnessState(): BrightnessState {
    val activity = LocalActivity.current
    val brightnessState = remember { BrightnessState(activity as PlayerActivity) }
    DisposableEffect(activity) { brightnessState.handleListeners(this) }
    return brightnessState
}

@Stable
class BrightnessState(
    private val activity: PlayerActivity,
) {
    val maxBrightness: Float = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
    var currentBrightness: Float by mutableFloatStateOf(activity.currentBrightness)
        private set

    var brightnessPercentage: Int by mutableIntStateOf(activity.brightnessPercentage)
        private set

    // PiP 期间暂存的窗口亮度覆盖值,非 null 表示亮度已交还系统
    private var suspendedBrightnessOverride: Float? = null

    fun updateBrightnessPercentage(percentage: Int) {
        setBrightness(brightness = percentage.coerceIn(0, 100) * maxBrightness / 100)
    }

    fun setBrightness(brightness: Float) {
        suspendedBrightnessOverride = null
        applyWindowBrightness(brightness.coerceIn(0f, maxBrightness))
    }

    // 进入 PiP 时调用,清除窗口覆盖让系统亮度设置生效
    fun suspendBrightnessOverride() {
        if (suspendedBrightnessOverride != null) return
        suspendedBrightnessOverride = activity.window.attributes.screenBrightness
        applyWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
    }

    // 退出 PiP 回到全屏时恢复播放器亮度
    fun resumeBrightnessOverride() {
        val brightness = suspendedBrightnessOverride ?: return
        suspendedBrightnessOverride = null
        applyWindowBrightness(brightness)
    }

    private fun applyWindowBrightness(brightness: Float) {
        val windowAttributes = activity.window.attributes
        windowAttributes.screenBrightness = brightness
        activity.window.attributes = windowAttributes
    }

    fun handleListeners(disposableEffectScope: DisposableEffectScope): DisposableEffectResult = with(disposableEffectScope) {
        val windowAttributesChangedListener: Consumer<WindowManager.LayoutParams?> = Consumer {
            if (suspendedBrightnessOverride != null) return@Consumer
            currentBrightness = activity.currentBrightness
            brightnessPercentage = activity.brightnessPercentage
        }
        activity.addOnWindowAttributesChangedListener(windowAttributesChangedListener)

        onDispose {
            activity.removeOnWindowAttributesChangedListener(windowAttributesChangedListener)
        }
    }
}
