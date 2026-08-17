package one.only.player.debug

import android.content.Context
import android.os.Bundle
import dagger.hilt.android.EntryPointAccessors
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import one.only.player.core.common.extensions.canonicalPathOrSelf
import one.only.player.core.model.Playlist
import one.only.player.core.model.PlaylistItem
import one.only.player.core.model.Video

internal fun Context.runPlaylistCommand(
    action: String,
    target: String?,
    extras: Bundle?,
): Bundle {
    val command = "playlist.$action"
    val entryPoint = EntryPointAccessors.fromApplication(
        applicationContext,
        DebugCommandEntryPoint::class.java,
    )
    val value = extras.withTarget(target)

    return runCatching {
        runBlocking { entryPoint.runPlaylistAction(action, value) }
    }.getOrElse {
        debugResult(
            isOk = false,
            message = it.message ?: "Failed to handle playlist action: $action",
            command = command,
            target = action,
        )
    }
}

private suspend fun DebugCommandEntryPoint.runPlaylistAction(
    action: String,
    extras: Bundle,
): Bundle {
    val command = "playlist.$action"
    return when (action) {
        "create" -> {
            val name = extras.getString(EXTRA_VALUE)?.takeIf { it.isNotBlank() }
                ?: extras.getString(EXTRA_NAME)?.takeIf { it.isNotBlank() }
                ?: "Playlist"
            val playlistId = playlistRepository().create(name)
            debugResult(
                isOk = true,
                message = "Created playlist: $playlistId",
                command = command,
                target = action,
                value = playlistId.toString(),
            )
        }
        "add" -> {
            val playlistId = extras.requiredLong(EXTRA_ID)
            val addedCount = addPlaylistItems(playlistId, extras)
            debugResult(
                isOk = true,
                message = "Added $addedCount item(s) to playlist: $playlistId",
                command = command,
                target = action,
                value = addedCount.toString(),
            )
        }
        "list" -> {
            val playlists = playlistRepository().observePlaylists().first()
            debugResult(
                isOk = true,
                message = playlists.joinToString(separator = "; ") { playlist -> playlist.debugSummary() },
                command = command,
                target = action,
                value = playlists.size.toString(),
            )
        }
        "items" -> {
            val playlistId = extras.requiredTargetLong(EXTRA_ID)
            val items = playlistRepository().getItems(playlistId)
            debugResult(
                isOk = true,
                message = items.joinToString(separator = "; ") { item -> item.debugSummary() },
                command = command,
                target = action,
                value = items.size.toString(),
            )
        }
        "delete" -> {
            val playlistId = extras.requiredTargetLong(EXTRA_ID)
            playlistRepository().delete(playlistId)
            debugResult(
                isOk = true,
                message = "Deleted playlist: $playlistId",
                command = command,
                target = action,
                value = playlistId.toString(),
            )
        }
        "clear" -> {
            playlistRepository().clear()
            debugResult(
                isOk = true,
                message = "Cleared playlists",
                command = command,
                target = action,
            )
        }
        else -> error("Unknown playlist action: $action")
    }
}

private suspend fun DebugCommandEntryPoint.addPlaylistItems(
    playlistId: Long,
    extras: Bundle,
): Int {
    val type = extras.getString("type")?.trim()?.lowercase().orEmpty()
    if (type == "remote") {
        error("Playlists are local only")
    }
    val videos = if (type == "folder") {
        val folderPath = extras.requiredString(EXTRA_PATH).canonicalPathOrSelf()
        mediaRepository().getVideosFlow().first().filter { video ->
            video.isUnderFolder(folderPath)
        }
    } else {
        listOf(requireDebugVideo(extras.requiredMediaTarget()))
    }
    return playlistRepository().addVideos(playlistId, videos)
}

private suspend fun DebugCommandEntryPoint.requireDebugVideo(target: String): Video {
    mediaRepository().getVideoByUri(target)?.let { return it }
    val videos = mediaRepository().getVideosFlow().first().distinctBy(Video::uriString)
    val exactMatches = videos.filter { video ->
        video.uriString == target ||
            video.path == target ||
            video.nameWithExtension == target ||
            video.displayName == target
    }
    if (exactMatches.size == 1) return exactMatches.single()
    if (exactMatches.size > 1) error("Ambiguous media target: $target")
    val partialMatches = videos.filter { video ->
        video.path.contains(target, ignoreCase = true) ||
            video.nameWithExtension.contains(target, ignoreCase = true) ||
            video.displayName.contains(target, ignoreCase = true)
    }
    if (partialMatches.size == 1) return partialMatches.single()
    if (partialMatches.size > 1) error("Ambiguous media target: $target")
    error("Media not found: $target")
}

private fun Video.isUnderFolder(folderPath: String): Boolean {
    val canonicalFolder = folderPath.canonicalPathOrSelf()
    val canonicalParent = parentPath.canonicalPathOrSelf()
    val canonicalPath = path.canonicalPathOrSelf()
    return canonicalParent == canonicalFolder ||
        canonicalParent.startsWith(canonicalFolder + File.separator) ||
        canonicalPath.startsWith(canonicalFolder + File.separator)
}

private fun Playlist.debugSummary(): String = "id=$id title=$title items=$itemCount"

private fun PlaylistItem.debugSummary(): String = "id=$id title=$title path=$mediaPath"
