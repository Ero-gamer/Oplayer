// Generated from MingCute Core SVG assets. Source: https://github.com/mingcute-design/mingcute-icons
// Do not edit paths by hand.
package one.only.player.core.ui.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

internal val MingCutePip: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCute.Pip",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = false,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M11 18H4a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h15a1 1 0 0 1 1 1v4M6.38 8.38 9.5 11.5m-2.498.502h3v-3M15 19h5a1 1 0 0 0 1-1v-4a1 1 0 0 0-1-1h-5a1 1 0 0 0-1 1v4a1 1 0 0 0 1 1").toNodes(),
            pathFillType = PathFillType.NonZero,
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }.build()
}
