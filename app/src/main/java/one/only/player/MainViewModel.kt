package one.only.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import one.only.player.core.data.repository.AppUpdateChecker
import one.only.player.core.data.repository.AppUpdateInfo
import one.only.player.core.data.repository.PreferencesRepository
import one.only.player.core.model.ApplicationPreferences

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val appUpdateChecker: AppUpdateChecker,
) : ViewModel() {

    private val initialPreferences = preferencesRepository.applicationPreferences.value

    val currentPreferences: ApplicationPreferences
        get() = preferencesRepository.applicationPreferences.value

    val uiState = preferencesRepository.applicationPreferences.map { preferences ->
        MainActivityUiState.Success(preferences)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainActivityUiState.Success(initialPreferences),
    )

    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo = _updateInfo.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.applicationPreferences.value
            if (!prefs.shouldCheckForUpdatesOnStartup) return@launch
            val versionName = context.packageManager
                .getPackageInfo(context.packageName, 0).versionName ?: return@launch
            val info = appUpdateChecker.checkForUpdate(versionName)
            _updateInfo.update { info }
        }
    }

    fun dismissUpdate() {
        _updateInfo.update { null }
    }
}

sealed interface MainActivityUiState {
    object Loading : MainActivityUiState
    data class Success(val preferences: ApplicationPreferences) : MainActivityUiState
}
