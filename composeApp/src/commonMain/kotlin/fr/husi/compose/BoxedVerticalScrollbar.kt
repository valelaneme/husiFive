package fr.husi.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    // 只保留该库支持的 thickness 参数调整厚度
    val thinStyle = style.copy(
        thickness = 3.dp // 将滚动条宽度设为 3dp（可按需改为 2.dp 或 4.dp）
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
