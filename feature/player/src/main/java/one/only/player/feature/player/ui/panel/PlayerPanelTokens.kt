package one.only.player.feature.player.ui.panel

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.Colors as MiuixColors
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 播放器面板统一使用磨砂风格：亮色主题浅色磨砂，暗色主题深色磨砂。
@Stable
data class PlayerPanelTokens(
    val containerColor: Color,
    val containerCornerRadius: Dp,
    val containerBorderColor: Color,
    val contentColor: Color,
    val secondaryContentColor: Color,
    val accentColor: Color,
    val onAccentColor: Color,
    val itemCornerRadius: Dp,
    val itemSpacing: Dp,
    val itemVerticalPadding: Dp,
    val itemColor: Color,
    val itemSelectedColor: Color,
    val itemContentColor: Color,
    val itemSelectedContentColor: Color,
    val tileCornerRadius: Dp,
)

// 独立卡片列表：每个条目使用统一的完整圆角。
val PlayerPanelTokens.optionShape: RoundedCornerShape
    get() = RoundedCornerShape(itemCornerRadius)

@Composable
fun rememberPlayerPanelTokens(): PlayerPanelTokens {
    val colorScheme = MiuixTheme.colorScheme
    val isDarkTheme = colorScheme.background.luminance() < 0.5f
    val primary = colorScheme.primary
    val onPrimary = colorScheme.onPrimary
    return remember(isDarkTheme, primary, onPrimary) {
        if (isDarkTheme) {
            PlayerPanelTokens(
                containerColor = Color(0xB31E1E22),
                containerCornerRadius = 28.dp,
                containerBorderColor = Color.White.copy(alpha = 0.16f),
                contentColor = Color(0xFFF2F2F7),
                secondaryContentColor = Color(0x99F2F2F7),
                accentColor = primary,
                onAccentColor = onPrimary,
                itemCornerRadius = 20.dp,
                itemSpacing = 8.dp,
                itemVerticalPadding = 14.dp,
                itemColor = Color.White.copy(alpha = 0.14f),
                itemSelectedColor = primary,
                itemContentColor = Color(0xFFF2F2F7),
                itemSelectedContentColor = onPrimary,
                tileCornerRadius = 18.dp,
            )
        } else {
            PlayerPanelTokens(
                containerColor = Color(0xBFF2F2F7),
                containerCornerRadius = 28.dp,
                containerBorderColor = Color.White.copy(alpha = 0.6f),
                contentColor = Color(0xFF1C1C1E),
                secondaryContentColor = Color(0x991C1C1E),
                accentColor = primary,
                onAccentColor = onPrimary,
                itemCornerRadius = 20.dp,
                itemSpacing = 8.dp,
                itemVerticalPadding = 14.dp,
                itemColor = Color.White.copy(alpha = 0.62f),
                itemSelectedColor = primary,
                itemContentColor = Color(0xFF1C1C1E),
                itemSelectedContentColor = onPrimary,
                tileCornerRadius = 18.dp,
            )
        }
    }
}

// 面板内 miuix 组件（Preference 系列、按钮、开关等）跟随面板风格换色。
@Composable
fun PlayerPanelTokens.rememberPanelMiuixColors(): MiuixColors {
    val baseColors = MiuixTheme.colorScheme
    return remember(this, baseColors) {
        baseColors.copy(
            primary = accentColor,
            onPrimary = onAccentColor,
            primaryVariant = accentColor,
            onPrimaryVariant = onAccentColor,
            primaryContainer = itemSelectedColor,
            onPrimaryContainer = itemSelectedContentColor,
            disabledPrimary = accentColor.copy(alpha = 0.4f),
            disabledOnPrimary = onAccentColor.copy(alpha = 0.6f),
            disabledPrimaryButton = accentColor.copy(alpha = 0.4f),
            disabledOnPrimaryButton = onAccentColor.copy(alpha = 0.6f),
            disabledPrimarySlider = accentColor.copy(alpha = 0.5f),
            secondaryVariant = itemColor,
            onSecondaryVariant = itemContentColor,
            disabledSecondaryVariant = itemColor.copy(alpha = itemColor.alpha * 0.5f),
            disabledOnSecondaryVariant = itemContentColor.copy(alpha = 0.35f),
            background = containerColor,
            onBackground = contentColor,
            onBackgroundVariant = secondaryContentColor,
            surface = containerColor,
            onSurface = contentColor,
            surfaceVariant = itemColor,
            onSurfaceSecondary = secondaryContentColor,
            onSurfaceVariantSummary = secondaryContentColor,
            onSurfaceVariantActions = secondaryContentColor,
            disabledOnSurface = contentColor.copy(alpha = 0.35f),
            surfaceContainer = itemColor,
            onSurfaceContainer = itemContentColor,
            onSurfaceContainerVariant = secondaryContentColor,
            surfaceContainerHigh = itemColor,
            onSurfaceContainerHigh = itemContentColor,
            surfaceContainerHighest = itemColor,
            onSurfaceContainerHighest = itemContentColor,
            outline = contentColor.copy(alpha = 0.2f),
            dividerLine = contentColor.copy(alpha = 0.12f),
            sliderBackground = contentColor.copy(alpha = 0.14f),
        )
    }
}

// 面板内沿用 Material 组件的内容跟随面板风格换色，避免逐个组件手工适配。
@Composable
fun PlayerPanelTokens.rememberPanelMaterialColorScheme(): ColorScheme {
    val baseScheme = MaterialTheme.colorScheme
    return remember(this, baseScheme) {
        baseScheme.copy(
            primary = accentColor,
            onPrimary = onAccentColor,
            primaryContainer = itemSelectedColor,
            onPrimaryContainer = itemSelectedContentColor,
            secondaryContainer = itemColor,
            onSecondaryContainer = itemContentColor,
            surface = containerColor,
            onSurface = contentColor,
            surfaceVariant = itemSelectedColor,
            onSurfaceVariant = secondaryContentColor,
            surfaceContainer = itemColor,
            surfaceContainerHigh = itemColor,
            surfaceContainerHighest = itemColor,
            outline = contentColor.copy(alpha = 0.3f),
        )
    }
}
