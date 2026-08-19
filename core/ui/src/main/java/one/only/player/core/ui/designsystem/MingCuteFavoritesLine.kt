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

internal val MingCuteFavoritesLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCute.FavoritesLine",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = false,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M12 5.705c-3.692-3.947-9.114-.478-8.998 4.666q.102 4.59 7.19 8.8c.274.163.706.41 1.08.62a1.48 1.48 0 0 0 1.457 0c.373-.21.805-.457 1.08-.62q7.085-4.21 7.19-8.8c.115-5.144-5.307-8.613-8.999-4.666Z").toNodes(),
            pathFillType = PathFillType.NonZero,
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Round,
        )
    }.build()
}
