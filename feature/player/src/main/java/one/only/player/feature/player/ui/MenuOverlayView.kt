package one.only.player.feature.player.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import one.only.player.core.ui.R
import one.only.player.core.ui.designsystem.NextIcons
import one.only.player.feature.player.ui.panel.FloatingPlayerPanel
import one.only.player.feature.player.ui.panel.FloatingPlayerPanelState
import one.only.player.feature.player.ui.panel.rememberFloatingPlayerPanelState
import one.only.player.feature.player.ui.panel.rememberPlayerPanelTokens
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton

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
    data object VideoInfo : MenuRoute
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
    onDismiss: () -> Unit = {},
    panelState: FloatingPlayerPanelState = rememberFloatingPlayerPanelState(),
    content: @Composable (MenuRoute) -> Unit,
) {
    val tokens = rememberPlayerPanelTokens()
    FloatingPlayerPanel(
        shouldShow = externalRoute != null,
        title = title,
        panelState = panelState,
        testTag = "panel_player_menu",
        onDismiss = onDismiss,
        navigationIcon = if (canGoBack) {
            {
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
            }
        } else {
            null
        },
    ) {
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
