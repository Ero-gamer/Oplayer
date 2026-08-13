package one.only.player.feature.player.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import one.only.player.core.ui.R
import one.only.player.core.ui.designsystem.NextIcons
import one.only.player.feature.player.ui.panel.rememberPanelMaterialColorScheme
import one.only.player.feature.player.ui.panel.rememberPanelMiuixColors
import one.only.player.feature.player.ui.panel.rememberPlayerPanelTokens
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

sealed interface MenuRoute {
    data object Root : MenuRoute
    data object ControlLock : MenuRoute
    data object Mute : MenuRoute
    data object AmbienceMode : MenuRoute
    data object MirrorVideo : MenuRoute
    data object SleepTimer : MenuRoute
    data object Decoder : MenuRoute
    data object LoopMode : MenuRoute
    data object ShuffleMode : MenuRoute
    data object PlaybackSpeed : MenuRoute
    data object Audio : MenuRoute
    data object Subtitle : MenuRoute
    data object Playlist : MenuRoute
    data object VideoContentScale : MenuRoute
    data object VideoFilters : MenuRoute
    data object PlaybackMarks : MenuRoute
    data object Chapters : MenuRoute
}

@Composable
fun BoxScope.MenuOverlayView(
    externalRoute: MenuRoute?,
    title: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
    content: @Composable (MenuRoute) -> Unit,
) {
    val configuration = LocalConfiguration.current
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
            if (configuration.isPortrait) Alignment.BottomCenter else Alignment.CenterEnd,
        ),
        visible = externalRoute != null,
        enter = if (configuration.isPortrait) slideInVertically { it } else slideInHorizontally { it },
        exit = if (configuration.isPortrait) slideOutVertically { it } else slideOutHorizontally { it },
    ) {
        Column(
            modifier = Modifier
                .testTag("panel_player_menu")
                .then(sizeModifier)
                .clip(panelShape)
                .background(tokens.containerColor)
                .border(1.dp, tokens.containerBorderColor, panelShape)
                .padding(top = 14.dp, bottom = 14.dp),
        ) {
            MiuixTheme(colors = tokens.rememberPanelMiuixColors()) {
                MaterialTheme(colorScheme = tokens.rememberPanelMaterialColorScheme()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (canGoBack) {
                            MiuixIconButton(
                                modifier = Modifier.testTag("btn_menu_back"),
                                onClick = onBack,
                            ) {
                                MiuixIcon(
                                    imageVector = NextIcons.ArrowBack,
                                    contentDescription = stringResource(R.string.navigate_up),
                                    tint = tokens.contentColor,
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        MiuixText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            text = title,
                            color = tokens.contentColor,
                            style = MiuixTheme.textStyles.title3,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    AnimatedContent(
                        targetState = externalRoute ?: MenuRoute.Root,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "menu_route",
                        modifier = Modifier.fillMaxSize(),
                    ) { route ->
                        content(route)
                    }
                }
            }
        }
    }
}
