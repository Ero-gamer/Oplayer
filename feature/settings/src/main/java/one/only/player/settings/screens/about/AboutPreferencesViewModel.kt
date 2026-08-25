package one.only.player.settings.screens.about

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import one.only.player.core.data.repository.AppUpdateChecker
import one.only.player.core.data.repository.AppUpdateResult
import one.only.player.core.data.repository.PreferencesRepository

@HiltViewModel
class AboutPreferencesViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val appUpdateChecker: AppUpdateChecker,
) : ViewModel() {

    private val uiStateInternal = MutableStateFlow(
        AboutPreferencesUiState(
            shouldCheckForUpdatesOnStartup = preferencesRepository.applicationPreferences.value.shouldCheckForUpdatesOnStartup,
        ),
    )
    val uiState = uiStateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.applicationPreferences.collect { prefs ->
                uiStateInternal.update {
                    it.copy(shouldCheckForUpdatesOnStartup = prefs.shouldCheckForUpdatesOnStartup)
                }
            }
        }
    }

    fun onEvent(event: AboutPreferencesUiEvent) {
        when (event) {
            is AboutPreferencesUiEvent.CheckForUpdates -> checkForUpdates(event.currentVersion)
            AboutPreferencesUiEvent.ToggleCheckOnStartup -> toggleCheckOnStartup()
        }
    }

    private fun checkForUpdates(currentVersion: String) {
        if (uiStateInternal.value.updateState == UpdateState.Checking) return
        uiStateInternal.update { it.copy(updateState = UpdateState.Checking) }

        viewModelScope.launch {
            val result = when (val checkResult = appUpdateChecker.checkForUpdate(currentVersion)) {
                is AppUpdateResult.Available -> UpdateState.UpdateAvailable(
                    latestVersion = checkResult.info.latestVersion,
                    releaseUrl = checkResult.info.releaseUrl,
                )
                AppUpdateResult.UpToDate -> UpdateState.UpToDate
                AppUpdateResult.Failed -> UpdateState.Error
            }
            uiStateInternal.update { it.copy(updateState = result) }
        }
    }

    private fun toggleCheckOnStartup() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(shouldCheckForUpdatesOnStartup = !it.shouldCheckForUpdatesOnStartup)
            }
        }
    }
}

@Stable
data class AboutPreferencesUiState(
    val updateState: UpdateState = UpdateState.Idle,
    val shouldCheckForUpdatesOnStartup: Boolean = false,
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class UpdateAvailable(val latestVersion: String, val releaseUrl: String) : UpdateState
    data object Error : UpdateState
}

sealed interface AboutPreferencesUiEvent {
    data class CheckForUpdates(val currentVersion: String) : AboutPreferencesUiEvent
    data object ToggleCheckOnStartup : AboutPreferencesUiEvent
}
