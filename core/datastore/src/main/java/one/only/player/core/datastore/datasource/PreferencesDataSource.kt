package one.only.player.core.datastore.datasource

import kotlinx.coroutines.flow.Flow

interface PreferencesDataSource<T> {

    val bootstrapPreferences: T

    val preferences: Flow<T>

    suspend fun update(transform: suspend (T) -> T)
}
