package one.only.player.feature.player.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlin.time.Duration.Companion.milliseconds
import one.only.player.core.ui.R
import one.only.player.core.ui.components.NextSegmentedListItem
import one.only.player.core.ui.designsystem.NextIcons
import one.only.player.feature.player.extensions.formatted
import one.only.player.feature.player.model.VideoChapter
import one.only.player.feature.player.model.currentChapterIndex
import one.only.player.feature.player.ui.panel.rememberPlayerPanelTokens
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BoxScope.ChaptersView(
    shouldShow: Boolean,
    chapters: List<VideoChapter>,
    positionMs: Long,
    mediaUri: Uri?,
    onChapterClick: (VideoChapter) -> Unit,
) {
    OverlayView(
        shouldShow = shouldShow,
        title = stringResource(R.string.chapters),
        testTag = "panel_chapters",
    ) {
        ChaptersContent(
            isVisible = shouldShow,
            chapters = chapters,
            positionMs = positionMs,
            mediaUri = mediaUri,
            onChapterClick = onChapterClick,
        )
    }
}

@Composable
fun ChaptersContent(
    isVisible: Boolean,
    chapters: List<VideoChapter>,
    positionMs: Long,
    mediaUri: Uri?,
    onChapterClick: (VideoChapter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentChapterIndex = chapters.currentChapterIndex(positionMs)
    val listState = rememberLazyListState()
    var previousChapterIndex by remember(chapters) { mutableStateOf<Int?>(null) }

    LaunchedEffect(isVisible, currentChapterIndex, chapters.size) {
        if (!isVisible || currentChapterIndex == null) {
            previousChapterIndex = null
            return@LaunchedEffect
        }

        val targetIndex = (currentChapterIndex - 1).coerceAtLeast(0)
        if (previousChapterIndex == null) {
            listState.scrollToItem(targetIndex)
        } else if (previousChapterIndex != currentChapterIndex) {
            listState.animateScrollToItem(targetIndex)
        }
        previousChapterIndex = currentChapterIndex
    }

    if (chapters.isEmpty()) {
        EmptyChaptersView(modifier = modifier)
        return
    }

    val tokens = rememberPlayerPanelTokens()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("chapter_list"),
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.itemSpacing),
    ) {
        itemsIndexed(
            items = chapters,
            key = { _, chapter -> chapter.startTimeMs },
        ) { index, chapter ->
            ChapterItem(
                chapter = chapter,
                positionMs = positionMs,
                mediaUri = mediaUri,
                isCurrentChapter = index == currentChapterIndex,
                isFirstItem = index == 0,
                isLastItem = index == chapters.lastIndex,
                onClick = { onChapterClick(chapter) },
            )
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: VideoChapter,
    positionMs: Long,
    mediaUri: Uri?,
    isCurrentChapter: Boolean,
    isFirstItem: Boolean,
    isLastItem: Boolean,
    onClick: () -> Unit,
) {
    val tokens = rememberPlayerPanelTokens()
    val chapterNumber = stringResource(R.string.chapter_number, chapter.index + 1)
    val contentColor = if (isCurrentChapter) tokens.itemSelectedContentColor else tokens.itemContentColor
    val secondaryColor = if (isCurrentChapter) {
        tokens.itemSelectedContentColor.copy(alpha = 0.7f)
    } else {
        tokens.secondaryContentColor
    }
    NextSegmentedListItem(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chapter_item_${chapter.index}"),
        isSelected = isCurrentChapter,
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        contentPadding = PaddingValues(8.dp),
        containerColor = tokens.itemColor,
        selectedContainerColor = tokens.itemSelectedColor,
        onClick = onClick,
        leadingContent = {
            ChapterThumbnail(
                chapter = chapter,
                positionMs = positionMs,
                mediaUri = mediaUri,
                isCurrentChapter = isCurrentChapter,
                contentDescription = stringResource(
                    R.string.chapter_preview_content_description,
                    chapter.title ?: chapterNumber,
                ),
            )
        },
        overlineContent = if (chapter.title != null) {
            {
                MiuixText(
                    text = chapterNumber,
                    style = MiuixTheme.textStyles.footnote2,
                    color = secondaryColor,
                )
            }
        } else {
            null
        },
        content = {
            MiuixText(
                text = chapter.title ?: chapterNumber,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MiuixTheme.textStyles.body1,
                fontWeight = if (isCurrentChapter) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
            )
        },
        supportingContent = {
            MiuixText(
                text = stringResource(
                    R.string.chapter_time_range,
                    chapter.startTimeMs.milliseconds.formatted(),
                    chapter.endTimeMs.milliseconds.formatted(),
                ),
                style = MiuixTheme.textStyles.footnote1,
                color = secondaryColor,
            )
        },
        trailingContent = {
            if (isCurrentChapter) {
                MiuixIcon(
                    imageVector = NextIcons.Play,
                    contentDescription = stringResource(R.string.current_chapter),
                    tint = tokens.itemSelectedContentColor,
                )
            }
        },
    )
}

@Composable
private fun ChapterThumbnail(
    chapter: VideoChapter,
    positionMs: Long,
    mediaUri: Uri?,
    isCurrentChapter: Boolean,
    contentDescription: String,
) {
    val context = LocalContext.current
    val tokens = rememberPlayerPanelTokens()
    val shape = RoundedCornerShape(8.dp)
    val width = min(112.dp, LocalConfiguration.current.screenWidthDp.dp * 0.3f)
    val cacheKey = remember(mediaUri, chapter.startTimeMs) {
        "$mediaUri#chapter=${chapter.startTimeMs}"
    }
    val imageRequest = remember(mediaUri, chapter.startTimeMs) {
        mediaUri?.let { uri ->
            ImageRequest.Builder(context)
                .data(uri)
                .memoryCacheKey(cacheKey)
                .diskCacheKey(cacheKey)
                .apply {
                    extras[ChapterThumbnailPositionMsExtra] = chapter.startTimeMs
                }
                .crossfade(true)
                .build()
        }
    }
    val borderModifier = if (isCurrentChapter) {
        Modifier.border(2.dp, tokens.accentColor, shape)
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .width(width)
            .aspectRatio(16f / 9f)
            .clip(shape)
            .background(tokens.contentColor.copy(alpha = 0.1f))
            .then(borderModifier),
        contentAlignment = Alignment.Center,
    ) {
        MiuixIcon(
            imageVector = NextIcons.Video,
            contentDescription = null,
            tint = tokens.secondaryContentColor.copy(alpha = 0.35f),
        )
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        MiuixText(
            text = chapter.startTimeMs.milliseconds.formatted(),
            style = MiuixTheme.textStyles.footnote2,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 5.dp)
                .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp),
        )
        if (isCurrentChapter && chapter.endTimeMs > chapter.startTimeMs) {
            val progress = ((positionMs - chapter.startTimeMs).toFloat() / (chapter.endTimeMs - chapter.startTimeMs))
                .coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp),
                color = tokens.accentColor,
                trackColor = Color.Transparent,
            )
        }
    }
}

@Composable
private fun EmptyChaptersView(modifier: Modifier = Modifier) {
    val tokens = rememberPlayerPanelTokens()
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("chapters_empty"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MiuixIcon(
                imageVector = NextIcons.PlaylistPlay,
                contentDescription = null,
                tint = tokens.secondaryContentColor,
            )
            MiuixText(
                text = stringResource(R.string.no_chapters),
                style = MiuixTheme.textStyles.body2,
                color = tokens.secondaryContentColor,
            )
        }
    }
}
