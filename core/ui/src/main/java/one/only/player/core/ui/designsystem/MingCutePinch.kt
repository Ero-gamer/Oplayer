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

internal val MingCutePinch: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCute.Pinch",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = false,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M11 11V5.5a1.5 1.5 0 0 0-3 0V13c-2-2.5-3-3-5-2l3.065 6.13A7 7 0 0 0 12.326 21H13a7 7 0 0 0 7-7v-1m-9-1V3.5a1.5 1.5 0 0 1 3 0V11v-.5a1.5 1.5 0 0 1 3 0V12v-.5a1.5 1.5 0 0 1 3 0v1.833").toNodes(),
            pathFillType = PathFillType.NonZero,
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }.build()
}
