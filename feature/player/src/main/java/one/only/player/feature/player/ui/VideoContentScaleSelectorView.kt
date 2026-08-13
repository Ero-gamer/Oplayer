package one.only.player.feature.player.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import one.only.player.core.model.VideoContentScale
import one.only.player.core.ui.R
import one.only.player.feature.player.extensions.nameRes
import one.only.player.feature.player.ui.panel.PanelActionButton
import one.only.player.feature.player.ui.panel.PanelOptionList
import one.only.player.feature.player.ui.panel.PanelOptionRow

@Composable
fun BoxScope.VideoContentScaleSelectorView(
    modifier: Modifier = Modifier,
    shouldShow: Boolean,
    videoContentScale: VideoContentScale,
    isCustomZoomActive: Boolean = false,
    onVideoContentScaleChanged: (VideoContentScale) -> Unit,
    onShowVideoFilters: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    OverlayView(
        modifier = modifier,
        shouldShow = shouldShow,
        title = stringResource(R.string.video_zoom),
    ) {
        VideoContentScaleSelectorContent(
            videoContentScale = videoContentScale,
            isCustomZoomActive = isCustomZoomActive,
            onVideoContentScaleChanged = onVideoContentScaleChanged,
            onShowVideoFilters = onShowVideoFilters,
            onDismiss = onDismiss,
        )
    }
}

@Composable
fun VideoContentScaleSelectorContent(
    videoContentScale: VideoContentScale,
    isCustomZoomActive: Boolean = false,
    onVideoContentScaleChanged: (VideoContentScale) -> Unit,
    onShowVideoFilters: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .padding(horizontal = 16.dp),
    ) {
        if (onShowVideoFilters != null) {
            PanelActionButton(
                modifier = Modifier.testTag("btn_open_video_filters"),
                text = stringResource(R.string.video_filters),
                onClick = onShowVideoFilters,
            )
            Spacer(modifier = Modifier.size(16.dp))
        }

        PanelOptionList(modifier = Modifier.selectableGroup()) {
            VideoContentScale.entries.forEachIndexed { index, contentScale ->
                PanelOptionRow(
                    isSelected = !isCustomZoomActive && contentScale == videoContentScale,
                    text = stringResource(contentScale.nameRes()),
                    testTag = "btn_video_scale_${contentScale.name.lowercase()}",
                    onClick = {
                        onVideoContentScaleChanged(contentScale)
                        onDismiss()
                    },
                )
            }
        }
    }
}
