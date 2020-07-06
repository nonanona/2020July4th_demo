package com.example.newspapertemplate.compose

import android.graphics.Point
import android.text.TextPaint
import androidx.compose.Composable
import androidx.compose.remember
import androidx.compose.state
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Layout
import androidx.ui.core.Modifier
import androidx.ui.core.drawBehind
import androidx.ui.graphics.drawscope.drawCanvas
import androidx.ui.text.TextStyle
import androidx.ui.unit.ipx
import androidx.ui.unit.toPx
import com.example.newspapertemplate.text.AdvancedTextLayout

@Composable
fun TextStyle.toPaint() : TextPaint {
    val res = TextPaint()
    with(DensityAmbient.current) {
        res.textSize = this@toPaint.fontSize.toPx().value
    }
    return res
}

@Composable
fun CircleText(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle
) {
    val lastLayout = state<android.text.Layout?> { null }
    val paint = remember(textStyle) { textStyle.toPaint() }
    Layout(
        children = {},
        modifier = modifier.drawBehind {
            drawCanvas { canvas, _ ->
                lastLayout.value?.draw(canvas.nativeCanvas)
            }
        }
    ) { measurables, constraints, layoutDirection ->
        val CIRCLE_RAD = Math.min(constraints.maxWidth.toPx().value, constraints.maxHeight.toPx().value) / 2
        val CIRCLE_C_X = constraints.maxWidth.toPx().value / 2
        val CIRCLE_C_Y = CIRCLE_RAD

        val newLayout = AdvancedTextLayout.create(
            text = text,
            paint = paint,
            widthConstraint = constraints.maxWidth.value,
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

        layout(newLayout.width.ipx, newLayout.height.ipx, mapOf()) {}
    }
}