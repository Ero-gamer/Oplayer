package one.only.player.core.datastore.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import one.only.player.core.common.Logger
import one.only.player.core.datastore.readPersistedPlayerPreferences
import one.only.player.core.model.PlayerPreferences

class PlayerPreferencesDataSource @Inject constructor(
    @ApplicationContext context: Context,
    private val preferencesDataStore: DataStore<PlayerPreferences>,
) : PreferencesDataSource<PlayerPreferences> {

    companion object {
        private const val TAG = "PlayerPreferencesDataSource"
    }

    override val bootstrapPreferences = context.readPersistedPlayerPreferences()
    override val preferences = preferencesDataStore.data

    override suspend fun update(transform: suspend (PlayerPreferences) -> PlayerPreferences) {
        try {
            preferencesDataStore.updateData(transform)
        } catch (ioException: Exception) {
            Logger.error(TAG, "Failed to update app preferences: $ioException")
        }
    }
}
