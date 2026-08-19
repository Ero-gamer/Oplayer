package one.only.player.core.domain

import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import one.only.player.core.common.Dispatcher
import one.only.player.core.common.DispatcherType
import one.only.player.core.data.repository.PreferencesRepository
import one.only.player.core.model.Folder
import one.only.player.core.model.MediaViewMode

class GetSortedMediaUseCase @Inject constructor(
    private val getSortedVideosUseCase: GetSortedVideosUseCase,
    private val getSortedFoldersUseCase: GetSortedFoldersUseCase,
    private val getSortedFolderTreeUseCase: GetSortedFolderTreeUseCase,
    private val preferencesRepository: PreferencesRepository,
    @Dispatcher(DispatcherType.Default) private val defaultDispatcher: CoroutineDispatcher,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        folderPath: String? = null,
        isRecycleBinOnly: Boolean = false,
    ): Flow<Folder?> {
        if (isRecycleBinOnly) {
            return getSortedVideosUseCase(isRecycleBinOnly = true).map { videos ->
                Folder.rootFolder.copy(
                    mediaList = videos,
                    folderList = emptyList(),
                )
            }.flowOn(defaultDispatcher)
        }

        return preferencesRepository.applicationPreferences
            .map { preferences -> preferences.mediaViewMode }
            .distinctUntilChanged()
            .flatMapLatest { mediaViewMode ->
                when (mediaViewMode) {
                    MediaViewMode.FOLDER_TREE -> getSortedFolderTreeUseCase(folderPath)
                    MediaViewMode.FOLDERS -> if (folderPath == null) {
                        getSortedFoldersUseCase().map { folders ->
                            Folder.rootFolder.copy(
                                mediaList = emptyList(),
                                folderList = folders,
                            )
                        }
                    } else {
                        getSortedVideosUseCase(folderPath).map { videos ->
                            val file = File(folderPath)
                            Folder(
                                name = file.name,
                                path = file.path,
                                dateModified = file.lastModified(),
                                mediaList = videos,
                                folderList = emptyList(),
                            )
                        }
                    }

                    MediaViewMode.VIDEOS -> getSortedVideosUseCase(folderPath).map { videos ->
                        Folder.rootFolder.copy(
                            mediaList = videos,
                            folderList = emptyList(),
                        )
                    }
                }
            }
            .flowOn(defaultDispatcher)
    }
}
