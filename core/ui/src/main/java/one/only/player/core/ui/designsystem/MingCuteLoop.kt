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

internal val MingCuteLoop: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCute.Loop",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = false,
    ).apply {
            addPath(
                pathData = PathParser().parsePathString("M4 14V8a2 2 0 0 1 2-2h12m2 4v6a2 2 0 0 1-2 2H6").toNodes(),
                pathFillType = PathFillType.NonZero,
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M16.578 3.654c-.28-.146-.604.065-.631.411a25 25 0 0 0-.073 1.93c0 .848.04 1.538.078 1.996.027.324.323.503.598.36a19 19 0 0 0 1.55-.91 20 20 0 0 0 1.476-1.056c.248-.196.259-.574.023-.762a19 19 0 0 0-1.498-1.074 19 19 0 0 0-1.523-.895m-9.162 12c.28-.146.604.065.632.411a25.4 25.4 0 0 1-.006 3.926c-.026.324-.323.503-.598.36a19 19 0 0 1-1.55-.91 20 20 0 0 1-1.476-1.056c-.248-.196-.258-.574-.023-.762a20 20 0 0 1 1.499-1.074 19 19 0 0 1 1.522-.895").toNodes(),
                pathFillType = PathFillType.NonZero,
                fill = SolidColor(Color.Black),
                stroke = null,
            )
    }.build()
}
