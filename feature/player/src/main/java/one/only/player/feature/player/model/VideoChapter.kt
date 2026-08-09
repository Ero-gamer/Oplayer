package one.only.player.feature.player.model

import android.os.Bundle
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.extractor.metadata.Chapter

data class VideoChapter(
    val index: Int,
    val title: String?,
    val startTimeMs: Long,
    val endTimeMs: Long,
)

fun Player.extractVideoChapters(): List<VideoChapter> {
    val windowOffsetMs = currentWindowOffsetMs()
    val durationMs = duration.takeIf { it != C.TIME_UNSET && it > 0L }
    val rawChapters = currentTracks.groups
        .asSequence()
        .flatMap { group ->
            (0 until group.length).asSequence().flatMap { trackIndex ->
                val metadata = group.getTrackFormat(trackIndex).metadata
                if (metadata == null) {
                    emptySequence()
                } else {
                    (0 until metadata.length()).asSequence()
                        .map(metadata::get)
                        .filterIsInstance<Chapter>()
                }
            }
        }
        .filterNot(Chapter::isHidden)
        .mapNotNull { chapter ->
            val startTimeMs = chapter.startTimeMs.takeIf { it != C.TIME_UNSET } ?: return@mapNotNull null
            RawVideoChapter(
                title = chapter.title?.value?.trim()?.takeIf(String::isNotEmpty),
                startTimeMs = (startTimeMs - windowOffsetMs).coerceAtLeast(0L),
                endTimeMs = chapter.endTimeMs
                    .takeIf { it != C.TIME_UNSET }
                    ?.let { (it - windowOffsetMs).coerceAtLeast(0L) },
            )
        }
        .distinct()
        .sortedBy(RawVideoChapter::startTimeMs)
        .toList()

    return rawChapters.mapIndexed { index, chapter ->
        val nextStartTimeMs = rawChapters.getOrNull(index + 1)?.startTimeMs
        val endTimeMs = chapter.endTimeMs
            ?: nextStartTimeMs
            ?: durationMs
            ?: chapter.startTimeMs
        VideoChapter(
            index = index,
            title = chapter.title,
            startTimeMs = chapter.startTimeMs,
            endTimeMs = endTimeMs.coerceAtLeast(chapter.startTimeMs),
        )
    }
}

fun List<VideoChapter>.currentChapterIndex(positionMs: Long): Int? {
    if (isEmpty()) return null
    return indexOfLast { chapter -> positionMs >= chapter.startTimeMs }
        .takeIf { it >= 0 }
}

fun VideoChapter.toBundle(): Bundle = Bundle().apply {
    putInt(KEY_INDEX, index)
    putString(KEY_TITLE, title)
    putLong(KEY_START_TIME_MS, startTimeMs)
    putLong(KEY_END_TIME_MS, endTimeMs)
}

fun Bundle.toVideoChapter(): VideoChapter = VideoChapter(
    index = getInt(KEY_INDEX),
    title = getString(KEY_TITLE),
    startTimeMs = getLong(KEY_START_TIME_MS),
    endTimeMs = getLong(KEY_END_TIME_MS),
)

private fun Player.currentWindowOffsetMs(): Long {
    val timeline = currentTimeline
    if (timeline.isEmpty || currentMediaItemIndex !in 0 until timeline.windowCount) return 0L
    return timeline
        .getWindow(currentMediaItemIndex, Timeline.Window())
        .positionInFirstPeriodUs / 1_000L
}

private data class RawVideoChapter(
    val title: String?,
    val startTimeMs: Long,
    val endTimeMs: Long?,
)

private const val KEY_INDEX = "index"
private const val KEY_TITLE = "title"
private const val KEY_START_TIME_MS = "start_time_ms"
private const val KEY_END_TIME_MS = "end_time_ms"
