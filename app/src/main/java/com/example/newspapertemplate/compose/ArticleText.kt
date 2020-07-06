package com.example.newspapertemplate.compose

import android.graphics.Point
import android.graphics.Rect
import android.util.Size
import androidx.compose.Composable
import androidx.compose.remember
import androidx.compose.state
import androidx.ui.core.Layout
import androidx.ui.core.Modifier
import androidx.ui.core.drawBehind
import androidx.ui.graphics.drawscope.drawCanvas
import androidx.ui.text.TextStyle
import androidx.ui.unit.ipx
import com.example.newspapertemplate.text.AdvancedTextLayout
import com.example.newspapertemplate.text.LayoutContext
import com.example.newspapertemplate.text.Line

enum class ArticleTemplate {
    Figure_Left,
    Figure_Center,
    Figure_Right
}

private data class TemplateCallbacks(
    val nextPosition: LayoutContext.(lines: List<Line>, word_w: Int, word_h: Int, figRect: Size) -> Point,
    val widthNegotiation: LayoutContext.(x: Int, y: Int, w: Int, h:Int, figRect: Size) -> Int,
    val figureLocation: (width: Int, figSize: Size) -> Point
)

private val TEMPLATE_MAP = mapOf(
    ArticleTemplate.Figure_Left to TemplateCallbacks(
        nextPosition = { lines, w_width, w_height, size ->
            val ltSquare = Rect(0, 0, size.width, size.height)
            val y = if (lines.isEmpty()) 0 else lines.last().bottom
            val x = if (y < ltSquare.bottom) ltSquare.right else 0
            Point(x, y)
        },
        widthNegotiation = { x, y, w, h, size ->
            val ltSquare = Rect(0, 0, size.width, size.height)
            if (x != 0) {
                width - ltSquare.width() - w
            } else {
                width - w
            }
        },
        figureLocation = { width, size ->
            Point(0, 0)
        }
    ),
    ArticleTemplate.Figure_Center to TemplateCallbacks(
        nextPosition = lambda@ { lines, w_width, w_height, size ->
            val ctSquare = Rect(
                width / 2 - size.width / 2,
                0,
                width / 2 + size.width / 2,
                size.height
            )
            if (lines.isEmpty()) return@lambda Point(0, 0)
            val last = lines.last()
            if (last.right < ctSquare.left) {
                // previously drawn left part. Draw right part with the same top next.
                return@lambda Point(ctSquare.right, last.top)
            } else if (last.bottom < ctSquare.bottom) {
                // previously drawn right part. Draw left part next line.
                return@lambda Point(0, last.bottom)
            } else {
                // Already bottom of the square.
                return@lambda Point(0, last.bottom)
            }

        },
        widthNegotiation = { x, y, w, h, size ->
            val ctSquare = Rect(
                width / 2 - size.width / 2,
                0,
                width / 2 + size.width / 2,
                size.height
            )
            if (y < ctSquare.bottom) {
                (width - size.width) / 2 - w
            } else {
                width - w
            }
        },
        figureLocation = { width, size ->
            Point(width / 2 - size.width / 2, 0)
        }
    ),
    ArticleTemplate.Figure_Right to TemplateCallbacks(
        nextPosition = { lines, w_width, w_height, size ->
            val rtSquare = Rect(width - size.width, 0, width, size.height)
            val y = if (lines.isEmpty()) 0 else lines.last().bottom
            val x = 0
            Point(x, y)
        },
        widthNegotiation = { x, y, w, h, size ->
            val rtSquare = Rect(width - size.width, 0, width, size.height)
            if (y < rtSquare.bottom) {
                width - rtSquare.width() - w
            } else {
                width - w
            }
        },
        figureLocation = { width, size ->
            Point(width - size.width, 0)
        }
    )
)

@Composable
fun ArticleText(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle,
    template: ArticleTemplate,
    children: @Composable() () -> Unit
) {
    val lastLayout = state<android.text.Layout?> { null }
    val paint = remember(textStyle) { textStyle.toPaint() }
    val callback = TEMPLATE_MAP[template] ?: throw RuntimeException("Unknown Tempalte: $template")
    Layout(
        children = children,
        modifier = modifier.drawBehind {
            drawCanvas { canvas, _ ->
                lastLayout.value?.draw(canvas.nativeCanvas)
            }
        }
    ) { measurables, constraints, layoutDirection ->
        val figure = measurables[0]
        val placeable = figure.measure(constraints = constraints, layoutDirection = layoutDirection)
        val size = Size(placeable.width.value, placeable.height.value)

        val newLayout = AdvancedTextLayout.create(
            text = text,
            paint = paint,
            widthConstraint = constraints.maxWidth.value,
            align = android.text.Layout.Alignment.ALIGN_NORMAL,
            nextPositionCallback = lambda@ { lines, word_w, word_h ->
                callback.nextPosition(this, lines, word_w, word_h, size)
            },
            widthNegotiaion = { x, y, w, h ->
                callback.widthNegotiation(this, x, y, w, h, size)
            }
        )
        lastLayout.value = newLayout

        layout(
            newLayout.width.ipx,
            newLayout.height.ipx,
            mapOf()
        ) {
            val location = callback.figureLocation(newLayout.width, size)
            placeable.place(location.x.ipx, location.y.ipx)
        }
    }
}