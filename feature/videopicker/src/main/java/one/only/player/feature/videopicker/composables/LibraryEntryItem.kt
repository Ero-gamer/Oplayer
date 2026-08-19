package one.only.player.feature.videopicker.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import one.only.player.core.ui.R
import one.only.player.core.ui.components.NextCardListItem
import one.only.player.core.ui.designsystem.NextIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryEntryItem(
    title: String,
    chips: List<String>,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable () -> Unit,
    overflowActions: List<MenuAction> = emptyList(),
) {
    var shouldShowOverflow by remember { mutableStateOf(false) }

    NextCardListItem(
        modifier = modifier.testTag(testTag),
        isSelected = false,
        containerColor = Color.Transparent,
        contentPadding = PaddingValues(MediaItemContentPadding),
        onClick = onClick,
        leadingContent = leadingContent,
        trailingContent = {
            if (overflowActions.isNotEmpty()) {
                Box {
                    IconButton(
                        onClick = { shouldShowOverflow = true },
                        holdDownState = shouldShowOverflow,
                        modifier = Modifier.testTag("${testTag}_more"),
                    ) {
                        Icon(
                            imageVector = NextIcons.MoreVert,
                            contentDescription = stringResource(R.string.more_actions),
                            tint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                    MenuActionsPopup(
                        expanded = shouldShowOverflow,
                        onDismissRequest = { shouldShowOverflow = false },
                        groups = listOf(overflowActions),
                    )
                }
            }
        },
        content = {
            Text(
                text = title,
                maxLines = 2,
                style = MiuixTheme.textStyles.title4,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = chips.takeIf { it.isNotEmpty() }?.let {
            {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        chips.forEach { chip ->
                            InfoChip(text = chip)
                        }
                    }
                }
            }
        },
    )
}

@Composable
fun libraryListThumbWidth(): Dp = min(150.dp, LocalConfiguration.current.screenWidthDp.dp * 0.35f)

@Composable
fun FolderThumbnail(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(horizontal = 8.dp)) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.folder_thumb),
            contentDescription = null,
            tint = MiuixTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .width(min(90.dp, LocalConfiguration.current.screenWidthDp.dp * 0.3f))
                .aspectRatio(20 / 17f),
        )
    }
}

@Composable
fun LibraryIconThumb(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(libraryListThumbWidth())
            .clip(RoundedCornerShape(8.dp))
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .aspectRatio(16f / 10f),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.size(28.dp),
        )
    }
}
