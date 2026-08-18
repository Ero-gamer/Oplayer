package one.only.player.settings.screens.player

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import one.only.player.core.model.PlayerControl
import one.only.player.core.ui.R
import one.only.player.core.ui.designsystem.NextIcons

internal val PlayerControl.id: String
    get() = name.lowercase()

@StringRes
internal fun PlayerControl.nameRes(): Int = when (this) {
    PlayerControl.BACK -> R.string.navigate_up
    PlayerControl.PLAYLIST -> R.string.now_playing
    PlayerControl.PLAYBACK_SPEED -> R.string.select_playback_speed
    PlayerControl.AUDIO -> R.string.select_audio_track
    PlayerControl.SUBTITLE -> R.string.select_subtitle_track
    PlayerControl.PREVIOUS -> R.string.player_controls_previous
    PlayerControl.PLAY_PAUSE -> R.string.player_controls_play_pause
    PlayerControl.NEXT -> R.string.player_controls_next
    PlayerControl.LOCK -> R.string.controls_lock_switch
    PlayerControl.MUTE -> R.string.mute_switch
    PlayerControl.MARK -> R.string.playback_marks
    PlayerControl.CHAPTERS -> R.string.chapters
    PlayerControl.SCALE -> R.string.video_zoom
    PlayerControl.DECODER -> R.string.decoder_priority
    PlayerControl.AMBIENCE_MODE -> R.string.ambience_mode
    PlayerControl.VIDEO_FILTERS -> R.string.video_filters
    PlayerControl.PIP -> R.string.pip_settings
    PlayerControl.SCREENSHOT -> R.string.take_screenshot
    PlayerControl.BACKGROUND_PLAY -> R.string.background_play
    PlayerControl.LOOP -> R.string.loop_mode
    PlayerControl.SHUFFLE -> R.string.shuffle
    PlayerControl.SLEEP_TIMER -> R.string.sleep_timer
    PlayerControl.ROTATE -> R.string.screen_rotation
    PlayerControl.MIRROR_VIDEO -> R.string.mirror_video
}

internal fun PlayerControl.icon(): ImageVector = when (this) {
    PlayerControl.BACK -> NextIcons.ArrowBack
    PlayerControl.PLAYLIST -> NextIcons.PlaylistPlay
    PlayerControl.PLAYBACK_SPEED -> NextIcons.Speed
    PlayerControl.AUDIO -> NextIcons.Audio
    PlayerControl.SUBTITLE -> NextIcons.Subtitle
    PlayerControl.PREVIOUS -> NextIcons.SkipPrevious
    PlayerControl.PLAY_PAUSE -> NextIcons.Play
    PlayerControl.NEXT -> NextIcons.SkipNext
    PlayerControl.LOCK -> NextIcons.Lock
    PlayerControl.MUTE -> NextIcons.VolumeUp
    PlayerControl.MARK -> NextIcons.History
    PlayerControl.CHAPTERS -> NextIcons.PlaylistPlay
    PlayerControl.SCALE -> NextIcons.Frame
    PlayerControl.DECODER -> NextIcons.Decoder
    PlayerControl.AMBIENCE_MODE -> NextIcons.Style
    PlayerControl.VIDEO_FILTERS -> NextIcons.Sensitivity
    PlayerControl.PIP -> NextIcons.Pip
    PlayerControl.SCREENSHOT -> NextIcons.Screenshot
    PlayerControl.BACKGROUND_PLAY -> NextIcons.Headset
    PlayerControl.LOOP -> NextIcons.Loop
    PlayerControl.SHUFFLE -> NextIcons.Shuffle
    PlayerControl.SLEEP_TIMER -> NextIcons.Timer
    PlayerControl.ROTATE -> NextIcons.Rotation
    PlayerControl.MIRROR_VIDEO -> NextIcons.Size
}

@Composable
internal fun PlayerControl.label(): String = stringResource(nameRes())
