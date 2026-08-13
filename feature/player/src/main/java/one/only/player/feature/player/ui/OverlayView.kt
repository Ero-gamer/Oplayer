package one.only.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import one.only.player.core.ui.extensions.withBottomFallback
import one.only.player.core.ui.theme.OnlyPlayerTheme
import one.only.player.feature.player.ui.panel.rememberPanelMaterialColorScheme
import one.only.player.feature.player.ui.panel.rememberPanelMiuixColors
import one.only.player.feature.player.ui.panel.rememberPlayerPanelTokens
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BoxScope.OverlayView(
    modifier: Modifier = Modifier,
    shouldShow: Boolean,
    title: String,
    testTag: String? = null,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val configuration = LocalConfiguration.current
    val resolvedContentPadding = contentPadding.withBottomFallback()
    val layoutDirection = LocalLayoutDirection.current
    val tokens = rememberPlayerPanelTokens()
    val safeDrawingPadding = WindowInsets.safeDrawing.asPaddingValues()
    val panelMargin = 12.dp
    val screenWidth = configuration.screenWidthDp.dp
    val panelShape = RoundedCornerShape(tokens.containerCornerRadius)

    val sizeModifier = if (configuration.isPortrait) {
        Modifier
            .padding(
                start = panelMargin,
                end = panelMargin,
                bottom = maxOf(safeDrawingPadding.calculateBottomPadding(), panelMargin),
            )
            .width(min(screenWidth - panelMargin * 2, 560.dp))
            .fillMaxHeight(0.45f)
    } else {
        Modifier
            .padding(
                top = panelMargin,
                bottom = panelMargin,
                end = maxOf(safeDrawingPadding.calculateEndPadding(layoutDirection), panelMargin),
            )
            .width(min(screenWidth * 0.45f, 400.dp))
            .fillMaxHeight()
    }

    AnimatedVisibility(
        modifier = Modifier.align(
            if (configuration.isPortrait) {
                Alignment.BottomCenter
            } else {
                Alignment.CenterEnd
            },
        ),
        visible = shouldShow,
        enter = if (configuration.isPortrait) slideInVertically { it } else slideInHorizontally { it },
        exit = if (configuration.isPortrait) slideOutVertically { it } else slideOutHorizontally { it },
    ) {
        Column(
            modifier = modifier
                .then(
                    if (testTag != null) {
                        Modifier
                            .testTag(testTag)
                            .semantics { contentDescription = testTag }
                    } else {
                        Modifier
                    },
                )
                .then(sizeModifier)
                .clip(panelShape)
                .background(tokens.containerColor)
                .border(1.dp, tokens.containerBorderColor, panelShape)
                .padding(resolvedContentPadding)
                .padding(top = 20.dp),
        ) {
            MiuixTheme(colors = tokens.rememberPanelMiuixColors()) {
                MaterialTheme(colorScheme = tokens.rememberPanelMaterialColorScheme()) {
                    MiuixText(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        text = title,
                        color = tokens.contentColor,
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    content()
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewOverlayView() {
    OnlyPlayerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            OverlayView(modifier = Modifier.align(Alignment.BottomCenter), title = "Selector view", shouldShow = true) {
                MiuixText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Lorem ipsum")
            }
        }
    }
}
