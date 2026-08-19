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

internal val MingCuteHomeLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCute.HomeLine",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = false,
    ).apply {
            addPath(
                pathData = PathParser().parsePathString("M4 9.5a1 1 0 0 1 .4-.8l7-5.25a1 1 0 0 1 1.2 0l7 5.25a1 1 0 0 1 .4.8V19a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1z").toNodes(),
                pathFillType = PathFillType.NonZero,
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
            )
    }.build()
}
