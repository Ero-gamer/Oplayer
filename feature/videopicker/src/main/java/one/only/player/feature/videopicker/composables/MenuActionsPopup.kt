package one.only.player.feature.videopicker.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.window.WindowListPopup

@Immutable
data class MenuAction(
    val text: String,
    val icon: ImageVector,
    val testTag: String,
    val onClick: () -> Unit,
)

// 顶栏图标按钮的下拉菜单：分组之间自动插入分隔线，弹窗与屏幕边缘留出边距。
@Composable
fun MenuActionsPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    groups: List<List<MenuAction>>,
) {
    val visibleGroups = groups.filter(List<MenuAction>::isNotEmpty)
    val actionCount = visibleGroups.sumOf(List<MenuAction>::size)
    WindowListPopup(
        show = expanded && actionCount > 0,
        popupPositionProvider = MenuPositionProvider,
        alignment = PopupPositionProvider.Align.End,
        onDismissRequest = onDismissRequest,
    ) {
        val dismiss = LocalDismissState.current
        ListPopupColumn {
            var actionIndex = 0
            visibleGroups.forEachIndexed { groupIndex, group ->
                if (groupIndex > 0) HorizontalDivider()
                group.forEach { action ->
                    val currentIndex = actionIndex++
                    Box(modifier = Modifier.testTag(action.testTag)) {
                        DropdownImpl(
                            item = DropdownItem(
                                text = action.text,
                                icon = { modifier ->
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = null,
                                        modifier = modifier,
                                    )
                                },
                            ),
                            optionSize = actionCount,
                            isSelected = false,
                            index = currentIndex,
                            dropdownColors = DropdownDefaults.dropdownColors(),
                            isFirst = currentIndex == 0,
                            isLast = currentIndex == actionCount - 1,
                            onSelectedIndexChange = {
                                dismiss?.invoke()
                                onDismissRequest()
                                action.onClick()
                            },
                        )
                    }
                }
            }
        }
    }
}

// miuix 默认的 DropdownPositionProvider 水平边距为 0，会让菜单紧贴屏幕边缘。
private val MenuPositionProvider = ListPopupDefaults.dropdownPositionProvider(horizontalMargin = 12.dp)
