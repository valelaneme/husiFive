package fr.husi.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp // 确保引入了 dp
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
    // 复制并缩减原 style 的厚度，让所有用到该组件的滚动条都变细
    val thinStyle = style.copy(
        thickness = 3.dp,          // 你想要的常态宽度（例如 3dp 或 4dp）
        unhoverThickness = 2.dp,   // 未悬停时的宽度
        hoverThickness = 4.dp      // 悬停/触摸时的宽度
    )

    Box {
        VerticalScrollbar(
            adapter = adapter,
            style = thinStyle, // 使用修改后的细滚动条样式
            modifier = modifier,
            reverseLayout = reverseLayout,
            interactionSource = interactionSource,
            enablePressToScroll = enablePressToScroll,
            indicator = indicator,
        )
    }
}
