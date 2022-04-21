package com.example.newspapertemplate.compose

import android.graphics.Point
import android.graphics.Rect
import android.util.Size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
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
    val lastLayout = remember { mutableStateOf<android.text.Layout?>(null) }
    val density = LocalDensity.current
    val paint = remember(textStyle, density) { textStyle.toPaint(density) }
    val callback = TEMPLATE_MAP[template] ?: throw RuntimeException("Unknown Tempalte: $template")
    Layout(
        content = children,
        modifier = modifier.drawBehind {
            drawIntoCanvas { canvas ->
                lastLayout.value?.draw(canvas.nativeCanvas)
            }
        }
    ) { measurables, constraints ->
        val figure = measurables[0]
        val placeable = figure.measure(constraints = constraints)
        val size = Size(placeable.width, placeable.height)

        val newLayout = AdvancedTextLayout.create(
            text = text,
            paint = paint,
            widthConstraint = constraints.maxWidth,
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
            newLayout.width,
            newLayout.height,
            mapOf()
        ) {
            val location = callback.figureLocation(newLayout.width, size)
            placeable.place(location.x, location.y)
        }
    }
}