package one.only.player.feature.player.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import one.only.player.core.common.Utils
import one.only.player.core.ui.R
import one.only.player.core.ui.designsystem.AppIcons
import one.only.player.feature.player.state.rememberPlaylistState
import one.only.player.feature.player.ui.panel.rememberPlayerPanelTokens
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(UnstableApi::class)
@Composable
fun BoxScope.PlaylistView(
    modifier: Modifier = Modifier,
    shouldShow: Boolean,
    player: Player,
) {
    OverlayView(
        modifier = modifier,
        shouldShow = shouldShow,
        title = stringResource(R.string.now_playing),
    ) {
        PlaylistContent(
            isVisible = shouldShow,
            player = player,
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlaylistContent(
    isVisible: Boolean,
    player: Player,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val playlistState = rememberPlaylistState(player)
    val playlistEntries = playlistState.playlist.toPlaylistEntries()
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        playlistState.moveItem(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LaunchedEffect(isVisible) {
        if (isVisible && playlistState.playlist.isNotEmpty()) {
            val currentIndex = playlistState.currentMediaItemIndex
            if (currentIndex in playlistState.playlist.indices) {
                lazyListState.scrollToItem(currentIndex)
            }
        }
    }

    if (playlistEntries.isEmpty()) {
        EmptyPlaylistView()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(
                items = playlistEntries,
                key = { _, item -> item.key },
            ) { index, entry ->
                ReorderableItem(
                    state = reorderableLazyListState,
                    key = entry.key,
                ) {
                    val isCurrentItem = index == playlistState.currentMediaItemIndex
                    PlaylistItemView(
                        mediaItem = entry.mediaItem,
                        itemIndex = index,
                        isCurrentItem = isCurrentItem,
                        canDelete = playlistEntries.size > 1,
                        onClick = { playlistState.seekToItem(index) },
                        onDelete = { playlistState.removeItem(index) },
                    )
                }
            }
        }
    }
}

private data class PlaylistEntry(
    val key: String,
    val mediaItem: MediaItem,
)

private fun List<MediaItem>.toPlaylistEntries(): List<PlaylistEntry> {
    val mediaIdOccurrences = mutableMapOf<String, Int>()
    return map { mediaItem ->
        val mediaId = mediaItem.mediaId
        val occurrence = mediaIdOccurrences.getOrDefault(mediaId, 0)
        mediaIdOccurrences[mediaId] = occurrence + 1
        PlaylistEntry(
            key = "$mediaId#$occurrence",
            mediaItem = mediaItem,
        )
    }
}

@Composable
private fun ReorderableCollectionItemScope.PlaylistItemView(
    mediaItem: MediaItem,
    itemIndex: Int,
    isCurrentItem: Boolean,
    canDelete: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current
    val tokens = rememberPlayerPanelTokens()
    val cardShape = RoundedCornerShape(16.dp)
    val cardColor = if (isCurrentItem) tokens.itemSelectedColor else tokens.itemColor
    val contentColor = if (isCurrentItem) tokens.itemSelectedContentColor else tokens.itemContentColor
    val secondaryColor = if (isCurrentItem) {
        tokens.itemSelectedContentColor.copy(alpha = 0.75f)
    } else {
        tokens.secondaryContentColor
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("playlist_item_$itemIndex")
            .draggableHandle(
                onDragStarted = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                },
                onDragStopped = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                },
                interactionSource = interactionSource,
                dragGestureDetector = DragGestureDetector.LongPress,
            )
            .clip(cardShape)
            .background(cardColor)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MiuixIcon(
            painter = painterResource(R.drawable.ic_drag_handle),
            contentDescription = stringResource(R.string.reorder),
            tint = secondaryColor,
        )
        ThumbnailView(
            mediaItem = mediaItem,
            modifier = Modifier
                .width(min(100.dp, LocalConfiguration.current.screenWidthDp.dp * 0.3f)),
        )
        MiuixText(
            text = mediaItem.mediaMetadata.title?.toString() ?: stringResource(R.string.unknown),
            maxLines = 2,
            style = MiuixTheme.textStyles.body1,
            fontWeight = if (isCurrentItem) FontWeight.SemiBold else FontWeight.Medium,
            overflow = TextOverflow.Ellipsis,
            color = contentColor,
            modifier = Modifier.weight(1f),
        )
        if (canDelete) {
            MiuixIconButton(onClick = onDelete) {
                MiuixIcon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.remove),
                    tint = secondaryColor,
                )
            }
        }
    }
}

@Composable
private fun ThumbnailView(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem,
) {
    val context = LocalContext.current
    val tokens = rememberPlayerPanelTokens()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(tokens.contentColor.copy(alpha = 0.1f))
            .aspectRatio(16f / 10f),
    ) {
        MiuixIcon(
            imageVector = AppIcons.Video,
            contentDescription = null,
            tint = tokens.secondaryContentColor.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.5f),
        )

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(mediaItem.mediaMetadata.artworkData ?: mediaItem.mediaMetadata.artworkUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        mediaItem.mediaMetadata.durationMs?.let { durationMs ->
            if (durationMs > 0) {
                MiuixText(
                    text = Utils.formatDurationMillis(durationMs),
                    style = MiuixTheme.textStyles.footnote2,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(vertical = 1.dp, horizontal = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyPlaylistView() {
    val tokens = rememberPlayerPanelTokens()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MiuixIcon(
            imageVector = AppIcons.Video,
            contentDescription = null,
            tint = tokens.secondaryContentColor.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxSize(0.3f),
        )
        MiuixText(
            text = stringResource(R.string.no_videos_in_queue),
            style = MiuixTheme.textStyles.body2,
            color = tokens.secondaryContentColor,
            textAlign = TextAlign.Center,
        )
    }
}
