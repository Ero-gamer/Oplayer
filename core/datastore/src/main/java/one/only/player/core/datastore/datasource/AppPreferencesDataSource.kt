package one.only.player.core.datastore.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import one.only.player.core.common.Logger
import one.only.player.core.datastore.readPersistedApplicationPreferences
import one.only.player.core.model.ApplicationPreferences

class AppPreferencesDataSource @Inject constructor(
    @ApplicationContext context: Context,
    private val appPreferences: DataStore<ApplicationPreferences>,
) : PreferencesDataSource<ApplicationPreferences> {

    companion object {
        private const val TAG = "AppPreferencesDataSource"
    }

    override val bootstrapPreferences = context.readPersistedApplicationPreferences()
    override val preferences = appPreferences.data

    override suspend fun update(
        transform: suspend (ApplicationPreferences) -> ApplicationPreferences,
    ) {
        try {
            appPreferences.updateData(transform)
        } catch (ioException: Exception) {
            Logger.error(TAG, "Failed to update app preferences: $ioException")
        }
    }
}
