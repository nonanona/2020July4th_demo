package com.example.newspapertemplate.text

import android.annotation.SuppressLint
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.text.BreakIterator

data class Line(
    val start: Int,
    val length: Int,
    val x: Int,
    val y: Int,
    val left: Float,
    val top: Int,
    val right: Float,
    val bottom: Int,
    val ascent: Int,
    val descent: Int,
    val paraDirection: Int,
    val directions: Layout.Directions,
    val widthConstraint: Float
) {
    val height: Int = bottom - top
    val width: Int = Math.ceil(right.toDouble() - left).toInt()
    val end = start + length
}

data class LayoutContext(
    val width: Int
)

typealias NextPositionCallback = LayoutContext.(lines: List<Line>, width: Int, height: Int) -> Point?
typealias WidthNegotiationCallback = LayoutContext.(x: Int, y: Int, w: Int, h: Int) -> Int

class AdvancedTextLayout private constructor(
    text: CharSequence,
    paint: TextPaint,
    width: Int,
    align: Alignment,
    val lines: List<Line>
): Layout(text, paint, width, align, 1f, 0f) {

    private val height_ = lines.maxByOrNull { it.bottom }!!.bottom

    // Metrics
    override fun getHeight(): Int = height_
    // width is constructor param

    var drawTextLineTopCallSite = -1

    // Lines
    override fun getLineTop(line: Int): Int {
        // Workaround getLineBottom/getLineBaseline is public final
        val caller = Thread.currentThread().stackTrace[3]
        return when (caller.methodName) {
            "getLineBottom", "getLineBaseline"-> lines[line-1].bottom
            "drawText" -> {
                // drawText calls getLineTop multiple times. The first call is for getting line top
                // vertical location, and following calls are for getting bottom location. Keep
                // lineNumber as a callsite location, and treat bottom access if the caller line
                // number is different from the first call.
                if (drawTextLineTopCallSite == -1) {
                    drawTextLineTopCallSite = caller.lineNumber
                }
                if (caller.lineNumber <= drawTextLineTopCallSite) {
                    lines[line].top
                } else {

                    // Hooking for setting word spacing
                    val lineObj = lines[line - 1]
                    val wsCount = countWhitespace(lineObj.start, lineObj.end)

                    val wordSpacing = if (wsCount != 0 && line != lines.size) {
                        (lineObj.widthConstraint - lineObj.width) / wsCount
                    } else {
                        0f
                    }

                    drawCanvas?.wordSpacing = wordSpacing
                    lines[line - 1].bottom
                }
            }
            else -> if (line == lineCount) {
                lines[line - 1].bottom
            } else {
                lines[line].top
            }
        }
    }

    class CanvasWrapper(val c: Canvas) : Canvas() {

        var wordSpacing: Float = 0f

        override fun drawText(
            text: CharSequence,
            start: Int,
            end: Int,
            x: Float,
            y: Float,
            paint: Paint
        ) {
            paint.wordSpacing = wordSpacing
            c.drawText(text, start, end, x, y, paint)
        }

        override fun translate(dx: Float, dy: Float) {
            c.translate(dx, dy)
        }

        override fun drawPath(path: Path, paint: Paint) {
            c.drawPath(path, paint)
        }

        override fun getClipBounds(bounds: Rect?): Boolean {
            return c.getClipBounds(bounds)
        }

        override fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
            c.drawRect(left, top, right, bottom, paint)
        }

        override fun drawTextRun(
            text: CharSequence,
            start: Int,
            end: Int,
            contextStart: Int,
            contextEnd: Int,
            x: Float,
            y: Float,
            isRtl: Boolean,
            paint: Paint
        ) {
            paint.wordSpacing = wordSpacing
            c.drawTextRun(text, start, end, contextStart, contextEnd, x, y, isRtl, paint)
        }

        override fun drawTextRun(
            text: CharArray,
            index: Int,
            count: Int,
            contextIndex: Int,
            contextCount: Int,
            x: Float,
            y: Float,
            isRtl: Boolean,
            paint: Paint
        ) {
            paint.wordSpacing = wordSpacing
            c.drawTextRun(text, index, count, contextIndex, contextCount, x, y, isRtl, paint)
        }
    }

    var drawCanvas: CanvasWrapper? = null

    override fun draw(
        canvas: Canvas,
        highlight: Path?,
        highlightPaint: Paint?,
        cursorOffsetVertical: Int
    ) {
        drawCanvas = CanvasWrapper(canvas)
        super.draw(drawCanvas, highlight, highlightPaint, cursorOffsetVertical)
        drawCanvas = null
    }

    fun countWhitespace(start: Int, end: Int): Int {
        var c = 0
        var seeNonSpace = false
        val len = end - start
        for (i in 0 until len) {
            if (text[end - i - 1] == ' ') {
                if (seeNonSpace) {
                    c++
                }
            } else {
                seeNonSpace = true
            }
        }
        return c
    }

    override fun getLineLeft(line: Int): Float {
        val caller = Thread.currentThread().stackTrace[3]
        if (caller.methodName == "bringPointIntoView") return 0f
        return lines[line].left
    }
    override fun getLineRight(line: Int): Float = lines[line].right
    // getLineBottom is public final. See getLineTop for workaround.

    // getLineAscent is public final
    override fun getLineDescent(line: Int): Int = lines[line].descent

    override fun getLineCount(): Int = lines.size
    override fun getLineStart(line: Int): Int =
        if (line == lineCount) lines[line - 1].end else lines[line].start
    // getLineEnd is public final, but it works.

    // Directions class is hidden.
    override fun getLineDirections(line: Int): Directions = lines[line].directions
    override fun getParagraphDirection(line: Int): Int = lines[line].paraDirection

    // TODO: Suppot paddings
    override fun getTopPadding(): Int = 0
    override fun getBottomPadding(): Int = 0


    override fun getEllipsisCount(line: Int): Int = 0 // no ellipsis support
    override fun getEllipsisStart(line: Int): Int = 0 // no ellipsis support
    override fun getLineContainsTab(line: Int): Boolean = false // no tab support

    // Overriding hidden API
    fun getIndentAdjust(line: Int, alignment: Alignment): Int = lines[line].left.toInt()



    // The original getLineForVertical returns upperBounds but needs to draw lowerBounds if multiple
    // lines in the same line.
    override fun getLineForVertical(vertical: Int): Int {
        var high = lineCount
        var low = -1

        while (high - low > 1) {
            val guess = (high + low) / 2
            if (getLineTop(guess) >= vertical) high = guess else low = guess
        }

        return if (low < 0) 0 else low
    }

    companion object {
        fun create(
            text: CharSequence,
            paint: TextPaint,
            widthConstraint: Int,
            align: Alignment,
            nextPositionCallback: NextPositionCallback,
            widthNegotiaion: WidthNegotiationCallback
        ): AdvancedTextLayout {
            val lines = mutableListOf<Line>()
            var currentStart = 0
            val brk = BreakIterator.getLineInstance().apply { setText(text.toString()) }
            val ctx = LayoutContext(widthConstraint)
            while (currentStart < text.length) {
                val nextBreakOp = brk.following(currentStart)
                val tmpLayout = createLayout(text, currentStart, nextBreakOp, paint, Int.MAX_VALUE)
                val pos = ctx.nextPositionCallback(
                    lines,
                    Math.ceil(tmpLayout.getLineWidth(0).toDouble()).toInt(),
                    tmpLayout.height
                ) ?: break
                var width = 0
                var height = 0
                var line: Line? = null
                do {
                    val diff =  ctx.widthNegotiaion(pos.x, pos.y, width, height)
                    if (diff != 0) {
                        width += diff
                        createLine(text, currentStart, paint, pos, width).also {
                            line = it
                            height = it.height
                        }
                    }
                } while (diff != 0)

                // If line is null, it is likely negotiation failed. Repeat requesting nextPosition.
                line?.let {
                    lines.add(it)
                    currentStart = it.end
                }
            }
            return AdvancedTextLayout(text, paint, widthConstraint, align, lines)
        }

        private fun createLine(
            text: CharSequence,
            start: Int,
            paint: TextPaint,
            pos: Point,
            width: Int
        ): Line {
            val layout = createLayout(text, start, text.length, paint, width)
            return Line(
                start = start,
                length = layout.getLineEnd(0) - layout.getLineStart(0),
                x = pos.x,
                y = pos.y,
                left = pos.x + layout.getLineLeft(0),
                top = pos.y + layout.getLineTop(0),
                right = pos.x + layout.getLineRight(0),
                bottom = pos.y + layout.getLineBottom(0),
                ascent = layout.getLineAscent(0),
                descent = layout.getLineDescent(0),
                paraDirection = layout.getParagraphDirection(0),
                directions = layout.getLineDirections(0),
                widthConstraint = width.toFloat(),
            )
        }

        @SuppressLint("WrongConstant")
        private fun createLayout(
            text: CharSequence,
            start: Int = 0,
            end: Int = text.length,
            paint: TextPaint,
            width: Int = Int.MAX_VALUE
        ) = StaticLayout.Builder.obtain(text, start, end, paint, width)
                .setIncludePad(false)
                .setBreakStrategy(BREAK_STRATEGY_SIMPLE) // Only greedy line break is supported
                .setHyphenationFrequency(HYPHENATION_FREQUENCY_NONE) // no hyphenation is supported
                .setMaxLines(2) // No need to calculate 2 or more lines
                .build()
    }
}