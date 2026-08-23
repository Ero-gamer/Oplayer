package one.only.player.feature.videopicker.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import one.only.player.core.model.ApplicationPreferences
import one.only.player.core.model.Video
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val META_SEPARATOR = " · "

// 视频附加信息的统一顺序，各页面共用避免同一视频在不同列表里顺序不一致
internal fun Video.metaParts(preferences: ApplicationPreferences): List<String> = buildList {
    if (preferences.shouldShowResolutionField && height > 0) {
        add("${height}p")
    }
    if (preferences.shouldShowSizeField) {
        add(formattedFileSize)
    }
}

// 媒体附加信息合并为一行弱化小字，层次靠字号与明度区分，不用色块
@Composable
internal fun MediaMetaText(
    parts: List<String>,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxLines: Int = 2,
) {
    if (parts.isEmpty()) return

    Text(
        text = parts.joinToString(META_SEPARATOR),
        modifier = modifier,
        style = MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.Normal),
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
    )
}

// 缩略图覆盖层徽标，胶囊底保证压在画面上仍可读
@Composable
internal fun ThumbnailBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.footnote2.copy(fontWeight = FontWeight.Medium),
        color = Color.White,
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
