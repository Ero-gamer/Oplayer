package one.only.player.feature.player.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import one.only.player.core.common.Utils
import one.only.player.core.model.Video
import one.only.player.core.ui.R
import one.only.player.core.ui.designsystem.AppIcons
import one.only.player.feature.player.service.VideoFormatInfo
import one.only.player.feature.player.service.getVideoFormatInfo
import one.only.player.feature.player.state.rememberTracksState
import one.only.player.feature.player.ui.panel.rememberPlayerPanelTokens
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class VideoInfoRow(
    val id: String,
    val label: String,
    val value: String,
)

private data class VideoInfoSection(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val rows: List<VideoInfoRow>,
)

@OptIn(UnstableApi::class)
@Composable
fun VideoInfoContent(
    player: Player,
    video: Video?,
    durationMs: Long,
    modifier: Modifier = Modifier,
) {
    val videoTracks = rememberTracksState(player, C.TRACK_TYPE_VIDEO).tracks
    val audioTracks = rememberTracksState(player, C.TRACK_TYPE_AUDIO).tracks
    val subtitleTracks = rememberTracksState(player, C.TRACK_TYPE_TEXT).tracks
    val videoFormat = videoTracks.selectedFormat()
    val audioFormat = audioTracks.selectedFormat()
    val subtitleFormat = subtitleTracks.selectedFormat()
    val currentMediaItem = player.currentMediaItem
    var serviceFormatInfo by remember(player) { mutableStateOf<VideoFormatInfo?>(null) }

    LaunchedEffect(player, currentMediaItem?.mediaId, videoTracks) {
        serviceFormatInfo = (player as? MediaController)?.getVideoFormatInfo()
    }

    val sections = buildVideoInfoSections(
        player = player,
        video = video,
        durationMs = durationMs,
        videoFormat = videoFormat,
        audioFormat = audioFormat,
        subtitleFormat = subtitleFormat,
        serviceFormatInfo = serviceFormatInfo,
    )
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("panel_video_info"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        items(
            items = sections,
            key = VideoInfoSection::id,
        ) { section ->
            VideoInfoSectionView(section)
        }
    }
}

@Composable
private fun buildVideoInfoSections(
    player: Player,
    video: Video?,
    durationMs: Long,
    videoFormat: Format?,
    audioFormat: Format?,
    subtitleFormat: Format?,
    serviceFormatInfo: VideoFormatInfo?,
): List<VideoInfoSection> {
    val mediaUri = player.currentMediaItem?.localConfiguration?.uri
        ?: player.currentMediaItem?.requestMetadata?.mediaUri
    val fileName = video?.nameWithExtension
        ?: player.mediaMetadata.title?.toString()
        ?: mediaUri?.lastPathSegment
    val resolvedDurationMs = durationMs.takeIf { it > 0L } ?: video?.duration?.takeIf { it > 0L }
    val fileRows = listOfNotNull(
        infoRow("file", R.string.file, fileName),
        infoRow("location", R.string.location, video?.parentPath),
        infoRow("size", R.string.size, video?.formattedFileSize),
        infoRow("format", R.string.format, video?.format),
        infoRow("duration", R.string.duration, resolvedDurationMs?.let(Utils::formatDurationMillis)),
    )

    val sourceVideoStream = video?.videoStream
    val videoWidth = videoFormat?.width.validDimension()
        ?: serviceFormatInfo?.width.validDimension()
        ?: sourceVideoStream?.frameWidth.validDimension()
        ?: video?.width.validDimension()
    val videoHeight = videoFormat?.height.validDimension()
        ?: serviceFormatInfo?.height.validDimension()
        ?: sourceVideoStream?.frameHeight.validDimension()
        ?: video?.height.validDimension()
    val videoBitrate = videoFormat?.averageBitrate.validBitrate()
        ?: videoFormat?.peakBitrate.validBitrate()
        ?: sourceVideoStream?.bitRate?.takeIf { it > 0L }
    val videoFrameRate = videoFormat?.frameRate.validFrameRate()
        ?: sourceVideoStream?.frameRate?.takeIf { it > 0.0 }
    val isHdr = serviceFormatInfo?.isHdr ?: videoFormat?.isHdr()
    val videoRows = listOfNotNull(
        infoRow("decoder", R.string.decoder, serviceFormatInfo?.decoderName),
        infoRow("resolution", R.string.resolution, resolution(videoWidth, videoHeight)),
        infoRow("codec", R.string.codec, videoFormat.codecLabel() ?: sourceVideoStream?.codecName),
        infoRow("bitrate", R.string.bitrate, videoBitrate?.let(Utils::formatBitrate)),
        infoRow("frame_rate", R.string.frame_rate, videoFrameRate?.let(Utils::formatFrameRate)),
        infoRow("dynamic_range", R.string.dynamic_range, isHdr?.let { if (it) "HDR" else "SDR" }),
    )

    val sourceAudioStream = video?.audioStreams?.firstOrNull()
    val audioBitrate = audioFormat?.averageBitrate.validBitrate()
        ?: audioFormat?.peakBitrate.validBitrate()
        ?: sourceAudioStream?.bitRate?.takeIf { it > 0L }
    val audioRows = listOfNotNull(
        infoRow("title", R.string.title, audioFormat?.label ?: sourceAudioStream?.title),
        infoRow("codec", R.string.codec, audioFormat.codecLabel() ?: sourceAudioStream?.codecName),
        infoRow(
            id = "sample_rate",
            labelRes = R.string.sample_rate,
            value = audioFormat?.sampleRate?.takeIf { it > 0 }?.let { "$it Hz" }
                ?: sourceAudioStream?.sampleRate?.takeIf { it > 0 }?.let { "$it Hz" },
        ),
        infoRow(
            id = "channels",
            labelRes = R.string.channels,
            value = sourceAudioStream?.channelLayout
                ?: audioFormat?.channelCount?.takeIf { it > 0 }?.toString()
                ?: sourceAudioStream?.channels?.takeIf { it > 0 }?.toString(),
        ),
        infoRow("bitrate", R.string.bitrate, audioBitrate?.let(Utils::formatBitrate)),
        infoRow(
            id = "language",
            labelRes = R.string.language,
            value = Utils.formatLanguage(audioFormat?.language ?: sourceAudioStream?.language),
        ),
    )

    val subtitleRows = listOfNotNull(
        infoRow("title", R.string.title, subtitleFormat?.label),
        infoRow("codec", R.string.codec, subtitleFormat.codecLabel()),
        infoRow("language", R.string.language, Utils.formatLanguage(subtitleFormat?.language)),
    )

    return listOfNotNull(
        fileRows.toSection("file", R.string.file, AppIcons.FileOpen),
        videoRows.toSection("video", R.string.video_track, AppIcons.Movie),
        audioRows.toSection("audio", R.string.audio_track, AppIcons.Audio),
        subtitleRows.toSection("subtitle", R.string.subtitle_track, AppIcons.Subtitle),
    )
}

@Composable
private fun infoRow(
    id: String,
    labelRes: Int,
    value: String?,
): VideoInfoRow? = value
    ?.takeIf(String::isNotBlank)
    ?.let { VideoInfoRow(id = id, label = stringResource(labelRes), value = it) }

@Composable
private fun List<VideoInfoRow>.toSection(
    id: String,
    titleRes: Int,
    icon: ImageVector,
): VideoInfoSection? = takeIf(List<VideoInfoRow>::isNotEmpty)?.let {
    VideoInfoSection(
        id = id,
        title = stringResource(titleRes),
        icon = icon,
        rows = it,
    )
}

@Composable
private fun VideoInfoSectionView(section: VideoInfoSection) {
    val tokens = rememberPlayerPanelTokens()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_info_section_${section.id}"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MiuixIcon(
                imageVector = section.icon,
                contentDescription = null,
                tint = tokens.accentColor,
                modifier = Modifier.size(18.dp),
            )
            MiuixText(
                text = section.title,
                style = MiuixTheme.textStyles.title3,
                color = tokens.contentColor,
            )
        }
        section.rows.forEach { row ->
            VideoInfoRowView(
                row = row,
                sectionId = section.id,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(tokens.containerBorderColor),
        ) {}
    }
}

@Composable
private fun VideoInfoRowView(
    row: VideoInfoRow,
    sectionId: String,
) {
    val tokens = rememberPlayerPanelTokens()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_info_${sectionId}_${row.id}")
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        MiuixText(
            text = row.label,
            style = MiuixTheme.textStyles.body2,
            color = tokens.secondaryContentColor,
            modifier = Modifier.weight(0.42f),
        )
        MiuixText(
            text = row.value,
            style = MiuixTheme.textStyles.body2.copy(fontFamily = FontFamily.Monospace),
            color = tokens.contentColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f),
        )
    }
}

@UnstableApi
private fun List<Tracks.Group>.selectedFormat(): Format? {
    val selectedGroup = firstOrNull(Tracks.Group::isSelected) ?: return null
    val selectedIndex = (0 until selectedGroup.length)
        .firstOrNull(selectedGroup::isTrackSelected)
        ?: return null
    return selectedGroup.getTrackFormat(selectedIndex)
}

private fun Format?.codecLabel(): String? = this?.let { format ->
    listOfNotNull(format.sampleMimeType, format.codecs)
        .distinct()
        .joinToString(separator = " / ")
        .takeIf(String::isNotBlank)
}

private fun Int?.validDimension(): Int? = this?.takeIf { it > 0 }

private fun Int?.validBitrate(): Long? = this?.takeIf { it > 0 }?.toLong()

private fun Float?.validFrameRate(): Double? = this?.takeIf { it > 0f }?.toDouble()

private fun resolution(
    width: Int?,
    height: Int?,
): String? = if (width != null && height != null) "$width x $height" else null

private fun Format.isHdr(): Boolean? {
    val colorTransfer = colorInfo?.colorTransfer ?: return null
    return colorTransfer == C.COLOR_TRANSFER_ST2084 || colorTransfer == C.COLOR_TRANSFER_HLG
}
