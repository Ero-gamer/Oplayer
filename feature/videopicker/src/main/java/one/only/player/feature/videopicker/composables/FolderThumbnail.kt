package one.only.player.feature.videopicker.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import one.only.player.core.ui.designsystem.AppIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 圆角底板为正方形，列表布局左右留出间距避免紧贴边缘
private val PlateSize = 75.dp
private val PlateHorizontalPadding = 15.dp
private val PlateShape = RoundedCornerShape(percent = 30)
private const val GLYPH_RATIO = 0.7f
private const val PLATE_ALPHA = 0.14f

// 列表布局的文件夹缩略图
@Composable
internal fun FolderThumbnail(
    modifier: Modifier = Modifier,
) {
    FolderPlate(
        modifier = modifier
            .padding(horizontal = PlateHorizontalPadding)
            .size(PlateSize),
    )
}

// 网格布局的文件夹缩略图，列宽不足时等比缩小，保证底板完整显示
@Composable
internal fun FolderGridThumbnail(
    modifier: Modifier = Modifier,
) {
    FolderPlate(
        modifier = modifier
            .widthIn(max = PlateSize)
            .fillMaxWidth()
            .aspectRatio(1f),
    )
}

@Composable
private fun FolderPlate(
    modifier: Modifier,
) {
    val primary = MiuixTheme.colorScheme.primary

    Box(
        modifier = modifier
            .clip(PlateShape)
            .background(primary.copy(alpha = PLATE_ALPHA)),
    ) {
        Icon(
            imageVector = AppIcons.FolderFill,
            contentDescription = null,
            tint = primary,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(GLYPH_RATIO),
        )
    }
}
