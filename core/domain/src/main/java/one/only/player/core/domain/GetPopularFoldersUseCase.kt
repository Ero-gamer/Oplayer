package one.only.player.core.domain

import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import one.only.player.core.common.Dispatcher
import one.only.player.core.common.DispatcherType
import one.only.player.core.model.Folder

class GetPopularFoldersUseCase @Inject constructor(
    private val getSortedFoldersUseCase: GetSortedFoldersUseCase,
    @Dispatcher(DispatcherType.Default) private val defaultDispatcher: CoroutineDispatcher,
) {

    operator fun invoke(limit: Int = 5): Flow<List<Folder>> = getSortedFoldersUseCase().map { folders ->
        folders.sortedWith(
            compareByDescending<Folder> { folder ->
                folder.allMediaList.count { it.lastPlayedAt != null }
            }.thenByDescending { folder ->
                folder.recentlyPlayedVideo?.lastPlayedAt?.time ?: 0L
            }.thenByDescending { folder ->
                folder.mediaList.size
            },
        ).take(limit)
    }.flowOn(defaultDispatcher)
}
