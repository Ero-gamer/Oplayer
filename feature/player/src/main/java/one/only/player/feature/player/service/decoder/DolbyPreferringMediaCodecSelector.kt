package one.only.player.feature.player.service.decoder

import androidx.annotation.OptIn
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector

@OptIn(UnstableApi::class)
internal object DolbyPreferringMediaCodecSelector : MediaCodecSelector {
    override fun getDecoderInfos(
        mimeType: String,
        requiresSecureDecoder: Boolean,
        requiresTunnelingDecoder: Boolean,
    ): List<MediaCodecInfo> {
        val decoderInfos = MediaCodecSelector.DEFAULT.getDecoderInfos(
            mimeType,
            requiresSecureDecoder,
            requiresTunnelingDecoder,
        )
        if (!mimeType.isDolbyAudioMimeType() || decoderInfos.size <= 1) return decoderInfos

        return decoderInfos.sortedWith(
            compareBy<MediaCodecInfo> { info ->
                when {
                    info.name.contains(DOLBY_DECODER_NAME_HINT, ignoreCase = true) -> 0
                    info.hardwareAccelerated -> 1
                    else -> 2
                }
            }.thenBy { it.name },
        )
    }
}

@OptIn(UnstableApi::class)
private fun String.isDolbyAudioMimeType(): Boolean = this == MimeTypes.AUDIO_AC3 ||
    this == MimeTypes.AUDIO_E_AC3 ||
    this == MimeTypes.AUDIO_E_AC3_JOC ||
    this == MimeTypes.AUDIO_AC4 ||
    this == MimeTypes.AUDIO_TRUEHD

private const val DOLBY_DECODER_NAME_HINT = "dolby"
