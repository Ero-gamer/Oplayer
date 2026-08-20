package one.only.player.core.datastore

import android.content.Context
import androidx.datastore.dataStoreFile
import java.io.File
import one.only.player.core.datastore.serializer.ApplicationPreferencesSerializer
import one.only.player.core.datastore.serializer.PlayerPreferencesSerializer
import one.only.player.core.model.ApplicationPreferences
import one.only.player.core.model.PlayerPreferences

internal fun Context.applicationPreferencesDataStoreFile(): File = dataStoreFile(APP_PREFERENCES_DATASTORE_FILE)

internal fun Context.playerPreferencesDataStoreFile(): File = dataStoreFile(PLAYER_PREFERENCES_DATASTORE_FILE)

fun Context.readPersistedApplicationPreferences(): ApplicationPreferences = ApplicationPreferencesSerializer.readFromFile(applicationPreferencesDataStoreFile())

internal fun Context.readPersistedPlayerPreferences(): PlayerPreferences = PlayerPreferencesSerializer.readFromFile(playerPreferencesDataStoreFile())

internal fun Context.searchHistoryDataStoreFile(): File = dataStoreFile(SEARCH_HISTORY_DATASTORE_FILE)

private const val APP_PREFERENCES_DATASTORE_FILE = "app_preferences.json"
private const val PLAYER_PREFERENCES_DATASTORE_FILE = "player_preferences.json"
private const val SEARCH_HISTORY_DATASTORE_FILE = "search_history.json"
