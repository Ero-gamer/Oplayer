package one.only.player.feature.player.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import one.only.player.core.model.PlayerControl
import one.only.player.core.ui.R
import one.only.player.core.ui.designsystem.NextIcons

// 控件点击后的行为：要么打开面板，要么直接执行
internal sealed interface PlayerControlAction {
    data class OpenPanel(val route: MenuRoute) : PlayerControlAction

    data class Execute(val onExecute: () -> Unit) : PlayerControlAction
}

// 菜单和角落共用同一份绑定，控件换位置不改变行为
internal data class PlayerControlBinding(
    val control: PlayerControl,
    val icon: ImageVector,
    val label: String,
    val menuTestTag: String,
    val cornerTestTag: String,
    val action: PlayerControlAction,
    // isAvailable 为 false 时整个控件不渲染；isEnabled 为 false 时渲染但不可点
    val isAvailable: Boolean = true,
    val isEnabled: Boolean = true,
)

@Composable
internal fun playerControlBindings(
    isPipSupported: Boolean,
    isTakingScreenshot: Boolean,
    hasChapters: Boolean,
    onRotate: () -> Unit,
    onPictureInPicture: () -> Unit,
    onScreenshot: () -> Unit,
    onPlayInBackground: () -> Unit,
): Map<PlayerControl, PlayerControlBinding> {
    val bindings = listOf(
        binding(
            control = PlayerControl.ROTATE,
            icon = NextIcons.Rotation,
            label = stringResource(R.string.screen_rotation),
            menuTestTag = "menu_item_rotate",
            action = PlayerControlAction.Execute(onRotate),
        ),
        binding(
            control = PlayerControl.PLAYLIST,
            icon = NextIcons.PlaylistPlay,
            label = stringResource(R.string.now_playing),
            menuTestTag = "menu_item_playlist",
            action = PlayerControlAction.OpenPanel(MenuRoute.Playlist),
        ),
        binding(
            control = PlayerControl.PLAYBACK_SPEED,
            icon = NextIcons.Speed,
            label = stringResource(R.string.select_playback_speed),
            menuTestTag = "menu_item_playback_speed",
            action = PlayerControlAction.OpenPanel(MenuRoute.PlaybackSpeed),
        ),
        binding(
            control = PlayerControl.SUBTITLE,
            icon = NextIcons.Subtitle,
            label = stringResource(R.string.select_subtitle_track),
            menuTestTag = "menu_item_subtitle",
            action = PlayerControlAction.OpenPanel(MenuRoute.Subtitle),
        ),
        binding(
            control = PlayerControl.AUDIO,
            icon = NextIcons.Audio,
            label = stringResource(R.string.select_audio_track),
            menuTestTag = "menu_item_audio",
            action = PlayerControlAction.OpenPanel(MenuRoute.Audio),
        ),
        binding(
            control = PlayerControl.CHAPTERS,
            icon = NextIcons.PlaylistPlay,
            label = stringResource(R.string.chapters),
            menuTestTag = "menu_item_chapters",
            action = PlayerControlAction.OpenPanel(MenuRoute.Chapters),
            isAvailable = hasChapters,
        ),
        binding(
            control = PlayerControl.SCALE,
            icon = NextIcons.Frame,
            label = stringResource(R.string.video_zoom),
            menuTestTag = "menu_item_video_scale",
            action = PlayerControlAction.OpenPanel(MenuRoute.VideoContentScale),
        ),
        binding(
            control = PlayerControl.DECODER,
            icon = NextIcons.Decoder,
            label = stringResource(R.string.decoder_priority),
            menuTestTag = "menu_item_decoder",
            action = PlayerControlAction.OpenPanel(MenuRoute.Decoder),
        ),
        binding(
            control = PlayerControl.VIDEO_FILTERS,
            icon = NextIcons.Sensitivity,
            label = stringResource(R.string.video_filters),
            menuTestTag = "menu_item_video_filters",
            action = PlayerControlAction.OpenPanel(MenuRoute.VideoFilters),
        ),
        binding(
            control = PlayerControl.SLEEP_TIMER,
            icon = NextIcons.Timer,
            label = stringResource(R.string.sleep_timer),
            menuTestTag = "menu_item_sleep_timer",
            action = PlayerControlAction.OpenPanel(MenuRoute.SleepTimer),
        ),
        binding(
            control = PlayerControl.MARK,
            icon = NextIcons.History,
            label = stringResource(R.string.playback_marks),
            menuTestTag = "menu_item_playback_marks",
            action = PlayerControlAction.OpenPanel(MenuRoute.PlaybackMarks),
        ),
        binding(
            control = PlayerControl.LOCK,
            icon = NextIcons.Lock,
            label = stringResource(R.string.controls_lock_switch),
            menuTestTag = "menu_item_lock",
            action = PlayerControlAction.OpenPanel(MenuRoute.ControlLock),
        ),
        binding(
            control = PlayerControl.MUTE,
            icon = NextIcons.VolumeUp,
            label = stringResource(R.string.mute_switch),
            menuTestTag = "menu_item_mute",
            action = PlayerControlAction.OpenPanel(MenuRoute.Mute),
        ),
        binding(
            control = PlayerControl.AMBIENCE_MODE,
            icon = NextIcons.Style,
            label = stringResource(R.string.ambience_mode),
            menuTestTag = "menu_item_ambience",
            action = PlayerControlAction.OpenPanel(MenuRoute.AmbienceMode),
        ),
        binding(
            control = PlayerControl.MIRROR_VIDEO,
            icon = NextIcons.Size,
            label = stringResource(R.string.mirror_video),
            menuTestTag = "menu_item_mirror_video",
            action = PlayerControlAction.OpenPanel(MenuRoute.MirrorVideo),
        ),
        binding(
            control = PlayerControl.PIP,
            icon = NextIcons.Pip,
            label = stringResource(R.string.pip_settings),
            menuTestTag = "menu_item_pip",
            action = PlayerControlAction.Execute(onPictureInPicture),
            isAvailable = isPipSupported,
        ),
        binding(
            control = PlayerControl.SCREENSHOT,
            icon = NextIcons.Screenshot,
            label = stringResource(R.string.take_screenshot),
            menuTestTag = "menu_item_screenshot",
            action = PlayerControlAction.Execute(onScreenshot),
            isEnabled = !isTakingScreenshot,
        ),
        binding(
            control = PlayerControl.BACKGROUND_PLAY,
            icon = NextIcons.Headset,
            label = stringResource(R.string.background_play),
            menuTestTag = "menu_item_background",
            action = PlayerControlAction.Execute(onPlayInBackground),
        ),
        binding(
            control = PlayerControl.LOOP,
            icon = NextIcons.Loop,
            label = stringResource(R.string.loop_mode),
            menuTestTag = "menu_item_loop",
            action = PlayerControlAction.OpenPanel(MenuRoute.LoopMode),
        ),
        binding(
            control = PlayerControl.SHUFFLE,
            icon = NextIcons.Shuffle,
            label = stringResource(R.string.shuffle),
            menuTestTag = "menu_item_shuffle",
            action = PlayerControlAction.OpenPanel(MenuRoute.ShuffleMode),
        ),
    )
    return bindings.associateBy(PlayerControlBinding::control)
}

// 按编排顺序取出可渲染的绑定
internal fun Map<PlayerControl, PlayerControlBinding>.resolve(controls: List<PlayerControl>): List<PlayerControlBinding> = controls
    .mapNotNull { control -> get(control) }
    .filter { it.isAvailable }

// 老角落按钮的 tag 保持不变，避免打断现有调试与自动化定位
private val LegacyCornerTestTags = mapOf(
    PlayerControl.ROTATE to "btn_rotate_modern",
    PlayerControl.PLAYLIST to "btn_playlist_modern",
    PlayerControl.PLAYBACK_SPEED to "btn_speed_modern",
)

private fun PlayerControl.cornerTestTag(): String = LegacyCornerTestTags[this] ?: "btn_corner_${name.lowercase()}"

private fun binding(
    control: PlayerControl,
    icon: ImageVector,
    label: String,
    menuTestTag: String,
    action: PlayerControlAction,
    isAvailable: Boolean = true,
    isEnabled: Boolean = true,
): PlayerControlBinding = PlayerControlBinding(
    control = control,
    icon = icon,
    label = label,
    menuTestTag = menuTestTag,
    cornerTestTag = control.cornerTestTag(),
    action = action,
    isAvailable = isAvailable,
    isEnabled = isEnabled,
)
