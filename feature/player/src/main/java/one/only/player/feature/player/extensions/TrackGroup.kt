package one.only.player.feature.player.extensions

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.util.UnstableApi
import java.util.Locale
import one.only.player.core.model.containsAtmosHint

@UnstableApi
fun TrackGroup.getName(trackType: @C.TrackType Int, index: Int): String {
    val format = this.getFormat(0)
    val language = format.language
    val label = format.label
    return buildString {
        if (label != null) {
            append(label)
        }
        if (isEmpty()) {
            if (trackType == C.TRACK_TYPE_TEXT) {
                append("Subtitle Track #${index + 1}")
            } else {
                append("Audio Track #${index + 1}")
            }
        }

        if (language != null && language != "und") {
            append(" - ")
            append(Locale.forLanguageTag(language).displayLanguage)
        }

        if (trackType != C.TRACK_TYPE_AUDIO) return@buildString
        format.audioFormatSuffix()?.let { suffix ->
            append(" - ")
            append(suffix)
        }
    }
}

@UnstableApi
private fun Format.audioFormatSuffix(): String? {
    val codecName = audioCodecDisplayName().takeIf { !label.alreadyMentionsAudioCodec() }
    val channelLayout = audioChannelLayoutLabel()
    val suffix = listOfNotNull(codecName, channelLayout).joinToString(" ")
    return suffix.takeIf { it.isNotBlank() }
}

@UnstableApi
private fun Format.audioCodecDisplayName(): String? {
    val mimeType = sampleMimeType
    val codecs = codecs.orEmpty().lowercase(Locale.US)
    val isAtmos = mimeType == MimeTypes.AUDIO_E_AC3_JOC ||
        codecs.containsAtmosHint() ||
        label.containsAtmosHint()
    return when {
        mimeType == MimeTypes.AUDIO_E_AC3_JOC || (mimeType == MimeTypes.AUDIO_E_AC3 && isAtmos) -> "Dolby Atmos"
        mimeType == MimeTypes.AUDIO_E_AC3 -> "Dolby Digital Plus"
        mimeType == MimeTypes.AUDIO_AC3 -> "Dolby Digital"
        mimeType == MimeTypes.AUDIO_AC4 && isAtmos -> "Dolby Atmos"
        mimeType == MimeTypes.AUDIO_AC4 -> "Dolby AC-4"
        mimeType == MimeTypes.AUDIO_TRUEHD && isAtmos -> "Dolby TrueHD Atmos"
        mimeType == MimeTypes.AUDIO_TRUEHD -> "Dolby TrueHD"
        else -> null
    }
}

@UnstableApi
private fun Format.audioChannelLayoutLabel(): String? = when (channelCount) {
    1 -> "1.0"
    2 -> "2.0"
    6 -> "5.1"
    8 -> "7.1"
    10 -> "5.1.4"
    12 -> "7.1.4"
    else -> channelCount.takeIf { it > 0 }?.let { "${it}ch" }
}

private fun String?.alreadyMentionsAudioCodec(): Boolean {
    val normalized = this?.lowercase(Locale.US) ?: return false
    return AUDIO_CODEC_HINTS.any { hint -> hint in normalized }
}

private val AUDIO_CODEC_HINTS = listOf(
    "dolby",
    "atmos",
    "truehd",
    "e-ac-3",
    "eac3",
    "ac-3",
    "ac3",
    "ac-4",
    "ac4",
)
