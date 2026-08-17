package one.only.player.debug

import android.content.Context
import android.os.Bundle
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import one.only.player.core.model.Video

internal fun Context.runHistoryCommand(
    action: String,
    target: String?,
    extras: Bundle?,
): Bundle {
    val command = "history.$action"
    val entryPoint = EntryPointAccessors.fromApplication(
        applicationContext,
        DebugCommandEntryPoint::class.java,
    )
    val value = extras.withTarget(target)

    return runCatching {
        runBlocking { entryPoint.runHistoryAction(action, value) }
    }.getOrElse {
        debugResult(
            isOk = false,
            message = it.message ?: "Failed to handle history action: $action",
            command = command,
            target = action,
        )
    }
}

private suspend fun DebugCommandEntryPoint.runHistoryAction(
    action: String,
    extras: Bundle,
): Bundle {
    val command = "history.$action"
    return when (action) {
        "list" -> {
            val filter = extras.optionalFilter()
            val videos = mediaRepository().getVideosFlow().first()
                .filter { video -> !video.isInRecycleBin && video.lastPlayedAt != null }
                .sortedByDescending { video -> video.lastPlayedAt?.time ?: 0L }
                .filter { video -> filter == null || video.matchesHistoryFilter(filter) }
            debugResult(
                isOk = true,
                message = videos.joinToString(separator = "; ") { video -> video.debugHistorySummary() },
                command = command,
                target = action,
                value = videos.size.toString(),
            )
        }
        "remove" -> {
            val video = requireHistoryVideo(extras.requiredMediaTarget())
            mediaRepository().clearMediumLastPlayedTime(video.uriString)
            debugResult(
                isOk = true,
                message = "Removed from history: ${video.displayName}",
                command = command,
                target = action,
                value = video.uriString,
            )
        }
        "clear" -> {
            mediaRepository().clearAllLastPlayedTimes()
            debugResult(
                isOk = true,
                message = "Cleared watch history",
                command = command,
                target = action,
            )
        }
        else -> error("Unknown history action: $action")
    }
}

private suspend fun DebugCommandEntryPoint.requireHistoryVideo(target: String): Video {
    mediaRepository().getVideoByUri(target)?.let { return it }
    val videos = mediaRepository().getVideosFlow().first().distinctBy(Video::uriString)
    val matches = videos.filter { video -> video.matchesHistoryFilter(target) }
    if (matches.size == 1) return matches.single()
    if (matches.size > 1) error("Ambiguous media target: $target")
    error("Media not found: $target")
}

private fun Video.matchesHistoryFilter(filter: String): Boolean = uriString == filter ||
    path == filter ||
    nameWithExtension == filter ||
    displayName == filter ||
    path.contains(filter, ignoreCase = true) ||
    nameWithExtension.contains(filter, ignoreCase = true) ||
    displayName.contains(filter, ignoreCase = true)

private fun Video.debugHistorySummary(): String = "title=$displayName path=$path played=${lastPlayedAt?.time ?: 0L}"
