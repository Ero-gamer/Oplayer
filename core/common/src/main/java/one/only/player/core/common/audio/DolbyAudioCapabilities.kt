package one.only.player.core.common.audio

import android.content.Context
import android.media.AudioManager
import android.media.MediaCodecList
import android.media.Spatializer
import android.media.audiofx.AudioEffect
import android.os.Build
import android.provider.Settings
import java.util.UUID

data class DolbyDecoderCapabilities(
    val hasAc3Decoder: Boolean,
    val hasEac3Decoder: Boolean,
    val hasEac3JocDecoder: Boolean,
    val hasAc4Decoder: Boolean,
    val hasTrueHdDecoder: Boolean,
) {
    val hasDolbyDecoder: Boolean
        get() = hasAc3Decoder || hasEac3Decoder || hasAc4Decoder || hasTrueHdDecoder
}

data class SpatializerStatus(
    val isSupported: Boolean,
    val isAvailable: Boolean,
    val isEnabled: Boolean,
)

data class OemDolbyProcessingStatus(
    val isPresent: Boolean,
    val implementer: String?,
    val effectName: String?,
)

object DolbyAudioCapabilities {
    private val decoderLock = Any()
    private val oemLock = Any()

    @Volatile
    private var cachedDecoderCapabilities: DolbyDecoderCapabilities? = null

    @Volatile
    private var cachedOemDolbyProcessing: OemDolbyProcessingStatus? = null

    fun decoderCapabilities(): DolbyDecoderCapabilities {
        cachedDecoderCapabilities?.let { return it }
        return synchronized(decoderLock) {
            cachedDecoderCapabilities ?: detectDecoderCapabilities().also { cachedDecoderCapabilities = it }
        }
    }

    fun spatializerStatus(context: Context): SpatializerStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2) {
            return SpatializerStatus(
                isSupported = false,
                isAvailable = false,
                isEnabled = false,
            )
        }
        val audioManager = context.getSystemService(AudioManager::class.java)
            ?: return SpatializerStatus(
                isSupported = false,
                isAvailable = false,
                isEnabled = false,
            )
        val spatializer = audioManager.spatializer
        return SpatializerStatus(
            isSupported = spatializer.immersiveAudioLevel != Spatializer.SPATIALIZER_IMMERSIVE_LEVEL_NONE,
            isAvailable = spatializer.isAvailable,
            isEnabled = spatializer.isEnabled,
        )
    }

    fun oemDolbyProcessing(context: Context): OemDolbyProcessingStatus {
        cachedOemDolbyProcessing?.let { return it }
        return synchronized(oemLock) {
            cachedOemDolbyProcessing ?: detectOemDolbyProcessing(context.applicationContext).also {
                cachedOemDolbyProcessing = it
            }
        }
    }

    private fun detectDecoderCapabilities(): DolbyDecoderCapabilities {
        val supportedTypes = linkedSetOf<String>()
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        codecList.codecInfos.forEach { info ->
            if (info.isEncoder) return@forEach
            supportedTypes.addAll(info.supportedTypes.map { it.lowercase() })
        }
        return DolbyDecoderCapabilities(
            hasAc3Decoder = MIME_AC3 in supportedTypes,
            hasEac3Decoder = MIME_EAC3 in supportedTypes,
            hasEac3JocDecoder = MIME_EAC3_JOC in supportedTypes,
            hasAc4Decoder = MIME_AC4 in supportedTypes,
            hasTrueHdDecoder = MIME_TRUEHD in supportedTypes,
        )
    }

    private fun detectOemDolbyProcessing(context: Context): OemDolbyProcessingStatus {
        val implementer = runCatching {
            Settings.Global.getString(context.contentResolver, EFFECT_IMPLEMENTER_SETTING)
        }.getOrNull()?.takeIf { it.isNotBlank() }
        val effect = runCatching {
            AudioEffect.queryEffects()?.firstOrNull { descriptor -> descriptor.isOemDolbyEffect() }
        }.getOrNull()
        return OemDolbyProcessingStatus(
            isPresent = effect != null || implementer.equals(DOLBY_IMPLEMENTER, ignoreCase = true),
            implementer = implementer,
            effectName = effect?.name,
        )
    }

    private fun AudioEffect.Descriptor.isOemDolbyEffect(): Boolean {
        if (uuid == DOLBY_DAP_UUID || type in OEM_DOLBY_EFFECT_TYPES) return true
        val implementorName = implementor.orEmpty()
        val effectName = name.orEmpty()
        return implementorName.contains(DOLBY_IMPLEMENTER, ignoreCase = true) ||
            effectName.contains(DAP_EFFECT_NAME_HINT, ignoreCase = true)
    }

    private const val MIME_AC3 = "audio/ac3"
    private const val MIME_EAC3 = "audio/eac3"
    private const val MIME_EAC3_JOC = "audio/eac3-joc"
    private const val MIME_AC4 = "audio/ac4"
    private const val MIME_TRUEHD = "audio/true-hd"
    private const val EFFECT_IMPLEMENTER_SETTING = "effect_implementer"
    private const val DOLBY_IMPLEMENTER = "dolby"
    private const val DAP_EFFECT_NAME_HINT = "dap"
    private val DOLBY_DAP_UUID = UUID.fromString("9d4921da-8225-4f29-aefa-39537a04bcaa")
    private val OEM_DOLBY_EFFECT_TYPES = setOf(
        UUID.fromString("46d279d9-9be7-453d-9d7c-ef937f675550"),
        UUID.fromString("46d279d9-9be7-453d-9d7c-ef937f675587"),
    )
}
