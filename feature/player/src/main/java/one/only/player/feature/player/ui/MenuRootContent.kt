package one.only.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import one.only.player.core.ui.R
import one.only.player.core.ui.designsystem.NextIcons
import one.only.player.feature.player.ui.panel.rememberPlayerPanelTokens
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class MenuTile(
    val icon: ImageVector,
    val label: String,
    val testTag: String,
    val isEnabled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
fun MenuRootContent(
    isPipSupported: Boolean,
    isTakingScreenshot: Boolean,
    hasChapters: Boolean,
    onNavigate: (MenuRoute) -> Unit,
    onPictureInPictureClick: () -> Unit,
    onScreenshotClick: () -> Unit,
    onPlayInBackgroundClick: () -> Unit,
) {
    val tiles = buildList {
        add(
            MenuTile(
                icon = NextIcons.Subtitle,
                label = stringResource(R.string.select_subtitle_track),
                testTag = "menu_item_subtitle",
                onClick = { onNavigate(MenuRoute.Subtitle) },
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.Audio,
                label = stringResource(R.string.select_audio_track),
                testTag = "menu_item_audio",
                onClick = { onNavigate(MenuRoute.Audio) },
            ),
        )
        if (hasChapters) {
            add(
                MenuTile(
                    icon = NextIcons.PlaylistPlay,
                    label = stringResource(R.string.chapters),
                    testTag = "menu_item_chapters",
                    onClick = { onNavigate(MenuRoute.Chapters) },
                ),
            )
        }
        add(
            MenuTile(
                icon = NextIcons.Frame,
                label = stringResource(R.string.video_zoom),
                testTag = "menu_item_video_scale",
                onClick = { onNavigate(MenuRoute.VideoContentScale) },
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.Decoder,
                label = stringResource(R.string.decoder_priority),
                testTag = "menu_item_decoder",
                onClick = { onNavigate(MenuRoute.Decoder) },
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.Sensitivity,
                label = stringResource(R.string.video_filters),
                testTag = "menu_item_video_filters",
                onClick = { onNavigate(MenuRoute.VideoFilters) },
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.Timer,
                label = stringResource(R.string.sleep_timer),
                testTag = "menu_item_sleep_timer",
                onClick = { onNavigate(MenuRoute.SleepTimer) },
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.History,
                label = stringResource(R.string.playback_marks),
                testTag = "menu_item_playback_marks",
                onClick = { onNavigate(MenuRoute.PlaybackMarks) },
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.Lock,
                label = stringResource(R.string.controls_lock_switch),
                testTag = "menu_item_lock",
                onClick = { onNavigate(MenuRoute.ControlLock) },
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.VolumeUp,
                label = stringResource(R.string.mute_switch),
                testTag = "menu_item_mute",
                onClick = { onNavigate(MenuRoute.Mute) },
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.Style,
                label = stringResource(R.string.ambience_mode),
                testTag = "menu_item_ambience",
                onClick = { onNavigate(MenuRoute.AmbienceMode) },
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.Size,
                label = stringResource(R.string.mirror_video),
                testTag = "menu_item_mirror_video",
                onClick = { onNavigate(MenuRoute.MirrorVideo) },
            ),
        )
        if (isPipSupported) {
            add(
                MenuTile(
                    icon = NextIcons.Pip,
                    label = stringResource(R.string.pip_settings),
                    testTag = "menu_item_pip",
                    onClick = onPictureInPictureClick,
                ),
            )
        }
        add(
            MenuTile(
                icon = NextIcons.Screenshot,
                label = stringResource(R.string.take_screenshot),
                testTag = "menu_item_screenshot",
                isEnabled = !isTakingScreenshot,
                onClick = onScreenshotClick,
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.Headset,
                label = stringResource(R.string.background_play),
                testTag = "menu_item_background",
                onClick = onPlayInBackgroundClick,
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.Loop,
                label = stringResource(R.string.loop_mode),
                testTag = "menu_item_loop",
                onClick = { onNavigate(MenuRoute.LoopMode) },
            ),
        )
        add(
            MenuTile(
                icon = NextIcons.Shuffle,
                label = stringResource(R.string.shuffle),
                testTag = "menu_item_shuffle",
                onClick = { onNavigate(MenuRoute.ShuffleMode) },
            ),
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 92.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = tiles,
            key = MenuTile::testTag,
        ) { tile ->
            MenuTileButton(tile = tile)
        }
    }
}

@Composable
private fun MenuTileButton(tile: MenuTile) {
    val tokens = rememberPlayerPanelTokens()
    val shape = RoundedCornerShape(tokens.tileCornerRadius)
    val contentAlpha = if (tile.isEnabled) 1f else 0.4f
    Column(
        modifier = Modifier
            .testTag(tile.testTag)
            .semantics { contentDescription = tile.testTag }
            .fillMaxWidth()
            .height(84.dp)
            .clip(shape)
            .background(tokens.itemColor)
            .clickable(
                enabled = tile.isEnabled,
                onClick = tile.onClick,
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MiuixIcon(
            imageVector = tile.icon,
            contentDescription = null,
            tint = tokens.itemContentColor.copy(alpha = contentAlpha),
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.size(6.dp))
        MiuixText(
            text = tile.label,
            color = tokens.itemContentColor.copy(alpha = contentAlpha),
            style = MiuixTheme.textStyles.footnote1,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
