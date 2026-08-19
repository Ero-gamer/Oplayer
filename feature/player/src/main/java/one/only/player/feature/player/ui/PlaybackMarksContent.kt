package one.only.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import one.only.player.core.model.PlaybackMark
import one.only.player.core.ui.R
import one.only.player.core.ui.designsystem.AppIcons
import one.only.player.feature.player.extensions.formatted
import one.only.player.feature.player.ui.panel.PanelActionButton
import one.only.player.feature.player.ui.panel.rememberPlayerPanelTokens
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PlaybackMarksContent(
    marks: List<PlaybackMark>,
    onAddMarkClick: () -> Unit,
    onMarkClick: (PlaybackMark) -> Unit,
    onDeleteMarkClick: (PlaybackMark) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = rememberPlayerPanelTokens()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PanelActionButton(
            modifier = Modifier.testTag("btn_add_playback_mark"),
            text = stringResource(R.string.add_playback_mark),
            isProminent = true,
            onClick = onAddMarkClick,
        )

        if (marks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                MiuixText(
                    text = stringResource(R.string.no_playback_marks),
                    style = MiuixTheme.textStyles.body2,
                    color = tokens.secondaryContentColor,
                    modifier = Modifier.padding(top = 48.dp),
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = marks,
                    key = PlaybackMark::id,
                ) { mark ->
                    PlaybackMarkItem(
                        mark = mark,
                        onClick = { onMarkClick(mark) },
                        onDeleteClick = { onDeleteMarkClick(mark) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaybackMarkItem(
    mark: PlaybackMark,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val tokens = rememberPlayerPanelTokens()
    val shape = RoundedCornerShape(tokens.itemCornerRadius)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("playback_mark_${mark.id}")
            .clip(shape)
            .background(tokens.itemColor)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MiuixIcon(
            imageVector = AppIcons.History,
            contentDescription = null,
            tint = tokens.itemContentColor,
        )
        Column(modifier = Modifier.weight(1f)) {
            MiuixText(
                text = mark.positionMs.milliseconds.formatted(),
                style = MiuixTheme.textStyles.body1,
                color = tokens.itemContentColor,
            )
            if (mark.durationMs > 0L) {
                MiuixText(
                    text = mark.durationMs.milliseconds.formatted(),
                    style = MiuixTheme.textStyles.footnote1,
                    color = tokens.secondaryContentColor,
                )
            }
        }
        MiuixIconButton(onClick = onDeleteClick) {
            MiuixIcon(
                imageVector = AppIcons.Delete,
                contentDescription = stringResource(R.string.delete_mark),
                tint = tokens.secondaryContentColor,
            )
        }
    }
}
