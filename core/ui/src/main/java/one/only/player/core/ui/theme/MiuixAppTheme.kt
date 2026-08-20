package one.only.player.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

// 应用主题入口，Miuix 为主，Material3 跟随同一色板
@Composable
fun OnlyPlayerTheme(
    shouldUseDarkTheme: Boolean = isSystemInDarkTheme(),
    shouldUseDynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    // 动态取色只跟随系统壁纸，Android 12 以下退回内置配色
    val shouldUseMonet = shouldUseDynamicColor && supportsDynamicTheming()
    val miuixController = remember(shouldUseDarkTheme, shouldUseMonet) {
        ThemeController(
            colorSchemeMode = when {
                shouldUseMonet && shouldUseDarkTheme -> ColorSchemeMode.MonetDark
                shouldUseMonet -> ColorSchemeMode.MonetLight
                shouldUseDarkTheme -> ColorSchemeMode.Dark
                else -> ColorSchemeMode.Light
            },
            isDark = shouldUseDarkTheme,
        )
    }

    val context = LocalContext.current
    MiuixTheme(controller = miuixController) {
        // Material3 组件跟随同一套 Miuix 色板，避免迁移期出现两套配色。
        val materialScheme = if (shouldUseMonet) {
            if (shouldUseDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            MiuixTheme.colorScheme.toMaterialColorScheme(shouldUseDarkTheme)
        }
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content,
        )
    }
}

private fun Colors.toMaterialColorScheme(isDark: Boolean) = (if (isDark) darkColorScheme() else lightColorScheme()).copy(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiaryContainer,
    onTertiary = onTertiaryContainer,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,
    background = background,
    onBackground = onBackground,
    surface = surface,
    surfaceTint = primary,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariantSummary,
    outline = outline,
    surfaceContainer = surfaceContainer,
    surfaceContainerHigh = surfaceContainerHigh,
    surfaceContainerHighest = surfaceContainerHighest,
)
