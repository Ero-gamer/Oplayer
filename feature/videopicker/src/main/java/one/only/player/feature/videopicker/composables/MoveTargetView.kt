package one.only.player.feature.videopicker.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import one.only.player.core.common.Utils
import one.only.player.core.media.services.MediaMoveSpaceCheck
import one.only.player.core.media.services.MediaMoveTargetDirectory
import one.only.player.core.media.services.MediaMoveTargetDirectoryContent
import one.only.player.core.ui.R
import one.only.player.core.ui.components.ListSectionTitle
import one.only.player.core.ui.components.NextSegmentedListItem
import one.only.player.core.ui.designsystem.NextIcons
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MoveTargetView(
    content: MediaMoveTargetDirectoryContent,
    spaceCheck: MediaMoveSpaceCheck?,
    canMoveHere: Boolean,
    isMoving: Boolean,
    contentPadding: PaddingValues,
    onDirectoryClick: (MediaMoveTargetDirectory) -> Unit,
    onMoveHere: () -> Unit,
) {
    val currentDirectory = content.currentDirectory

    Column(modifier = Modifier.fillMaxSize()) {
        currentDirectory?.let { directory ->
            CurrentMoveDirectory(
                directory = directory,
                spaceCheck = spaceCheck,
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (currentDirectory != null && content.directories.isEmpty()) {
                MediaMessageState(
                    icon = NextIcons.Folder,
                    title = stringResource(R.string.empty_directory),
                    contentPadding = PaddingValues(bottom = 8.dp),
                )
            } else {
                MoveTargetDirectoryList(
                    directories = content.directories,
                    isStorageRoot = currentDirectory == null,
                    onDirectoryClick = onDirectoryClick,
                )
            }
        }

        if (currentDirectory != null) {
            MoveTargetActionBar(
                isEnabled = canMoveHere,
                isMoving = isMoving,
                bottomPadding = contentPadding.calculateBottomPadding(),
                onMoveHere = onMoveHere,
            )
        }
    }
}

@Composable
private fun CurrentMoveDirectory(
    directory: MediaMoveTargetDirectory,
    spaceCheck: MediaMoveSpaceCheck?,
) {
    val statusText = moveSpaceStatusText(directory = directory, spaceCheck = spaceCheck)
    val isInsufficient = spaceCheck?.hasEnoughSpace == false

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("move_space_status"),
        color = MiuixTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MoveTargetIcon(
                iconSize = 26.dp,
                containerSize = 46.dp,
                isInsufficient = isInsufficient,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = directory.name,
                    style = MiuixTheme.textStyles.title4,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = directory.path,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                statusText?.let { text ->
                    Text(
                        text = text,
                        style = MiuixTheme.textStyles.body2,
                        color = if (isInsufficient) {
                            MiuixTheme.colorScheme.error
                        } else {
                            MiuixTheme.colorScheme.primary
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun MoveTargetDirectoryList(
    directories: List<MediaMoveTargetDirectory>,
    isStorageRoot: Boolean,
    onDirectoryClick: (MediaMoveTargetDirectory) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            top = 4.dp,
            end = 12.dp,
            bottom = 8.dp,
        ),
    ) {
        item {
            ListSectionTitle(
                text = stringResource(if (isStorageRoot) R.string.storage_locations else R.string.folders),
                contentPadding = PaddingValues(
                    start = 4.dp,
                    top = 4.dp,
                    bottom = 8.dp,
                ),
            )
        }
        itemsIndexed(
            items = directories,
            key = { _, directory -> directory.path },
        ) { index, directory ->
            if (isStorageRoot && directory.storage != null) {
                StorageTargetItem(
                    directory = directory,
                    isFirstItem = index == 0,
                    isLastItem = index == directories.lastIndex,
                    onClick = { onDirectoryClick(directory) },
                )
            } else {
                FolderTargetItem(
                    directory = directory,
                    isFirstItem = index == 0,
                    isLastItem = index == directories.lastIndex,
                    onClick = { onDirectoryClick(directory) },
                )
            }
        }
    }
}

@Composable
private fun StorageTargetItem(
    directory: MediaMoveTargetDirectory,
    isFirstItem: Boolean,
    isLastItem: Boolean,
    onClick: () -> Unit,
) {
    val storage = requireNotNull(directory.storage)
    val availableBytes = storage.availableBytes
    val totalBytes = storage.totalBytes
    val availablePercent = if (availableBytes != null && totalBytes != null && totalBytes > 0L) {
        ((availableBytes.toDouble() / totalBytes) * 100).toInt().coerceIn(0, 100)
    } else {
        null
    }
    val usedFraction = availablePercent?.let { percent -> 1f - percent / 100f }

    NextSegmentedListItem(
        modifier = Modifier.testTag("item_move_target_${directory.path}"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        onClick = onClick,
        leadingContent = {
            MoveTargetIcon(
                iconSize = 28.dp,
                containerSize = 48.dp,
            )
        },
        content = {
            Text(
                text = storage.name,
                style = MiuixTheme.textStyles.title4,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = directory.path,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                usedFraction?.let { progress ->
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (availableBytes != null && availablePercent != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.storage_available_percent,
                                Utils.formatFileSize(availableBytes),
                                availablePercent,
                            ),
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        totalBytes?.let { bytes ->
                            Text(
                                text = stringResource(R.string.storage_total, Utils.formatFileSize(bytes)),
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun FolderTargetItem(
    directory: MediaMoveTargetDirectory,
    isFirstItem: Boolean,
    isLastItem: Boolean,
    onClick: () -> Unit,
) {
    NextSegmentedListItem(
        modifier = Modifier.testTag("item_move_target_${directory.path}"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        onClick = onClick,
        leadingContent = {
            Icon(
                imageVector = NextIcons.Folder,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        },
        content = {
            Text(
                text = directory.name,
                style = MiuixTheme.textStyles.title4,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@Composable
private fun MoveTargetIcon(
    iconSize: androidx.compose.ui.unit.Dp,
    containerSize: androidx.compose.ui.unit.Dp,
    isInsufficient: Boolean = false,
) {
    Surface(
        modifier = Modifier.size(containerSize),
        color = MiuixTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isInsufficient) NextIcons.Priority else NextIcons.DriveFileMove,
                contentDescription = null,
                tint = if (isInsufficient) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Composable
private fun MoveTargetActionBar(
    isEnabled: Boolean,
    isMoving: Boolean,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onMoveHere: () -> Unit,
) {
    Surface(
        color = MiuixTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = onMoveHere,
            enabled = isEnabled && !isMoving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = bottomPadding + 12.dp)
                .height(50.dp)
                .testTag("btn_move_here"),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = NextIcons.DriveFileMove,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(if (isMoving) R.string.moving else R.string.move_here),
                    style = MiuixTheme.textStyles.button,
                )
            }
        }
    }
}

@Composable
private fun moveSpaceStatusText(
    directory: MediaMoveTargetDirectory,
    spaceCheck: MediaMoveSpaceCheck?,
): String? {
    val storage = directory.storage ?: return null
    val availableBytes = spaceCheck?.availableBytes ?: storage.availableBytes
    val totalBytes = storage.totalBytes
    return when {
        spaceCheck?.hasEnoughSpace == false && availableBytes != null -> stringResource(
            R.string.move_insufficient_space,
            Utils.formatFileSize(spaceCheck.requiredBytes),
            Utils.formatFileSize(availableBytes),
        )
        spaceCheck != null && spaceCheck.requiredBytes > 0L && availableBytes != null -> stringResource(
            R.string.move_required_space,
            Utils.formatFileSize(spaceCheck.requiredBytes),
            Utils.formatFileSize(availableBytes),
        )
        availableBytes != null && totalBytes != null -> stringResource(
            R.string.storage_available_total,
            Utils.formatFileSize(availableBytes),
            Utils.formatFileSize(totalBytes),
        )
        availableBytes != null -> stringResource(
            R.string.storage_available,
            Utils.formatFileSize(availableBytes),
        )
        else -> null
    }
}
