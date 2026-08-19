package one.only.player.feature.videopicker.screens.mediapicker

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import one.only.player.core.common.Dispatcher
import one.only.player.core.common.DispatcherType
import one.only.player.core.common.di.ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object MediaPickerCacheModule {

    @Provides
    @Singleton
    fun provideSnapshotCache(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        @Dispatcher(DispatcherType.IO) ioDispatcher: CoroutineDispatcher,
    ): MediaPickerSnapshotCache = MediaPickerSnapshotCache(
        cacheDir = File(context.cacheDir, "media_snapshots"),
        scope = scope,
        ioDispatcher = ioDispatcher,
    )
}
