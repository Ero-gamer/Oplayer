package one.only.player.settings.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import one.only.player.core.model.PlayerControl
import one.only.player.core.ui.R
import one.only.player.core.ui.designsystem.AppIcons
import one.only.player.core.ui.extensions.icon
import one.only.player.core.ui.extensions.id
import one.only.player.core.ui.extensions.label
import one.only.player.core.ui.extensions.playerCornerControlsCapacity
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val PreviewBackground = Color(0xFF161616)
private val PreviewGlyphTint = Color.White.copy(alpha = 0.78f)
private val PreviewTileBackground = Color.White.copy(alpha = 0.08f)
private const val PREVIEW_MENU_COLUMNS = 4

// 预览的三种视角，横竖屏只影响控制栏宽度
internal enum class ControlsPreviewLayout {
    PORTRAIT,
    LANDSCAPE,
    MENU,
}

@Composable
internal fun PlayerControlsPreviewCard(
    layout: ControlsPreviewLayout,
    topRightControls: List<PlayerControl>,
    bottomRightControls: List<PlayerControl>,
    menuControls: List<PlayerControl>,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = layout.previewWidth())
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(PreviewBackground)
                .testTag("preview_customize_controls")
                .semantics { contentDescription = "preview_customize_controls" }
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (layout) {
                ControlsPreviewLayout.PORTRAIT,
                ControlsPreviewLayout.LANDSCAPE,
                -> {
                    val capacity = playerCornerControlsCapacity(
                        isPortrait = layout == ControlsPreviewLayout.PORTRAIT,
                    )
                    CustomizePreviewTopBar(
                        topRightControls = topRightControls,
                        maxVisibleControls = capacity.topRight,
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (layout == ControlsPreviewLayout.LANDSCAPE) 56.dp else 88.dp),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                    )
                    CustomizePreviewBottomBar(
                        bottomRightControls = bottomRightControls,
                        maxVisibleControls = capacity.bottomRight,
                    )
                }

                ControlsPreviewLayout.MENU -> CustomizePreviewMenu(menuControls = menuControls)
            }
        }
    }
}

// 菜单面板本身铺满宽度，不受竖屏窄框限制
private fun ControlsPreviewLayout.previewWidth(): Dp = when (this) {
    ControlsPreviewLayout.PORTRAIT -> 320.dp
    ControlsPreviewLayout.LANDSCAPE, ControlsPreviewLayout.MENU -> 720.dp
}

@Composable
internal fun ControlsPreviewLayout.label(): String = when (this) {
    ControlsPreviewLayout.PORTRAIT -> stringResource(R.string.customize_controls_layout_portrait)
    ControlsPreviewLayout.LANDSCAPE -> stringResource(R.string.customize_controls_layout_landscape)
    ControlsPreviewLayout.MENU -> stringResource(R.string.customize_controls_layout_menu)
}

@Composable
private fun CustomizePreviewTopBar(
    topRightControls: List<PlayerControl>,
    maxVisibleControls: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        PreviewGlyph(imageVector = AppIcons.ArrowBack)
        MiuixText(
            text = stringResource(R.string.customize_controls_preview),
            color = PreviewGlyphTint,
            style = MiuixTheme.textStyles.footnote1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        PreviewCornerControls(
            controls = topRightControls,
            maxVisibleControls = maxVisibleControls,
        )
        PreviewGlyph(imageVector = AppIcons.Menu)
    }
}

@Composable
private fun CustomizePreviewBottomBar(
    bottomRightControls: List<PlayerControl>,
    maxVisibleControls: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        PreviewGlyph(imageVector = AppIcons.Play)
        PreviewGlyph(imageVector = AppIcons.SkipPrevious)
        PreviewGlyph(imageVector = AppIcons.SkipNext)
        Spacer(modifier = Modifier.weight(1f))
        PreviewCornerControls(
            controls = bottomRightControls,
            maxVisibleControls = maxVisibleControls,
        )
    }
}

@Composable
private fun PreviewCornerControls(
    controls: List<PlayerControl>,
    maxVisibleControls: Int,
) {
    Row(
        modifier = Modifier
            .widthIn(max = PreviewControlSize * maxVisibleControls)
            .horizontalScroll(rememberScrollState()),
    ) {
        controls.forEach { control ->
            PreviewControlButton(control = control)
        }
    }
}

// 按真实菜单的网格顺序铺开，行内不足的位置留空
@Composable
private fun CustomizePreviewMenu(menuControls: List<PlayerControl>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        menuControls.chunked(PREVIEW_MENU_COLUMNS).forEach { rowControls ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                rowControls.forEach { control ->
                    PreviewMenuTile(
                        control = control,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(PREVIEW_MENU_COLUMNS - rowControls.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PreviewMenuTile(
    control: PlayerControl,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PreviewTileBackground)
            .testTag("preview_menu_${control.id}")
            .semantics { contentDescription = "preview_menu_${control.id}" }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MiuixIcon(
            imageVector = control.icon(),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.size(4.dp))
        MiuixText(
            text = control.label(),
            color = PreviewGlyphTint,
            style = MiuixTheme.textStyles.footnote2,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PreviewControlButton(control: PlayerControl) {
    Box(
        modifier = Modifier
            .size(PreviewControlSize)
            .testTag("preview_control_${control.id}")
            .semantics { contentDescription = "preview_control_${control.id}" },
        contentAlignment = Alignment.Center,
    ) {
        MiuixIcon(
            imageVector = control.icon(),
            contentDescription = control.label(),
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
    }
}

private val PreviewControlSize = 32.dp

@Composable
private fun PreviewGlyph(imageVector: ImageVector) {
    MiuixIcon(
        imageVector = imageVector,
        contentDescription = null,
        tint = PreviewGlyphTint,
        modifier = Modifier
            .padding(4.dp)
            .size(18.dp),
    )
}
