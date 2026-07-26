package one.only.player.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import one.only.player.core.model.ThemeColorSpec as ModelColorSpec
import one.only.player.core.model.ThemePaletteStyle as ModelPaletteStyle
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

// 应用主题入口，Miuix 为主，Material3 跟随同一色板
@Composable
fun OnlyPlayerTheme(
    shouldUseDarkTheme: Boolean = isSystemInDarkTheme(),
    shouldUseDynamicColor: Boolean = true,
    shouldUseSystemDynamicColor: Boolean = true,
    seedColor: Long = DEFAULT_SEED_COLOR,
    paletteStyle: ModelPaletteStyle = ModelPaletteStyle.TONAL_SPOT,
    colorSpec: ModelColorSpec = ModelColorSpec.SPEC_2025,
    content: @Composable () -> Unit,
) {
    val shouldUseMonet = shouldUseDynamicColor &&
        (supportsDynamicTheming() || !shouldUseSystemDynamicColor)
    val miuixController = remember(
        shouldUseDarkTheme,
        shouldUseMonet,
        shouldUseSystemDynamicColor,
        seedColor,
        paletteStyle,
        colorSpec,
    ) {
        ThemeController(
            colorSchemeMode = when {
                shouldUseMonet && shouldUseDarkTheme -> ColorSchemeMode.MonetDark
                shouldUseMonet -> ColorSchemeMode.MonetLight
                shouldUseDarkTheme -> ColorSchemeMode.Dark
                else -> ColorSchemeMode.Light
            },
            keyColor = Color(seedColor).takeIf {
                shouldUseMonet && !shouldUseSystemDynamicColor
            },
            colorSpec = colorSpec.toMiuix(),
            paletteStyle = paletteStyle.toMiuix(),
            isDark = shouldUseDarkTheme,
        )
    }

    val context = LocalContext.current
    MiuixTheme(controller = miuixController) {
        // Material3 组件跟随同一套 Miuix 色板，避免迁移期出现两套配色。
        val materialScheme = when {
            shouldUseMonet && shouldUseSystemDynamicColor ->
                if (shouldUseDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

            shouldUseMonet -> rememberDynamicColorScheme(
                seedColor = Color(seedColor),
                isDark = shouldUseDarkTheme,
                isAmoled = false,
                style = paletteStyle.toMaterialKolor(),
                specVersion = colorSpec.toMaterialKolorSpec(),
            )

            else -> MiuixTheme.colorScheme.toMaterialColorScheme(shouldUseDarkTheme)
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

private fun ModelPaletteStyle.toMiuix(): ThemePaletteStyle = when (this) {
    ModelPaletteStyle.TONAL_SPOT -> ThemePaletteStyle.TonalSpot
    ModelPaletteStyle.NEUTRAL -> ThemePaletteStyle.Neutral
    ModelPaletteStyle.VIBRANT -> ThemePaletteStyle.Vibrant
    ModelPaletteStyle.EXPRESSIVE -> ThemePaletteStyle.Expressive
    ModelPaletteStyle.RAINBOW -> ThemePaletteStyle.Rainbow
    ModelPaletteStyle.FRUIT_SALAD -> ThemePaletteStyle.FruitSalad
    ModelPaletteStyle.MONOCHROME -> ThemePaletteStyle.Monochrome
    ModelPaletteStyle.FIDELITY -> ThemePaletteStyle.Fidelity
    ModelPaletteStyle.CONTENT -> ThemePaletteStyle.Content
}

private fun ModelColorSpec.toMiuix(): ThemeColorSpec = when (this) {
    ModelColorSpec.SPEC_2021 -> ThemeColorSpec.Spec2021
    ModelColorSpec.SPEC_2025 -> ThemeColorSpec.Spec2025
}

private fun ModelPaletteStyle.toMaterialKolor(): PaletteStyle = when (this) {
    ModelPaletteStyle.TONAL_SPOT -> PaletteStyle.TonalSpot
    ModelPaletteStyle.NEUTRAL -> PaletteStyle.Neutral
    ModelPaletteStyle.VIBRANT -> PaletteStyle.Vibrant
    ModelPaletteStyle.EXPRESSIVE -> PaletteStyle.Expressive
    ModelPaletteStyle.RAINBOW -> PaletteStyle.Rainbow
    ModelPaletteStyle.FRUIT_SALAD -> PaletteStyle.FruitSalad
    ModelPaletteStyle.MONOCHROME -> PaletteStyle.Monochrome
    ModelPaletteStyle.FIDELITY -> PaletteStyle.Fidelity
    ModelPaletteStyle.CONTENT -> PaletteStyle.Content
}

private fun ModelColorSpec.toMaterialKolorSpec(): ColorSpec.SpecVersion = when (this) {
    ModelColorSpec.SPEC_2021 -> ColorSpec.SpecVersion.SPEC_2021
    ModelColorSpec.SPEC_2025 -> ColorSpec.SpecVersion.SPEC_2025
}

const val DEFAULT_SEED_COLOR: Long = 0xFF6750A4
