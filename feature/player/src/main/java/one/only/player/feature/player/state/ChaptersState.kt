package one.only.player.feature.player.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.session.MediaController
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import one.only.player.feature.player.extensions.seekToRequestedPosition
import one.only.player.feature.player.model.VideoChapter
import one.only.player.feature.player.model.currentChapterIndex
import one.only.player.feature.player.model.extractVideoChapters
import one.only.player.feature.player.service.getVideoChapters

@Composable
fun rememberChaptersState(player: Player): ChaptersState {
    val chaptersState = remember(player) { ChaptersState(player) }
    LaunchedEffect(chaptersState) { chaptersState.observe() }
    return chaptersState
}

@Stable
class ChaptersState(
    private val player: Player,
) {
    var chapters: List<VideoChapter> by mutableStateOf(emptyList())
        private set

    val mediaUri: android.net.Uri?
        get() = player.currentMediaItem?.localConfiguration?.uri

    fun seekTo(index: Int): VideoChapter? {
        val chapter = chapters.getOrNull(index) ?: return null
        player.seekToRequestedPosition(chapter.startTimeMs)
        return chapter
    }

    fun seekToNext(positionMs: Long = player.currentPosition): VideoChapter? {
        val currentIndex = chapters.currentChapterIndex(positionMs)
        return seekTo(currentIndex?.plus(1) ?: 0)
    }

    fun seekToPrevious(positionMs: Long = player.currentPosition): VideoChapter? {
        val currentIndex = chapters.currentChapterIndex(positionMs) ?: return null
        return seekTo(currentIndex - 1)
    }

    suspend fun observe() {
        coroutineScope {
            var updateJob: Job? = null
            updateChapters()
            player.listen { events ->
                if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                    chapters = emptyList()
                }
                if (events.containsAny(
                        Player.EVENT_TRACKS_CHANGED,
                        Player.EVENT_MEDIA_ITEM_TRANSITION,
                        Player.EVENT_TIMELINE_CHANGED,
                    )
                ) {
                    updateJob?.cancel()
                    updateJob = launch { updateChapters() }
                }
            }
        }
    }

    private suspend fun updateChapters() {
        chapters = when (player) {
            is MediaController -> player.getVideoChapters()
            else -> player.extractVideoChapters()
        }
    }
}
