package one.only.player

import android.content.Context
import one.only.player.core.common.AppThemeMode
import one.only.player.core.datastore.readPersistedApplicationPreferences
import one.only.player.core.model.ApplicationPreferences
import one.only.player.core.model.ThemeConfig

internal object StartupPreferencesCache {

    private var initialPreferences: ApplicationPreferences? = null

    fun initialize(context: Context): ApplicationPreferences = readPersistedStartupPreferences(context).also {
        initialPreferences = it
    }

    fun consume(context: Context): ApplicationPreferences {
        // 只复用首次 Activity 创建，后续重建仍读取最新偏好。
        val preferences = initialPreferences ?: return readPersistedStartupPreferences(context)
        initialPreferences = null
        return preferences
    }
}

internal fun ThemeConfig.toAppThemeMode(): AppThemeMode = when (this) {
    ThemeConfig.SYSTEM -> AppThemeMode.FOLLOW_SYSTEM
    ThemeConfig.OFF -> AppThemeMode.LIGHT
    ThemeConfig.ON -> AppThemeMode.DARK
}

internal fun readPersistedStartupPreferences(context: Context): ApplicationPreferences = context.readPersistedApplicationPreferences()
