// Generated from MingCute Core SVG assets. Source: https://github.com/mingcute-design/mingcute-icons
// Do not edit paths by hand.
package one.only.player.core.ui.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.unit.dp

internal val MingCuteFocus: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCute.Focus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = false,
    ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12.5 12a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0").toNodes(),
                pathFillType = PathFillType.NonZero,
                fill = SolidColor(Color.Black),
                stroke = null,
            )
            addPath(
                pathData = PathParser().parsePathString("M12 3v3m0 12v3m-9-9h3m12 0h3m-1 0a8 8 0 1 1-16 0 8 8 0 0 1 16 0m-7.5 0a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0").toNodes(),
                pathFillType = PathFillType.NonZero,
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
    }.build()
}
