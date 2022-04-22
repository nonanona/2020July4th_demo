package com.example.newspapertemplate

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect

private val PAINT = Paint().apply {
    strokeWidth = 4f
    color = Color.RED
    style = Paint.Style.STROKE
}

private const val SQUARE_WIDTH = 360
private const val SQUARE_HEIGHT = 360

private const val CIRCLE_RAD = 500
private const val CIRCLE_C_X = 540
private const val CIRCLE_C_Y = 540

val DEMOS = listOf(
    Demo(
        title = "Left-Top Square Out",
        nextPosition = { lines, w_width, w_height ->
            val ltSquare = Rect(0, 0, SQUARE_WIDTH, SQUARE_HEIGHT)
            val y = if (lines.isEmpty()) 0 else lines.last().bottom
            val x = if (y < ltSquare.bottom) ltSquare.right else 0
            Point(x, y)
        },
        widthNegotiation = { x, y, w, h ->
            val ltSquare = Rect(0, 0, SQUARE_WIDTH, SQUARE_HEIGHT)
            if (x != 0) {
                width - ltSquare.width() - w
            } else {
                width - w
            }
        },
        boundsDrawer = {canvas ->
            val ltSquare = Rect(0, 0, SQUARE_WIDTH, SQUARE_HEIGHT)
            canvas.drawRect(ltSquare, PAINT)
        }
    ),
    Demo(
        title = "Right-Top Square Out",
        nextPosition = { lines, w_width, w_height ->
            val rtSquare = Rect(SQUARE_WIDTH * 2, 0, SQUARE_WIDTH * 3, SQUARE_HEIGHT)
            val y = if (lines.isEmpty()) 0 else lines.last().bottom
            val x = 0
            Point(x, y)
        },
        widthNegotiation = { x, y, w, h ->
            val rtSquare = Rect(SQUARE_WIDTH * 2, 0, SQUARE_WIDTH * 3, SQUARE_HEIGHT)
            if (y < rtSquare.bottom) {
                width - rtSquare.width() - w
            } else {
                width - w
            }
        },
        boundsDrawer = {canvas ->
            val rtSquare = Rect(SQUARE_WIDTH * 2, 0, SQUARE_WIDTH * 3, SQUARE_HEIGHT)
            canvas.drawRect(rtSquare, PAINT)
        }
    ),
    Demo(
        title = "Center-Top Square Out",
        nextPosition = lambda@ { lines, w_width, w_height ->
            val ctSquare = Rect(SQUARE_WIDTH, 0, SQUARE_WIDTH * 2, SQUARE_HEIGHT)
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
        widthNegotiation = { x, y, w, h ->
            val ctSquare = Rect(SQUARE_WIDTH, 0, SQUARE_WIDTH * 2, SQUARE_HEIGHT)
            if (y < ctSquare.bottom) {
                SQUARE_WIDTH - w
            } else {
                width - w
            }
        },
        boundsDrawer = {canvas ->
            val ctSquare = Rect(SQUARE_WIDTH, 0, SQUARE_WIDTH * 2, SQUARE_HEIGHT)
            canvas.drawRect(ctSquare, PAINT)
        }
    ),
    Demo(
        title = "Circle In",
        nextPosition = lambda@ { lines, w_width, w_height ->
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
        widthNegotiation = { x, y, w, h ->
            val x2 = x - CIRCLE_C_X
            val width = Math.abs(x2) * 2
            width - w
        },
        boundsDrawer = { canvas ->
            /*canvas.drawCircle(
                CIRCLE_C_X.toFloat(),
                CIRCLE_C_Y.toFloat(),
                CIRCLE_RAD.toFloat(),
                PAINT
            )*/
        }
    )
)