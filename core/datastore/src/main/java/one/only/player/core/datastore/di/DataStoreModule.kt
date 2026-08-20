package one.only.player.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import one.only.player.core.common.Dispatcher
import one.only.player.core.common.DispatcherType
import one.only.player.core.common.di.ApplicationScope
import one.only.player.core.datastore.applicationPreferencesDataStoreFile
import one.only.player.core.datastore.playerPreferencesDataStoreFile
import one.only.player.core.datastore.searchHistoryDataStoreFile
import one.only.player.core.datastore.serializer.ApplicationPreferencesSerializer
import one.only.player.core.datastore.serializer.PlayerPreferencesSerializer
import one.only.player.core.datastore.serializer.SearchHistorySerializer
import one.only.player.core.model.ApplicationPreferences
import one.only.player.core.model.PlayerPreferences
import one.only.player.core.model.SearchHistory

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAppPreferencesDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(DispatcherType.IO) ioDispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<ApplicationPreferences> = DataStoreFactory.create(
        serializer = ApplicationPreferencesSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { ApplicationPreferences() },
        scope = CoroutineScope(scope.coroutineContext + ioDispatcher),
        produceFile = context::applicationPreferencesDataStoreFile,
    )

    @Provides
    @Singleton
    fun providePlayerPreferencesDataStore(
        @ApplicationContext applicationContext: Context,
        @Dispatcher(DispatcherType.IO) ioDispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<PlayerPreferences> = DataStoreFactory.create(
        serializer = PlayerPreferencesSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { PlayerPreferences() },
        scope = CoroutineScope(scope.coroutineContext + ioDispatcher),
        produceFile = applicationContext::playerPreferencesDataStoreFile,
    )

    @Provides
    @Singleton
    fun provideSearchHistoryDataStore(
        @ApplicationContext applicationContext: Context,
        @Dispatcher(DispatcherType.IO) ioDispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<SearchHistory> = DataStoreFactory.create(
        serializer = SearchHistorySerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { SearchHistory() },
        scope = CoroutineScope(scope.coroutineContext + ioDispatcher),
        produceFile = applicationContext::searchHistoryDataStoreFile,
    )
}
