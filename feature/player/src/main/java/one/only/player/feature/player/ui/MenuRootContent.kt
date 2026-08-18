package one.only.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import one.only.player.core.model.PlayerControl
import one.only.player.feature.player.ui.panel.rememberPlayerPanelTokens
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MenuRootContent(
    menuControls: List<PlayerControl>,
    bindings: Map<PlayerControl, PlayerControlBinding>,
    onNavigate: (MenuRoute) -> Unit,
    onDismiss: () -> Unit,
) {
    val tiles = bindings.resolve(menuControls)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 92.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = tiles,
            key = PlayerControlBinding::menuTestTag,
        ) { binding ->
            MenuTileButton(
                binding = binding,
                onClick = {
                    when (val action = binding.action) {
                        is PlayerControlAction.OpenPanel -> onNavigate(action.route)
                        is PlayerControlAction.Execute -> {
                            action.onExecute()
                            onDismiss()
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun MenuTileButton(
    binding: PlayerControlBinding,
    onClick: () -> Unit,
) {
    val tokens = rememberPlayerPanelTokens()
    val shape = RoundedCornerShape(tokens.tileCornerRadius)
    val contentAlpha = if (binding.isEnabled) 1f else 0.4f
    Column(
        modifier = Modifier
            .testTag(binding.menuTestTag)
            .semantics { contentDescription = binding.menuTestTag }
            .fillMaxWidth()
            .height(84.dp)
            .clip(shape)
            .background(tokens.itemColor)
            .clickable(
                enabled = binding.isEnabled,
                onClick = onClick,
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MiuixIcon(
            imageVector = binding.icon,
            contentDescription = null,
            tint = tokens.itemContentColor.copy(alpha = contentAlpha),
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.size(6.dp))
        MiuixText(
            text = binding.label,
            color = tokens.itemContentColor.copy(alpha = contentAlpha),
            style = MiuixTheme.textStyles.footnote1,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
