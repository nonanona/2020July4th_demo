package com.example.newspapertemplate.compose

import android.graphics.Paint
import android.graphics.Point
import android.text.TextPaint
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
import androidx.compose.ui.unit.Density
import com.example.newspapertemplate.text.LayoutContext
import com.example.newspapertemplate.text.Line
import com.example.newspapertemplate.text.AdvancedTextLayout

fun TextStyle.toPaint(density: Density) : TextPaint {
    val res = TextPaint()
    with(density) {
        res.textSize = this@toPaint.fontSize.toPx()
    }
    res.flags = Paint.ANTI_ALIAS_FLAG
    return res
}

@Composable
fun CircleText(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle
) {
    val lastLayout = remember { mutableStateOf<android.text.Layout?>(null) }
    val density = LocalDensity.current
    val paint = remember(textStyle, density) { textStyle.toPaint(density) }
    Layout(
        content = {},
        modifier = modifier.drawBehind {
            drawIntoCanvas { canvas, ->
                lastLayout.value?.draw(canvas.nativeCanvas)
            }
        }
    ) { measurables, constraints ->
        val CIRCLE_RAD = Math.min(constraints.maxWidth, constraints.maxHeight) / 2
        val CIRCLE_C_X = constraints.maxWidth / 2
        val CIRCLE_C_Y = CIRCLE_RAD

        val newLayout = AdvancedTextLayout.create(
            text = text,
            paint = paint,
            widthConstraint = constraints.maxWidth,
            align = android.text.Layout.Alignment.ALIGN_NORMAL,
            nextPositionCallback = lambda@ { lines, w_width, w_height ->
                if (lines.isEmpty()) {
                    val x2 = - w_width / 2.0
                    val y2 = - Math.sqrt(CIRCLE_RAD * CIRCLE_RAD - x2 * x2)
                    return@lambda Point((x2 + CIRCLE_C_X).toInt(), (y2 + CIRCLE_C_Y).toInt())
                }

                val bottom = lines.last().bottom
                if (bottom + w_height> CIRCLE_C_Y + CIRCLE_RAD) return@lambda null

                val y2 = lines.last().bottom - CIRCLE_C_Y
                if (y2 < 0) {  // Northern Hemisphere
                    val x2 = -Math.sqrt((CIRCLE_RAD * CIRCLE_RAD - y2 * y2).toDouble())
                    Point((x2 + CIRCLE_C_X).toInt(), (y2 + CIRCLE_C_Y).toInt())
                } else {  // Sourthern Hemisphere
                    val x2 = -Math.sqrt((CIRCLE_RAD * CIRCLE_RAD - (y2 + w_height) * (y2 + w_height)).toDouble())
                    Point((x2 + CIRCLE_C_X).toInt(), (y2 + CIRCLE_C_Y).toInt())
                }
            },
            widthNegotiaion = { x, y, w, h ->
                val x2 = x - CIRCLE_C_X.toInt()
                val width = Math.abs(x2) * 2
                width - w
            }
        )
        lastLayout.value = newLayout

        layout(newLayout.width, newLayout.height, mapOf()) {}
    }
}