package one.only.player.feature.videopicker.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Stable
data class SelectionBarAction(
    val label: String,
    val icon: ImageVector,
    val testTag: String,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit,
)

// 选中模式底部浮动操作栏，只承载高频操作；低频操作由顶栏的溢出菜单承载。
@Composable
fun SelectionBottomBar(
    isVisible: Boolean,
    actions: List<SelectionBarAction>,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isVisible && actions.isNotEmpty(),
        enter = slideInVertically { fullHeight -> fullHeight } + fadeIn(),
        exit = slideOutVertically { fullHeight -> fullHeight } + fadeOut(),
        modifier = modifier,
    ) {
        val barShape = RoundedCornerShape(26.dp)
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(top = 8.dp),
        ) {
            Surface(
                shape = barShape,
                color = MiuixTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = barShape,
                        ambientColor = Color.Black.copy(alpha = 0.5f),
                        spotColor = Color.Black.copy(alpha = 0.5f),
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actions.forEach { action ->
                        SelectionBarButton(
                            action = action,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionBarButton(
    action: SelectionBarAction,
    modifier: Modifier = Modifier,
) {
    val iconTint = if (action.isDestructive) {
        MiuixTheme.colorScheme.onErrorContainer
    } else {
        MiuixTheme.colorScheme.onSurface
    }
    val labelColor = if (action.isDestructive) {
        MiuixTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
    } else {
        MiuixTheme.colorScheme.onSurfaceVariantSummary
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = action.onClick)
            .padding(vertical = 9.dp)
            .testTag(action.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = action.label,
            tint = iconTint,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = action.label,
            style = MiuixTheme.textStyles.footnote2,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
