package one.only.player.feature.videopicker.screens.history

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import one.only.player.core.data.repository.MediaRepository
import one.only.player.core.data.repository.PreferencesRepository
import one.only.player.core.domain.GetWatchHistoryUseCase
import one.only.player.core.model.ApplicationPreferences
import one.only.player.core.model.PlayerPreferences
import one.only.player.core.model.Video

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getWatchHistoryUseCase: GetWatchHistoryUseCase,
    private val mediaRepository: MediaRepository,
    preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val initialPreferences = preferencesRepository.applicationPreferences.value
    private val initialPlayerPreferences = preferencesRepository.playerPreferences.value

    val uiState = combine(
        getWatchHistoryUseCase(),
        searchQuery,
        preferencesRepository.applicationPreferences,
        preferencesRepository.playerPreferences,
    ) { videos, query, preferences, playerPreferences ->
        val normalizedQuery = query.trim()
        val visibleVideos = if (normalizedQuery.isBlank()) {
            videos
        } else {
            videos.filter { video ->
                video.displayName.contains(normalizedQuery, ignoreCase = true) ||
                    video.nameWithExtension.contains(normalizedQuery, ignoreCase = true) ||
                    File(video.parentPath).name.contains(normalizedQuery, ignoreCase = true)
            }
        }
        HistoryUiState(
            videos = visibleVideos,
            searchQuery = query,
            preferences = preferences,
            playerPreferences = playerPreferences,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(
            preferences = initialPreferences,
            playerPreferences = initialPlayerPreferences,
        ),
    )

    fun onEvent(event: HistoryUiEvent) {
        when (event) {
            is HistoryUiEvent.UpdateSearchQuery -> searchQuery.value = event.value
            is HistoryUiEvent.Remove -> remove(event.video)
            HistoryUiEvent.Clear -> clear()
        }
    }

    private fun remove(video: Video) {
        viewModelScope.launch {
            mediaRepository.clearMediumLastPlayedTime(video.uriString)
        }
    }

    private fun clear() {
        viewModelScope.launch {
            mediaRepository.clearAllLastPlayedTimes()
        }
    }
}

@Stable
data class HistoryUiState(
    val videos: List<Video> = emptyList(),
    val searchQuery: String = "",
    val preferences: ApplicationPreferences = ApplicationPreferences(),
    val playerPreferences: PlayerPreferences = PlayerPreferences(),
)

sealed interface HistoryUiEvent {
    data class UpdateSearchQuery(val value: String) : HistoryUiEvent
    data class Remove(val video: Video) : HistoryUiEvent
    data object Clear : HistoryUiEvent
}
