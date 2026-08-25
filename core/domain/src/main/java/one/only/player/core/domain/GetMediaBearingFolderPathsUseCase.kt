package one.only.player.core.domain

import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import one.only.player.core.common.Dispatcher
import one.only.player.core.common.DispatcherType
import one.only.player.core.data.repository.MediaRepository
import one.only.player.core.data.repository.PreferencesRepository
import one.only.player.core.model.StoragePath

class GetMediaBearingFolderPathsUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val preferencesRepository: PreferencesRepository,
    @Dispatcher(DispatcherType.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    // 自身直接含可见媒体的目录，不含仅有子目录的中间层级
    operator fun invoke(): Flow<Set<StoragePath>> = combine(
        mediaRepository.getFoldersFlow(),
        preferencesRepository.applicationPreferences,
    ) { folders, preferences ->
        folders.mapNotNullTo(mutableSetOf()) { folder ->
            val path = StoragePath.of(folder.path)
            if (preferences.isPathExcluded(path)) return@mapNotNullTo null

            val visibleMedia = folder.mediaList.filterNot { video ->
                preferences.isRecycleBinEnabled && video.isInRecycleBin
            }
            if (visibleMedia.isEmpty()) return@mapNotNullTo null

            path
        }
    }.flowOn(defaultDispatcher)
}
