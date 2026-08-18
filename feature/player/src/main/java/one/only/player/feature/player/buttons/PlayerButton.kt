package one.only.player.feature.player.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val PlayerButtonSize = 40.dp

// 播放器覆盖层上的圆形强调按钮
@Composable
fun PlayerButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(PlayerButtonSize),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary,
        ),
        content = content,
    )
}
