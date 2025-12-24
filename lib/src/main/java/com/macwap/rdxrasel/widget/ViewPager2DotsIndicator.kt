package com.macwap.rdxrasel.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.macwap.rdxrasel.R

@SuppressLint("UseKtx")
class ViewPager2DotsIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private val dotSize: Float
    private val dotSpacing: Float
    private val selectedDotColor: Int
    private val dotColor: Int

    private val selectedDotPaint: Paint
    private val dotPaint: Paint

    private var count: Int = 0
    private var selectedPosition: Int = 0

    private var viewPager: ViewPager2? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ViewPager2DotsIndicator)

        dotColor = typedArray.getColor(
            R.styleable.ViewPager2DotsIndicator_dot_color,
            ContextCompat.getColor(context, R.color.colorTextSplash)
        )
        selectedDotColor = typedArray.getColor(
            R.styleable.ViewPager2DotsIndicator_dot_selected_color,
            ContextCompat.getColor(context, R.color.ic_launcher_background)
        )
        dotSize = typedArray.getDimension(R.styleable.ViewPager2DotsIndicator_dot_size, 24f)
        dotSpacing = typedArray.getDimension(R.styleable.ViewPager2DotsIndicator_dot_spacing, 16f)

        typedArray.recycle()

        dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = dotColor }
        selectedDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = selectedDotColor }
    }

    fun attachTo(viewPager: ViewPager2) {
        this.viewPager = viewPager
        this.count = viewPager.adapter?.itemCount ?: 0

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectedPosition = position
                invalidate()
            }
        })

        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = ((count * dotSize) + ((count - 1).coerceAtLeast(0) * dotSpacing)).toInt()
        val desiredHeight = dotSize.toInt()

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (count == 0) return

        // Total width required to draw all dots
        val totalDotsWidth = (count * dotSize) + ((count - 1) * dotSpacing)

        // Starting X position to center the dots horizontally
        var cx = (width - totalDotsWidth) / 2f + (dotSize / 2f)

        for (i in 0 until count) {
            val paint = if (i == selectedPosition) selectedDotPaint else dotPaint
            canvas.drawCircle(cx, height / 2f, dotSize / 2, paint)
            cx += dotSize + dotSpacing
        }
    }
}
