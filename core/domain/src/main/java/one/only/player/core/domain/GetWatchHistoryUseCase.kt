package one.only.player.core.domain

import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import one.only.player.core.common.Dispatcher
import one.only.player.core.common.DispatcherType
import one.only.player.core.data.repository.MediaRepository
import one.only.player.core.model.Video

class GetWatchHistoryUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    @Dispatcher(DispatcherType.Default) private val defaultDispatcher: CoroutineDispatcher,
) {

    operator fun invoke(): Flow<List<Video>> = mediaRepository.getVideosFlow().map { videos ->
        videos.filter { video ->
            !video.isInRecycleBin && video.lastPlayedAt != null
        }.sortedByDescending { video ->
            video.lastPlayedAt?.time ?: 0L
        }
    }.flowOn(defaultDispatcher)
}
