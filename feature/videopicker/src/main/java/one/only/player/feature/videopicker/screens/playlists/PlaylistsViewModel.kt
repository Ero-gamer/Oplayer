package one.only.player.feature.videopicker.screens.playlists

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import one.only.player.core.data.repository.MediaRepository
import one.only.player.core.data.repository.PlaylistRepository
import one.only.player.core.data.repository.PreferencesRepository
import one.only.player.core.model.ApplicationPreferences
import one.only.player.core.model.PlayerPreferences
import one.only.player.core.model.Playlist
import one.only.player.core.model.PlaylistItem
import one.only.player.core.model.Video

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    mediaRepository: MediaRepository,
    preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val currentPlaylistId = MutableStateFlow<Long?>(null)
    private val openTarget = MutableStateFlow<PlaylistOpenTarget?>(null)

    val uiState = combine(
        playlistRepository.observePlaylists(),
        currentPlaylistId.flatMapLatest { playlistId ->
            if (playlistId == null) {
                flowOf(emptyList())
            } else {
                combine(
                    playlistRepository.observeItems(playlistId),
                    mediaRepository.getVideosFlow(),
                ) { items, videos ->
                    val videoByUri = videos.associateBy(Video::uriString)
                    val videoByPath = videos.associateBy(Video::path)
                    items.map { item ->
                        val video = videoByUri[item.mediaUri]
                            ?: item.mediaPath.takeIf { item.mediaUri.isBlank() }?.let(videoByPath::get)
                        item.copy(video = video)
                    }
                }
            }
        },
        searchQuery,
        currentPlaylistId,
        combine(
            openTarget,
            preferencesRepository.applicationPreferences,
            preferencesRepository.playerPreferences,
            ::Triple,
        ),
    ) { playlists, items, query, playlistId, extras ->
        val (target, preferences, playerPreferences) = extras
        val normalizedQuery = query.trim()
        val visiblePlaylists = if (normalizedQuery.isBlank()) {
            playlists
        } else {
            playlists.filter { playlist -> playlist.title.contains(normalizedQuery, ignoreCase = true) }
        }
        val visibleItems = if (normalizedQuery.isBlank()) {
            items
        } else {
            items.filter { item ->
                item.title.contains(normalizedQuery, ignoreCase = true) ||
                    item.video?.displayName.orEmpty().contains(normalizedQuery, ignoreCase = true)
            }
        }
        PlaylistsUiState(
            playlists = visiblePlaylists,
            items = visibleItems,
            allItems = items,
            searchQuery = query,
            currentPlaylistId = playlistId,
            currentTitle = playlists.firstOrNull { it.id == playlistId }?.title,
            openTarget = target,
            preferences = preferences,
            playerPreferences = playerPreferences,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaylistsUiState(),
    )

    fun onEvent(event: PlaylistsUiEvent) {
        when (event) {
            is PlaylistsUiEvent.UpdateSearchQuery -> searchQuery.value = event.value
            is PlaylistsUiEvent.OpenPlaylist -> currentPlaylistId.value = event.id
            PlaylistsUiEvent.NavigateParent -> currentPlaylistId.value = null
            is PlaylistsUiEvent.Create -> create(event.title)
            is PlaylistsUiEvent.Rename -> rename(event.id, event.title)
            is PlaylistsUiEvent.Delete -> delete(event.id)
            is PlaylistsUiEvent.RemoveItem -> removeItem(event.item)
            is PlaylistsUiEvent.PlayPlaylist -> playPlaylist(event.startItem)
            PlaylistsUiEvent.ConsumeOpenTarget -> openTarget.value = null
        }
    }

    private fun create(title: String) {
        viewModelScope.launch {
            playlistRepository.create(title)
        }
    }

    private fun rename(
        id: Long,
        title: String,
    ) {
        viewModelScope.launch {
            playlistRepository.rename(id, title)
        }
    }

    private fun delete(id: Long) {
        viewModelScope.launch {
            playlistRepository.delete(id)
            if (currentPlaylistId.value == id) {
                currentPlaylistId.value = null
            }
        }
    }

    private fun removeItem(item: PlaylistItem) {
        viewModelScope.launch {
            playlistRepository.removeItems(listOf(item.id))
        }
    }

    private fun playPlaylist(startItem: PlaylistItem?) {
        val videos = uiState.value.allItems.mapNotNull(PlaylistItem::video)
        if (videos.isEmpty()) return
        if (startItem != null && startItem.video == null) return
        val startVideo = startItem?.video ?: videos.first()
        val startIndex = videos.indexOfFirst { video -> video.uriString == startVideo.uriString }
            .takeIf { it >= 0 } ?: 0
        val orderedVideos = videos.drop(startIndex) + videos.take(startIndex)
        openTarget.value = PlaylistOpenTarget(
            video = startVideo,
            playlist = orderedVideos.map { video -> video.uriString.toUri() },
        )
    }
}

@Stable
data class PlaylistsUiState(
    val playlists: List<Playlist> = emptyList(),
    val items: List<PlaylistItem> = emptyList(),
    val allItems: List<PlaylistItem> = emptyList(),
    val searchQuery: String = "",
    val currentPlaylistId: Long? = null,
    val currentTitle: String? = null,
    val openTarget: PlaylistOpenTarget? = null,
    val preferences: ApplicationPreferences = ApplicationPreferences(),
    val playerPreferences: PlayerPreferences = PlayerPreferences(),
)

sealed interface PlaylistsUiEvent {
    data class UpdateSearchQuery(val value: String) : PlaylistsUiEvent
    data class OpenPlaylist(val id: Long) : PlaylistsUiEvent
    data object NavigateParent : PlaylistsUiEvent
    data class Create(val title: String) : PlaylistsUiEvent
    data class Rename(
        val id: Long,
        val title: String,
    ) : PlaylistsUiEvent
    data class Delete(val id: Long) : PlaylistsUiEvent
    data class RemoveItem(val item: PlaylistItem) : PlaylistsUiEvent
    data class PlayPlaylist(val startItem: PlaylistItem? = null) : PlaylistsUiEvent
    data object ConsumeOpenTarget : PlaylistsUiEvent
}

data class PlaylistOpenTarget(
    val video: Video,
    val playlist: List<Uri>,
)
