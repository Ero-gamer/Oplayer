package one.only.player.feature.player.ui.controls

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import one.only.player.core.model.PlayerControl
import one.only.player.feature.player.ui.MenuRoute
import one.only.player.feature.player.ui.PlayerControlAction
import one.only.player.feature.player.ui.PlayerControlBinding
import one.only.player.feature.player.ui.resolve
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton

// 顶栏和底栏共用：按编排顺序摆出角落按钮
@Composable
internal fun PlayerCornerControls(
    controls: List<PlayerControl>,
    bindings: Map<PlayerControl, PlayerControlBinding>,
    onOpenPanel: (MenuRoute) -> Unit,
) {
    bindings.resolve(controls).forEach { binding ->
        PlayerCornerControlButton(
            binding = binding,
            onClick = {
                when (val action = binding.action) {
                    is PlayerControlAction.OpenPanel -> onOpenPanel(action.route)
                    is PlayerControlAction.Execute -> action.onExecute()
                }
            },
        )
    }
}

@Composable
internal fun PlayerCornerControlButton(
    binding: PlayerControlBinding,
    onClick: () -> Unit,
) {
    MiuixIconButton(
        modifier = Modifier.testTag(binding.cornerTestTag),
        onClick = onClick,
        enabled = binding.isEnabled,
    ) {
        MiuixIcon(
            modifier = Modifier.size(24.dp),
            imageVector = binding.icon,
            contentDescription = binding.label,
            tint = if (binding.isEnabled) Color.White else Color.White.copy(alpha = 0.4f),
        )
    }
}
