package one.only.player.feature.videopicker.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import one.only.player.core.common.Utils
import one.only.player.core.model.Video
import one.only.player.core.model.displayCodecName
import one.only.player.core.ui.R
import one.only.player.core.ui.components.AppDialog
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun VideoInfoDialog(
    video: Video,
    onDismiss: () -> Unit,
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = video.displayName,
        content = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .testTag("panel_video_info"),
            ) {
                InfoSection(
                    title = stringResource(R.string.file),
                    fields = buildList {
                        add(InfoField.wide(stringResource(R.string.file_name), video.nameWithExtension))
                        add(InfoField.wide(stringResource(R.string.location), video.parentPath))
                        add(InfoField.narrow(stringResource(R.string.size), video.formattedFileSize))
                        add(InfoField.narrow(stringResource(R.string.duration), video.formattedDuration))
                        video.format?.let { add(InfoField.wide(stringResource(R.string.format), it)) }
                    },
                )
                video.videoStream?.let { videoStream ->
                    InfoSection(
                        title = stringResource(R.string.video_track),
                        fields = buildList {
                            videoStream.title?.let { add(InfoField.wide(stringResource(R.string.title), it)) }
                            add(InfoField.wide(stringResource(R.string.codec), videoStream.codecName))
                            add(
                                InfoField.narrow(
                                    label = stringResource(R.string.resolution),
                                    value = "${videoStream.frameWidth} x ${videoStream.frameHeight}",
                                ),
                            )
                            add(
                                InfoField.narrow(
                                    label = stringResource(R.string.frame_rate),
                                    value = videoStream.frameRate.toInt().toString(),
                                ),
                            )
                            Utils.formatBitrate(videoStream.bitRate)?.let {
                                add(InfoField.narrow(stringResource(R.string.bitrate), it))
                            }
                        },
                    )
                }
                video.audioStreams.forEachIndexed { index, audioStream ->
                    InfoSection(
                        title = "${stringResource(R.string.audio_track)} #${index + 1}",
                        fields = buildList {
                            audioStream.title?.let { add(InfoField.wide(stringResource(R.string.title), it)) }
                            add(InfoField.wide(stringResource(R.string.codec), audioStream.displayCodecName()))
                            add(
                                InfoField.narrow(
                                    label = stringResource(R.string.sample_rate),
                                    value = "${audioStream.sampleRate} Hz",
                                ),
                            )
                            add(
                                InfoField.narrow(
                                    label = stringResource(R.string.sample_format),
                                    value = audioStream.sampleFormat.toString(),
                                ),
                            )
                            Utils.formatBitrate(audioStream.bitRate)?.let {
                                add(InfoField.narrow(stringResource(R.string.bitrate), it))
                            }
                            add(
                                InfoField.narrow(
                                    label = stringResource(R.string.channels),
                                    value = audioStream.channelLayout ?: audioStream.channels.toString(),
                                ),
                            )
                            Utils.formatLanguage(audioStream.language)?.let {
                                add(InfoField.narrow(stringResource(R.string.language), it))
                            }
                        },
                    )
                }
                video.subtitleStreams.forEachIndexed { index, subtitleStream ->
                    InfoSection(
                        title = "${stringResource(R.string.subtitle_track)} #${index + 1}",
                        fields = buildList {
                            subtitleStream.title?.let { add(InfoField.wide(stringResource(R.string.title), it)) }
                            add(InfoField.wide(stringResource(R.string.codec), subtitleStream.codecName))
                            Utils.formatLanguage(subtitleStream.language)?.let {
                                add(InfoField.narrow(stringResource(R.string.language), it))
                            }
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag("btn_video_info_ok"),
                text = stringResource(id = R.string.okay),
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        },
    )
}

@Composable
private fun InfoSection(
    title: String,
    fields: List<InfoField>,
) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.subtitle,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(top = SectionTopSpacing, bottom = SectionBottomSpacing),
    )
    Column(verticalArrangement = Arrangement.spacedBy(FieldSpacing)) {
        fields.chunkIntoRows().forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(FieldSpacing)) {
                row.forEach { field ->
                    InfoFieldCell(field = field, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InfoFieldCell(
    field: InfoField,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = field.label,
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            text = field.value,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurface,
        )
    }
}

@Immutable
private data class InfoField(
    val label: String,
    val value: String,
    val isWide: Boolean,
) {
    companion object {
        fun wide(label: String, value: String) = InfoField(label = label, value = value, isWide = true)

        fun narrow(label: String, value: String) = InfoField(label = label, value = value, isWide = false)
    }
}

private fun List<InfoField>.chunkIntoRows(): List<List<InfoField>> = buildList {
    var narrowRun = mutableListOf<InfoField>()
    for (field in this@chunkIntoRows) {
        if (!field.isWide) {
            narrowRun.add(field)
            continue
        }
        addAll(narrowRun.chunked(NarrowFieldsPerRow))
        narrowRun = mutableListOf()
        add(listOf(field))
    }
    addAll(narrowRun.chunked(NarrowFieldsPerRow))
}

private val SectionTopSpacing = 18.dp
private val SectionBottomSpacing = 8.dp
private val FieldSpacing = 12.dp
private const val NarrowFieldsPerRow = 2
