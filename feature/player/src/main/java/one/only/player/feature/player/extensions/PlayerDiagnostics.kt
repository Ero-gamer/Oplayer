package one.only.player.feature.player.extensions

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi

data class PlayerDiagnostics(
    val sourceType: String,
    val playbackState: Int,
    val playbackStateName: String,
    val positionMs: Long,
    val bufferedPositionMs: Long,
    val remainingBufferedDurationMs: Long,
    val durationMs: Long,
    val bufferedPercentage: Int,
    val playbackSpeed: Float,
    val audioBitrate: Int?,
    val videoBitrate: Int?,
    val isPlaying: Boolean,
    val playWhenReady: Boolean,
    val isLoading: Boolean,
    val errorCode: Int?,
    val errorCodeName: String?,
)

@OptIn(UnstableApi::class)
fun Player.diagnostics(): PlayerDiagnostics {
    val error = playerError
    val positionMs = currentPosition.safeDiagnosticTime()
    val bufferedPositionMs = bufferedPosition.safeDiagnosticTime()
    return PlayerDiagnostics(
        sourceType = diagnosticSourceType(),
        playbackState = playbackState,
        playbackStateName = playbackStateName(playbackState),
        positionMs = positionMs,
        bufferedPositionMs = bufferedPositionMs,
        remainingBufferedDurationMs = (bufferedPositionMs - positionMs).coerceAtLeast(0L),
        durationMs = availableDurationMs().safeDiagnosticTime(),
        bufferedPercentage = bufferedPercentage,
        playbackSpeed = playbackParameters.speed,
        audioBitrate = currentTracks.selectedBitrate(C.TRACK_TYPE_AUDIO),
        videoBitrate = currentTracks.selectedBitrate(C.TRACK_TYPE_VIDEO),
        isPlaying = isPlaying,
        playWhenReady = playWhenReady,
        isLoading = isLoading,
        errorCode = error?.errorCode,
        errorCodeName = error?.errorCodeName,
    )
}

fun PlayerDiagnostics.toLogString(): String = "source=$sourceType state=$playbackStateName positionMs=$positionMs " +
    "bufferedPositionMs=$bufferedPositionMs remainingBufferedDurationMs=$remainingBufferedDurationMs " +
    "durationMs=$durationMs bufferedPercentage=$bufferedPercentage speed=$playbackSpeed " +
    "audioBitrate=$audioBitrate videoBitrate=$videoBitrate isPlaying=$isPlaying " +
    "playWhenReady=$playWhenReady isLoading=$isLoading errorCode=$errorCode " +
    "errorName=$errorCodeName"

@OptIn(UnstableApi::class)
private fun Tracks.selectedBitrate(trackType: @C.TrackType Int): Int? {
    val group = groups.firstOrNull { it.type == trackType && it.isSelected } ?: return null
    val selectedIndex = (0 until group.length).firstOrNull(group::isTrackSelected) ?: return null
    return group.getTrackFormat(selectedIndex).bitrate.takeIf { it > 0 }
}

private fun Player.diagnosticSourceType(): String {
    currentMediaItem?.mediaMetadata?.remoteProtocol?.lowercase()?.let { return it }
    val uri = currentMediaItem?.localConfiguration?.uri
        ?: currentMediaItem?.requestMetadata?.mediaUri
    return uri?.scheme?.lowercase() ?: "unknown"
}

private fun playbackStateName(state: Int): String = when (state) {
    Player.STATE_IDLE -> "IDLE"
    Player.STATE_BUFFERING -> "BUFFERING"
    Player.STATE_READY -> "READY"
    Player.STATE_ENDED -> "ENDED"
    else -> "UNKNOWN($state)"
}

private fun Long.safeDiagnosticTime(): Long = takeIf { it != C.TIME_UNSET && it >= 0L } ?: 0L
