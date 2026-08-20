package one.only.player.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import one.only.player.core.ui.R as UiR
import one.only.player.core.ui.designsystem.AppIcons
import one.only.player.core.ui.extensions.LocalRootBottomBarPadding
import one.only.player.ui.component.FloatingBottomBar
import one.only.player.ui.component.FloatingBottomBarItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 根 Tab 定义，每项对应一个顶级导航目的地
enum class RootDestination(
    val labelRes: Int,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val tag: String,
) {
    HOME(UiR.string.tab_home, AppIcons.HomeLine, AppIcons.HomeFilled, "root_tab_home"),
    CLOUD(UiR.string.tab_cloud, AppIcons.CloudLine, AppIcons.CloudFilled, "root_tab_cloud"),
    FAVORITES(UiR.string.tab_favorites, AppIcons.FavoritesLine, AppIcons.FavoritesFilled, "root_tab_favorites"),
    SETTINGS(UiR.string.tab_settings, AppIcons.SettingsLine, AppIcons.SettingsFilled, "root_tab_settings"),
}

@Serializable
data object RootPagerRoute

@Composable
fun RootScaffold(
    rootNavigationState: RootNavigationState,
    modifier: Modifier = Modifier,
    shouldUseFloatingNavigationBar: Boolean = false,
    shouldBlurFloatingNavigationBar: Boolean = true,
    shouldShowBottomBar: Boolean = true,
    content: @Composable (RootDestination) -> Unit,
) {
    val currentPage = rootNavigationState.pagerState.currentPage
    LaunchedEffect(currentPage) {
        rootNavigationState.syncPage()
    }
    if (!shouldShowBottomBar) {
        Box(modifier = modifier.fillMaxSize()) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = rootNavigationState.pagerState,
                // 首帧只组合首页，优先显示已缓存的媒体内容。
                beyondViewportPageCount = 0,
                key = { page -> RootDestination.entries[page] },
            ) { page ->
                content(RootDestination.entries[page])
            }
        }
        return
    }

    val bottomBarPadding = rememberRootBottomBarPadding(shouldUseFloatingNavigationBar)
    val floatingBlurBackdrop = rememberRootBlurBackdrop(
        shouldBlurNavigationBar = shouldBlurFloatingNavigationBar,
    )

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier
                .fillMaxSize()
                .then(if (floatingBlurBackdrop != null) Modifier.layerBackdrop(floatingBlurBackdrop) else Modifier),
            state = rootNavigationState.pagerState,
            // 首帧只组合首页，优先显示已缓存的媒体内容。
            beyondViewportPageCount = 0,
            key = { page -> RootDestination.entries[page] },
        ) { page ->
            CompositionLocalProvider(LocalRootBottomBarPadding provides bottomBarPadding) {
                content(RootDestination.entries[page])
            }
        }
        RootBottomBar(
            currentRoot = rootNavigationState.selectedDestination,
            shouldUseFloatingNavigationBar = shouldUseFloatingNavigationBar,
            floatingBlurBackdrop = floatingBlurBackdrop,
            onTabSelected = rootNavigationState::animateTo,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun rememberRootBottomBarPadding(
    shouldUseFloatingNavigationBar: Boolean,
): PaddingValues {
    // 内容区底部预留：系统导航栏 + 底栏高度，避免导航栏遮挡内容。
    val navigationBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val navigationBarHeight = if (shouldUseFloatingNavigationBar) FLOATING_NAV_BAR_RESERVED_HEIGHT else NAV_BAR_CONTENT_HEIGHT
    return remember(navigationBarsBottom, navigationBarHeight) {
        PaddingValues(bottom = navigationBarsBottom + navigationBarHeight)
    }
}

@Composable
fun rememberRootBlurBackdrop(
    shouldBlurNavigationBar: Boolean,
): LayerBackdrop? {
    if (!shouldBlurNavigationBar || !isRuntimeShaderSupported()) return null

    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

@Composable
fun RootBottomBar(
    currentRoot: RootDestination,
    shouldUseFloatingNavigationBar: Boolean,
    floatingBlurBackdrop: Backdrop?,
    modifier: Modifier = Modifier,
    onTabSelected: (RootDestination) -> Unit,
) {
    if (shouldUseFloatingNavigationBar) {
        FloatingRootBottomBar(
            currentRoot = currentRoot,
            blurBackdrop = floatingBlurBackdrop,
            modifier = modifier,
            onTabSelected = onTabSelected,
        )
        return
    }

    val isBlurEnabled = floatingBlurBackdrop != null
    val barColor = if (isBlurEnabled) Color.Transparent else MiuixTheme.colorScheme.surface
    NavigationBar(
        modifier = modifier.then(
            if (isBlurEnabled) {
                Modifier.textureBlur(
                    backdrop = floatingBlurBackdrop,
                    shape = RectangleShape,
                    blurRadius = 25f,
                    colors = BlurDefaults.blurColors(
                        blendColors = listOf(
                            BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(0.8f)),
                        ),
                    ),
                )
            } else {
                Modifier
            },
        ),
        color = barColor,
    ) {
        RootDestination.entries.forEach { target ->
            RootNavigationBarItem(
                destination = target,
                isSelected = currentRoot == target,
                onClick = { onTabSelected(target) },
            )
        }
    }
}

@Composable
private fun FloatingRootBottomBar(
    currentRoot: RootDestination,
    blurBackdrop: Backdrop?,
    modifier: Modifier = Modifier,
    onTabSelected: (RootDestination) -> Unit,
) {
    val navigationBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // isBlurEnabled 为 false 时 backdrop 不被采样，兜底一个空 backdrop 即可
    val fallbackBackdrop = rememberLayerBackdrop { drawContent() }
    val backdrop = blurBackdrop ?: fallbackBackdrop
    val selectedIndex = currentRoot.ordinal

    FloatingBottomBar(
        modifier = modifier.padding(bottom = navigationBarsBottom + 12.dp),
        selectedIndex = { selectedIndex },
        onSelected = { index -> onTabSelected(RootDestination.entries[index]) },
        backdrop = backdrop,
        tabsCount = RootDestination.entries.size,
        isBlurEnabled = blurBackdrop != null,
    ) {
        RootDestination.entries.forEach { target ->
            val label = stringResource(target.labelRes)
            FloatingBottomBarItem(
                onClick = { onTabSelected(target) },
                modifier = Modifier
                    .defaultMinSize(minWidth = 76.dp)
                    .testTag(target.tag),
            ) {
                // 图标恒用 onSurface，选中态由上层 tint 采样药丸表现
                Icon(
                    imageVector = if (currentRoot == target) target.selectedIcon else target.unselectedIcon,
                    contentDescription = label,
                    tint = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp),
                )
                Text(
                    text = label,
                    color = MiuixTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RowScope.RootNavigationBarItem(
    destination: RootDestination,
    isSelected: Boolean,
    itemHeight: androidx.compose.ui.unit.Dp = NAV_BAR_CONTENT_HEIGHT,
    onClick: () -> Unit,
) {
    val tint = if (isSelected) {
        MiuixTheme.colorScheme.onSurfaceContainer
    } else {
        MiuixTheme.colorScheme.onSurfaceContainer.copy(alpha = 0.45f)
    }
    val label = stringResource(destination.labelRes)

    Column(
        modifier = Modifier
            .height(itemHeight)
            .weight(1f)
            .testTag(destination.tag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(26.dp),
        )
        Text(
            text = label,
            color = tint,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val NAV_BAR_CONTENT_HEIGHT = 72.dp
private val FLOATING_NAV_BAR_RESERVED_HEIGHT = 76.dp
