package com.example.newspapertemplate.views

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.example.newspapertemplate.Demo
import com.example.newspapertemplate.R
import com.example.newspapertemplate.text.AdvancedTextLayout

class Article : View {
    @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    fun init() {
        // Read all TextView attributes
    }

    val paint = TextPaint().apply {
        textSize = 32f
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private var layout: Layout? = null
    var demo: Demo? = null
        get() = field
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        layout?.draw(canvas)

        demo?.boundsDrawer?.invoke(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val demo = demo?: return super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        layout = AdvancedTextLayout.create(
            text = resources.getText(R.string.lorem_ipsum).repeat(10),
            paint = paint,
            widthConstraint = width,
            align = Layout.Alignment.ALIGN_NORMAL,
            nextPositionCallback = demo.nextPosition,
            widthNegotiaion = demo.widthNegotiation
        ).also {
            setMeasuredDimension(
                it.width.coerceAtMost(width),
                it.height.coerceAtMost(height))
        }
    }
}