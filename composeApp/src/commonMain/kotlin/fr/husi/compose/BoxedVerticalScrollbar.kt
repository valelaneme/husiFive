package fr.husi.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp // ← 必须确保有这行导入！
import io.github.oikvpqya.compose.fastscroller.ScrollbarAdapter
import io.github.oikvpqya.compose.fastscroller.ScrollbarStyle
import io.github.oikvpqya.compose.fastscroller.VerticalScrollbar

/** Use it with Row */
@Composable
fun BoxedVerticalScrollbar(
    adapter: ScrollbarAdapter,
    style: ScrollbarStyle,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enablePressToScroll: Boolean = true,
    indicator: @Composable (position: Float, isVisible: Boolean) -> Unit = { _, _ -> },
) {
    // 调整这里的厚度数值把滚动条变细
    val thinStyle = style.copy(
        thickness = 3.dp,          // 常态宽度（可根据需要改成 2.dp 或 3.dp）
        unhoverThickness = 2.dp,   // 未悬停时的宽度
        hoverThickness = 4.dp      // 悬停/触摸时的宽度
    )

    Box {
        VerticalScrollbar(
            adapter = adapter,
            style = thinStyle,
            modifier = modifier,
            reverseLayout = reverseLayout,
            interactionSource = interactionSource,
            enablePressToScroll = enablePressToScroll,
            indicator = indicator,
        )
    }
}
