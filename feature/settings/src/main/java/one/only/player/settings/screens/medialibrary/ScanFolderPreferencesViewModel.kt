package one.only.player.settings.screens.medialibrary

import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import one.only.player.core.common.Logger
import one.only.player.core.common.extensions.toPrivateLogSummary
import one.only.player.core.data.repository.PreferencesRepository
import one.only.player.core.model.ApplicationPreferences

@HiltViewModel
class ScanFolderPreferencesViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val uiStateInternal = MutableStateFlow(
        ScanFolderPreferencesUiState(
            preferences = preferencesRepository.applicationPreferences.value,
        ),
    )
    val uiState: StateFlow<ScanFolderPreferencesUiState> = uiStateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.applicationPreferences.collect { preferences ->
                uiStateInternal.update { currentState ->
                    currentState.copy(preferences = preferences)
                }
            }
        }
    }

    fun onEvent(event: ScanFolderPreferencesUiEvent) {
        when (event) {
            is ScanFolderPreferencesUiEvent.AddFolder -> addScanFolder(event.treeUri)
            is ScanFolderPreferencesUiEvent.RemoveFolder -> removeScanFolder(event.path)
        }
    }

    private fun addScanFolder(treeUri: Uri) {
        val path = treeUri.toFolderPathOrNull()
        if (path == null) {
            Logger.info(TAG, "Ignored unsupported scan folder uri: ${treeUri.toPrivateLogSummary()}")
            return
        }

        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(
                    scanFolders = (it.scanFolders + path).distinct().sorted(),
                )
            }
        }
    }

    private fun removeScanFolder(path: String) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(scanFolders = it.scanFolders - path)
            }
        }
    }

    // 仅支持系统外部存储 DocumentsProvider 的目录，其余来源无法映射到文件路径
    private fun Uri.toFolderPathOrNull(): String? {
        if (authority != EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY) return null

        val documentId = runCatching { DocumentsContract.getTreeDocumentId(this) }.getOrNull() ?: return null
        val volumeId = documentId.substringBefore(':')
        val relativePath = documentId.substringAfter(':', missingDelimiterValue = "").trim('/')
        val rootPath = if (volumeId.equals(PRIMARY_VOLUME_ID, ignoreCase = true)) {
            Environment.getExternalStorageDirectory().path
        } else {
            "/storage/$volumeId"
        }
        val folderPath = if (relativePath.isBlank()) rootPath else "$rootPath/$relativePath"
        return folderPath.takeIf { File(it).isDirectory }
    }

    companion object {
        private const val TAG = "ScanFolderPreferences"
        private const val EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY = "com.android.externalstorage.documents"
        private const val PRIMARY_VOLUME_ID = "primary"
    }
}

data class ScanFolderPreferencesUiState(
    val preferences: ApplicationPreferences = ApplicationPreferences(),
)

sealed interface ScanFolderPreferencesUiEvent {
    data class AddFolder(val treeUri: Uri) : ScanFolderPreferencesUiEvent
    data class RemoveFolder(val path: String) : ScanFolderPreferencesUiEvent
}
