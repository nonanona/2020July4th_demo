package com.example.newspapertemplate.views

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.text.BoringLayout
import android.text.Layout
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import com.example.newspapertemplate.Demo
import com.example.newspapertemplate.text.AdvancedTextLayout

class ArticleTextView : TextView {
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
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    var demo: Demo? = null
        get() = field
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        demo?.boundsDrawer?.invoke(canvas)
    }

    override fun onPreDraw(): Boolean {
        if (movementMethod != null)
            return super.onPreDraw()
        return true
    }

    // Overriding hidden API
    fun makeSingleLayout(
        wantWidth: Int,
        boring: BoringLayout.Metrics,
        ellipsisWidth: Int,
        alignment: Layout.Alignment,
        shouldEllipsize: Boolean,
        effectiveEllipsis: TextUtils.TruncateAt?,
        useSaved: Boolean
    ): Layout {
        // TODO transformation
        val demo = demo ?: return BoringLayout.make(text, paint, wantWidth, alignment, 1f, 0f, null, false)
        val layout = AdvancedTextLayout.create(
            text = text,
            paint = paint,
            widthConstraint = wantWidth,
            align = Layout.Alignment.ALIGN_NORMAL,
            nextPositionCallback = demo.nextPosition,
            widthNegotiaion = demo.widthNegotiation
        )
        return layout
    }
}