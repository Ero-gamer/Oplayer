package one.only.player.feature.videopicker.composables

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.File
import one.only.player.core.common.extensions.canonicalPathOrSelf
import one.only.player.core.common.extensions.getStorageVolumes
import one.only.player.core.common.extensions.prettyName
import one.only.player.core.ui.R
import one.only.player.core.ui.designsystem.AppIcons
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Immutable
internal data class MediaPickerPathEntry(
    val path: String?,
    val label: String,
    val depth: Int,
)

internal fun buildMediaPickerPathEntries(
    context: Context,
    folderPath: String?,
    currentFolderName: String?,
    rootLabel: String,
): List<MediaPickerPathEntry> {
    val normalizedPath = folderPath
        ?.takeIf(String::isNotBlank)
        ?.let(::normalizePath)
        ?: return emptyList()
    val storageRoot = context.getStorageVolumes()
        .map { volume -> normalizePath(volume.path) }
        .firstOrNull { root ->
            normalizedPath == root || normalizedPath.startsWith("$root/")
        }
    val paths = buildList {
        var path = normalizedPath
        while (path != "/") {
            add(path)
            if (path == storageRoot) break
            path = path.substringBeforeLast('/').ifBlank { "/" }
        }
    }.asReversed()

    return buildList {
        add(
            MediaPickerPathEntry(
                path = null,
                label = rootLabel,
                depth = 0,
            ),
        )
        paths.forEachIndexed { index, path ->
            val file = File(path)
            add(
                MediaPickerPathEntry(
                    path = path,
                    label = if (path == normalizedPath) {
                        currentFolderName ?: file.prettyName
                    } else {
                        file.prettyName
                    },
                    depth = index + 1,
                ),
            )
        }
    }
}

@Composable
internal fun MediaPickerPathPanel(
    isExpanded: Boolean,
    entries: List<MediaPickerPathEntry>,
    onDismissRequest: () -> Unit,
    onPathSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVisible = isExpanded
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(SCRIM_DURATION_MILLIS)),
            exit = fadeOut(tween(SCRIM_DURATION_MILLIS)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MiuixTheme.colorScheme.windowDimming)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest,
                    )
                    .testTag("scrim_path_panel"),
            )
        }
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(PANEL_ENTER_MILLIS)) +
                expandVertically(tween(PANEL_ENTER_MILLIS), expandFrom = Alignment.Top),
            exit = fadeOut(tween(PANEL_EXIT_MILLIS)) +
                shrinkVertically(tween(PANEL_EXIT_MILLIS), shrinkTowards = Alignment.Top),
        ) {
            PathPanelContent(entries = entries, onPathSelected = onPathSelected)
        }
    }
}

@Composable
private fun PathPanelContent(
    entries: List<MediaPickerPathEntry>,
    onPathSelected: (String?) -> Unit,
) {
    val fullPath = entries.lastOrNull()?.path ?: return
    var shouldShowAllLevels by remember(entries) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = PanelBottomMargin)
            .dropShadow(shape = PanelShape, shadow = PanelShadow)
            .background(color = MiuixTheme.colorScheme.surface, shape = PanelShape)
            .padding(vertical = PanelPadding)
            .testTag("panel_path"),
    ) {
        PathPanelHeader(
            fullPath = fullPath,
            shouldShowAllLevels = shouldShowAllLevels,
            onCollapseClick = { shouldShowAllLevels = false },
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = PanelPadding, vertical = PanelPadding),
            color = MiuixTheme.colorScheme.dividerLine,
        )
        PathLevelList(
            entries = entries,
            shouldShowAllLevels = shouldShowAllLevels,
            onExpandAllClick = { shouldShowAllLevels = true },
            onPathSelected = onPathSelected,
        )
    }
}

@Composable
private fun PathPanelHeader(
    fullPath: String,
    shouldShowAllLevels: Boolean,
    onCollapseClick: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = PanelPadding + RowPadding),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.current_location),
                style = MiuixTheme.textStyles.subtitle,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = fullPath,
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("text_path_panel_full_path"),
            )
        }
        if (shouldShowAllLevels) {
            Spacer(modifier = Modifier.width(IconSpacing))
            Surface(
                onClick = onCollapseClick,
                shape = RoundedCornerShape(50),
                color = MiuixTheme.colorScheme.secondaryContainer,
                modifier = Modifier.testTag("btn_path_level_collapse"),
            ) {
                Text(
                    text = stringResource(R.string.collapse),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun PathLevelList(
    entries: List<MediaPickerPathEntry>,
    shouldShowAllLevels: Boolean,
    onExpandAllClick: () -> Unit,
    onPathSelected: (String?) -> Unit,
) {
    if (shouldShowAllLevels) {
        FlatLevelList(entries = entries, onPathSelected = onPathSelected)
    } else {
        IndentedLevelList(
            entries = entries,
            onExpandAllClick = onExpandAllClick,
            onPathSelected = onPathSelected,
        )
    }
}

@Composable
private fun IndentedLevelList(
    entries: List<MediaPickerPathEntry>,
    onExpandAllClick: () -> Unit,
    onPathSelected: (String?) -> Unit,
) {
    val nodes = remember(entries) { entries.toLevelNodes() }
    val connectorColor = MiuixTheme.colorScheme.outline
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PanelPadding)
            .drawBehind { drawLevelConnectors(nodes = nodes, color = connectorColor) },
    ) {
        nodes.forEachIndexed { index, node ->
            when (node) {
                is PathLevelNode.Entry -> PathLevelRow(
                    entry = node.entry,
                    isCurrent = index == nodes.lastIndex,
                    rowHeight = RowHeight,
                    onClick = { onPathSelected(node.entry.path) },
                    modifier = Modifier.padding(start = IndentStep * node.indentLevel),
                )
                is PathLevelNode.Collapsed -> CollapsedLevelsRow(
                    hiddenCount = node.hiddenCount,
                    indentLevel = node.indentLevel,
                    onClick = onExpandAllClick,
                )
            }
        }
    }
}

@Composable
private fun FlatLevelList(
    entries: List<MediaPickerPathEntry>,
    onPathSelected: (String?) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        listState.scrollToItem(COLLAPSED_HEAD_LEVELS)
    }
    LazyColumn(
        state = listState,
        modifier = Modifier
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * EXPANDED_HEIGHT_RATIO)
            .padding(horizontal = PanelPadding),
    ) {
        itemsIndexed(entries, key = { _, entry -> entry.depth }) { index, entry ->
            val isCurrent = index == entries.lastIndex
            PathLevelRow(
                entry = entry,
                isCurrent = isCurrent,
                rowHeight = FlatRowHeight,
                onClick = { onPathSelected(entry.path) },
                leadingContent = {
                    Text(
                        text = if (entry.depth == 0) "" else entry.depth.toString(),
                        modifier = Modifier.width(LevelIndexWidth),
                        color = if (isCurrent) {
                            MiuixTheme.colorScheme.primary
                        } else {
                            MiuixTheme.colorScheme.onSurfaceVariantSummary
                        },
                        style = MiuixTheme.textStyles.footnote1,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(IconSpacing))
                },
            )
        }
    }
}

@Composable
private fun PathLevelRow(
    entry: MediaPickerPathEntry,
    isCurrent: Boolean,
    rowHeight: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable () -> Unit = {},
) {
    val accentColor = MiuixTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        shape = RowShape,
        color = if (isCurrent) accentColor.copy(alpha = CURRENT_ROW_ALPHA) else Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .testTag("path_level_${entry.depth}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
                .padding(horizontal = RowPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent()
            Icon(
                imageVector = if (entry.path == null) AppIcons.HomeLine else AppIcons.Folder,
                contentDescription = null,
                tint = if (isCurrent) accentColor else MiuixTheme.colorScheme.onSurfaceVariantActions,
                modifier = Modifier.size(IconSize),
            )
            Spacer(modifier = Modifier.width(IconSpacing))
            Text(
                text = entry.label,
                modifier = Modifier.weight(1f),
                color = if (isCurrent) accentColor else MiuixTheme.colorScheme.onSurface,
                style = MiuixTheme.textStyles.body1,
                fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (isCurrent) {
                Icon(
                    imageVector = AppIcons.Check,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(CheckIconSize),
                )
            }
        }
    }
}

@Composable
private fun CollapsedLevelsRow(
    hiddenCount: Int,
    indentLevel: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RowShape,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = IndentStep * indentLevel)
            .testTag("path_level_expand_all"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RowHeight)
                .padding(horizontal = RowPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = AppIcons.MoreVert,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                modifier = Modifier
                    .size(IconSize)
                    .rotate(90f),
            )
            Spacer(modifier = Modifier.width(IconSpacing))
            Text(
                text = stringResource(R.string.path_panel_more_levels, hiddenCount),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun DrawScope.drawLevelConnectors(
    nodes: List<PathLevelNode>,
    color: Color,
) {
    val rowHeight = RowHeight.toPx()
    val indentStep = IndentStep.toPx()
    val iconRadius = IconSize.toPx() / 2
    val iconCenterOffset = RowPadding.toPx() + iconRadius
    val corner = ConnectorCorner.toPx()
    val gap = ConnectorGap.toPx()
    val stroke = Stroke(width = ConnectorWidth.toPx(), cap = StrokeCap.Round)

    nodes.forEachIndexed { index, node ->
        if (index == 0) return@forEachIndexed
        val startX = nodes[index - 1].indentLevel * indentStep + iconCenterOffset
        val startY = (index - 1) * rowHeight + rowHeight / 2 + iconRadius + gap
        val centerY = index * rowHeight + rowHeight / 2
        val endX = node.indentLevel * indentStep + iconCenterOffset - iconRadius - gap
        val connector = Path().apply {
            moveTo(startX, startY)
            lineTo(startX, centerY - corner)
            quadraticTo(startX, centerY, startX + corner, centerY)
            lineTo(endX, centerY)
        }
        drawPath(path = connector, color = color, style = stroke)
    }
}

private sealed interface PathLevelNode {
    val indentLevel: Int

    data class Entry(
        val entry: MediaPickerPathEntry,
        override val indentLevel: Int,
    ) : PathLevelNode

    data class Collapsed(
        val hiddenCount: Int,
        override val indentLevel: Int,
    ) : PathLevelNode
}

private fun List<MediaPickerPathEntry>.toLevelNodes(): List<PathLevelNode> {
    if (size <= MAX_VISIBLE_LEVELS) {
        return map { entry -> PathLevelNode.Entry(entry = entry, indentLevel = entry.depth) }
    }
    val tailCount = MAX_VISIBLE_LEVELS - COLLAPSED_HEAD_LEVELS - 1
    val headEntries = take(COLLAPSED_HEAD_LEVELS)
    val tailEntries = takeLast(tailCount)
    val hiddenCount = size - COLLAPSED_HEAD_LEVELS - tailCount
    return buildList {
        headEntries.forEachIndexed { index, entry ->
            add(PathLevelNode.Entry(entry = entry, indentLevel = index))
        }
        add(
            PathLevelNode.Collapsed(
                hiddenCount = hiddenCount,
                indentLevel = COLLAPSED_HEAD_LEVELS,
            ),
        )
        tailEntries.forEachIndexed { index, entry ->
            add(PathLevelNode.Entry(entry = entry, indentLevel = COLLAPSED_HEAD_LEVELS + 1 + index))
        }
    }
}

private fun normalizePath(path: String): String = path
    .canonicalPathOrSelf()
    .trimEnd('/')
    .ifBlank { "/" }

private val PanelShape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
private val RowShape = RoundedCornerShape(12.dp)
private val PanelBottomMargin = 8.dp
private val PanelPadding = 14.dp

private val PanelShadow = Shadow(radius = 10.dp, color = Color.Black, alpha = 0.1f)
private val RowHeight = 46.dp
private val FlatRowHeight = 40.dp
private val RowPadding = 10.dp
private val IndentStep = 26.dp
private val IconSize = 20.dp
private val IconSpacing = 10.dp
private val CheckIconSize = 18.dp
private val LevelIndexWidth = 20.dp
private val ConnectorWidth = 1.5.dp
private val ConnectorCorner = 7.dp
private val ConnectorGap = 3.dp
private const val CURRENT_ROW_ALPHA = 0.14f
private const val EXPANDED_HEIGHT_RATIO = 0.6f
private const val MAX_VISIBLE_LEVELS = 7
private const val COLLAPSED_HEAD_LEVELS = 2
private const val SCRIM_DURATION_MILLIS = 160
private const val PANEL_ENTER_MILLIS = 220
private const val PANEL_EXIT_MILLIS = 160
