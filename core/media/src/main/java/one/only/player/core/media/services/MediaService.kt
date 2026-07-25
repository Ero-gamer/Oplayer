package one.only.player.core.media.services

import android.net.Uri
import androidx.activity.ComponentActivity

interface MediaService {
    fun initialize(activity: ComponentActivity)
    suspend fun deleteMedia(uris: List<Uri>): Boolean
    suspend fun renameMedia(uri: Uri, to: String): Boolean
    suspend fun getMoveTargetDirectory(path: String?): MediaMoveTargetDirectoryContent?
    suspend fun checkMoveSpace(
        videoUris: List<Uri>,
        folderPaths: List<String>,
        targetFolderPath: String,
    ): MediaMoveSpaceCheck?
    suspend fun moveMediaToRecycleBin(uri: Uri): MediaMoveResult?
    suspend fun moveMediaToFolder(
        uri: Uri,
        targetFolderPath: String,
        shouldCancel: () -> Boolean = { false },
        onProgress: (MediaCopyProgress) -> Unit = {},
    ): MediaMoveResult?
    suspend fun moveFolderToFolder(
        folderPath: String,
        targetFolderPath: String,
    ): MediaFolderMoveResult
    suspend fun restoreMediaFromRecycleBin(
        uri: Uri,
        originalPath: String,
        originalFileName: String,
    ): MediaMoveResult?
    suspend fun shareMedia(uris: List<Uri>)

    companion object {
        fun shouldAskSystemForDeleteConfirmation(): Boolean = true
    }
}

data class MediaMoveResult(
    val uri: Uri,
    val path: String,
    val parentPath: String,
    val fileName: String,
    val originalPath: String? = null,
)

data class MediaMoveTargetDirectoryContent(
    val currentDirectory: MediaMoveTargetDirectory?,
    val directories: List<MediaMoveTargetDirectory>,
    val canMoveHere: Boolean,
)

data class MediaMoveTargetDirectory(
    val name: String,
    val path: String,
    val storage: MediaStorageInfo? = null,
)

data class MediaStorageInfo(
    val name: String,
    val availableBytes: Long?,
    val totalBytes: Long?,
)

data class MediaMoveSpaceCheck(
    val requiredBytes: Long,
    val availableBytes: Long?,
    val hasEnoughSpace: Boolean,
)

data class MediaFolderMoveResult(
    val movedMedia: List<MediaMoveResult> = emptyList(),
    val isComplete: Boolean = false,
)

data class MediaCopyProgress(
    val copiedBytes: Long,
    val totalBytes: Long,
)
