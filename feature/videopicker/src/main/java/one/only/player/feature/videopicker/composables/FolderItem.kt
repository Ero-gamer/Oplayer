package one.only.player.feature.videopicker.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import one.only.player.core.common.Utils
import one.only.player.core.model.ApplicationPreferences
import one.only.player.core.model.Folder
import one.only.player.core.model.MediaLayoutMode
import one.only.player.core.ui.R
import one.only.player.core.ui.components.CardListItem
import one.only.player.core.ui.theme.OnlyPlayerTheme
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun FolderItem(
    folder: Folder,
    isRecentlyPlayedFolder: Boolean,
    preferences: ApplicationPreferences,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    when (preferences.mediaLayoutMode) {
        MediaLayoutMode.LIST -> FolderListItem(
            folder = folder,
            isRecentlyPlayedFolder = isRecentlyPlayedFolder,
            preferences = preferences,
            modifier = modifier,
            isSelected = isSelected,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        MediaLayoutMode.GRID -> FolderGridItem(
            folder = folder,
            isRecentlyPlayedFolder = isRecentlyPlayedFolder,
            preferences = preferences,
            modifier = modifier,
            isSelected = isSelected,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FolderListItem(
    folder: Folder,
    isRecentlyPlayedFolder: Boolean,
    preferences: ApplicationPreferences,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    CardListItem(
        modifier = modifier.testTag("item_folder_${folder.name}"),
        isSelected = false,
        containerColor = Color.Transparent,
        contentPadding = PaddingValues(MediaItemContentPadding),
        onClick = onClick,
        onLongClick = onLongClick,
        leadingContent = {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.folder_thumb),
                contentDescription = "",
                tint = MiuixTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(min(90.dp, LocalConfiguration.current.screenWidthDp.dp * 0.3f))
                    .aspectRatio(20 / 17f),
            )
        },
        trailingContent = {
            SelectionCheckIndicator(isSelected = isSelected)
        },
        content = {
            Text(
                text = folder.name,
                maxLines = 3,
                style = MiuixTheme.textStyles.title4,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (preferences.shouldShowPathField) {
                    Text(
                        text = folder.path.substringBeforeLast("/"),
                        maxLines = 2,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    if (folder.mediaList.isNotEmpty()) {
                        InfoChip(
                            text = "${folder.mediaList.size} " +
                                stringResource(id = R.string.video.takeIf { folder.mediaList.size == 1 } ?: R.string.videos),
                        )
                    }
                    if (folder.folderList.isNotEmpty()) {
                        InfoChip(
                            text = "${folder.folderList.size} " +
                                stringResource(id = R.string.folder.takeIf { folder.folderList.size == 1 } ?: R.string.folders),
                        )
                    }
                    if (preferences.shouldShowSizeField) {
                        InfoChip(text = Utils.formatFileSize(folder.mediaSize))
                    }
                }
            }
        },
    )
}

@Composable
private fun FolderGridItem(
    folder: Folder,
    isRecentlyPlayedFolder: Boolean,
    preferences: ApplicationPreferences,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    CardListItem(
        modifier = modifier
            .fillMaxWidth()
            .testTag("item_folder_${folder.name}"),
        isSelected = false,
        containerColor = Color.Transparent,
        contentPadding = PaddingValues(MediaItemContentPadding),
        onClick = onClick,
        onLongClick = onLongClick,
        trailingContent = {
            SelectionCheckIndicator(isSelected = isSelected)
        },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.folder_thumb),
                    contentDescription = "",
                    tint = MiuixTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .width(min(90.dp, LocalConfiguration.current.screenWidthDp.dp * 0.3f))
                        .aspectRatio(20 / 17f),
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = folder.name,
                        maxLines = 3,
                        style = MiuixTheme.textStyles.title4,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isRecentlyPlayedFolder && preferences.shouldMarkLastPlayedMedia) {
                            MiuixTheme.colorScheme.primary
                        } else {
                            MiuixTheme.colorScheme.onSurface
                        },
                        textAlign = TextAlign.Center,
                    )
                    val mediaCount = if (folder.mediaList.isNotEmpty()) {
                        "${folder.mediaList.size} " + stringResource(id = R.string.video.takeIf { folder.mediaList.size == 1 } ?: R.string.videos)
                    } else {
                        null
                    }
                    val folderCount = if (folder.folderList.isNotEmpty()) {
                        "${folder.folderList.size} " + stringResource(id = R.string.folder.takeIf { folder.folderList.size == 1 } ?: R.string.folders)
                    } else {
                        null
                    }

                    Text(
                        text = buildString {
                            mediaCount?.let {
                                append(it)
                                folderCount?.let {
                                    append(", ")
                                    append("\u00A0")
                                }
                            }
                            folderCount?.let {
                                append(it)
                            }
                        },
                        maxLines = 2,
                        style = MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.Normal),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    )
}

@PreviewLightDark
@Composable
fun FolderItemRecentlyPlayedPreview() {
    OnlyPlayerTheme {
        FolderListItem(
            folder = Folder.sample,
            preferences = ApplicationPreferences(),
            isRecentlyPlayedFolder = true,
        )
    }
}

@PreviewLightDark
@Composable
fun FolderItemPreview() {
    OnlyPlayerTheme {
        FolderListItem(
            folder = Folder.sample.copy(folderList = listOf(Folder.sample)),
            preferences = ApplicationPreferences(),
            isRecentlyPlayedFolder = false,
        )
    }
}

@PreviewLightDark
@Composable
fun FolderGridViewPreview() {
    OnlyPlayerTheme {
        FolderGridItem(
            folder = Folder.sample.copy(folderList = listOf(Folder.sample)),
            preferences = ApplicationPreferences(),
            isRecentlyPlayedFolder = true,
        )
    }
}
