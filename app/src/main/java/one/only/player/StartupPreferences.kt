package one.only.player

import java.io.File
import one.only.player.core.common.AppThemeMode
import one.only.player.core.model.ThemeConfig

internal data class PersistedStartupPreferences(
    val themeConfig: ThemeConfig,
    val shouldEnablePredictiveBack: Boolean,
    val shouldHideInRecents: Boolean,
    val shouldUseDynamicColors: Boolean,
)

internal object StartupPreferencesCache {

    private var initialPreferences: PersistedStartupPreferences? = null

    fun initialize(dataDir: String): PersistedStartupPreferences = readPersistedStartupPreferences(dataDir).also {
        initialPreferences = it
    }

    fun consume(dataDir: String): PersistedStartupPreferences {
        // 只复用首次 Activity 创建，后续重建仍读取最新偏好。
        val preferences = initialPreferences ?: return readPersistedStartupPreferences(dataDir)
        initialPreferences = null
        return preferences
    }
}

internal fun ThemeConfig.toAppThemeMode(): AppThemeMode = when (this) {
    ThemeConfig.SYSTEM -> AppThemeMode.FOLLOW_SYSTEM
    ThemeConfig.OFF -> AppThemeMode.LIGHT
    ThemeConfig.ON -> AppThemeMode.DARK
}

internal fun readPersistedStartupPreferences(dataDir: String): PersistedStartupPreferences {
    val preferencesFile = File(dataDir, "files/datastore/app_preferences.json")
    val rawPreferences = preferencesFile
        .takeIf(File::exists)
        ?.let { file -> runCatching(file::readText).getOrNull() }
    val themeConfig = rawPreferences
        ?.let(THEME_CONFIG_PATTERN::find)
        ?.groupValues
        ?.getOrNull(1)
        ?.let { rawConfig -> ThemeConfig.entries.firstOrNull { it.name == rawConfig } }
        ?: ThemeConfig.SYSTEM

    return PersistedStartupPreferences(
        themeConfig = themeConfig,
        shouldEnablePredictiveBack = rawPreferences
            ?.let(PREDICTIVE_BACK_PATTERN::find)
            ?.groupValues
            ?.getOrNull(1)
            ?.toBooleanStrictOrNull()
            ?: false,
        shouldHideInRecents = rawPreferences
            ?.let(HIDE_IN_RECENTS_PATTERN::find)
            ?.groupValues
            ?.getOrNull(1)
            ?.toBooleanStrictOrNull()
            ?: false,
        shouldUseDynamicColors = rawPreferences
            ?.let(DYNAMIC_COLORS_PATTERN::find)
            ?.groupValues
            ?.getOrNull(1)
            ?.toBooleanStrictOrNull()
            ?: true,
    )
}

private val THEME_CONFIG_PATTERN = "\"themeConfig\"\\s*:\\s*\"([A-Z_]+)\"".toRegex()
private val PREDICTIVE_BACK_PATTERN = "\"shouldEnablePredictiveBack\"\\s*:\\s*(true|false)".toRegex()
private val HIDE_IN_RECENTS_PATTERN = "\"shouldHideInRecents\"\\s*:\\s*(true|false)".toRegex()
private val DYNAMIC_COLORS_PATTERN = "\"shouldUseDynamicColors\"\\s*:\\s*(true|false)".toRegex()
