package one.only.player.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import top.yukonga.miuix.kmp.preference.SliderPreference

@Composable
fun PreferenceSlider(
    modifier: Modifier = Modifier,
    sliderModifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    isEnabled: Boolean = true,
    isSliderEnabled: Boolean = isEnabled,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
    trailingContent: @Composable () -> Unit = {},
) {
    PreferenceContainer(modifier = modifier) {
        SliderPreference(
            value = value,
            onValueChange = onValueChange,
            title = title,
            summary = description,
            startAction = icon?.let { { PreferenceIcon(it, isEnabled) } },
            endActions = { trailingContent() },
            enabled = isEnabled && isSliderEnabled,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished,
        )
    }
}
