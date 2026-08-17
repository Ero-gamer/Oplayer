package one.only.player.feature.player.service.audio

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import one.only.player.core.model.PlayerPreferences

internal fun PlayerPreferences.toPlaybackAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
    .setUsage(C.USAGE_MEDIA)
    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
    .setSpatializationBehavior(
        if (isSpatialAudioEnabled) {
            C.SPATIALIZATION_BEHAVIOR_AUTO
        } else {
            C.SPATIALIZATION_BEHAVIOR_NEVER
        },
    )
    .build()
