@file:SuppressLint("DefaultLocale", "NewApi")
package com.macwap.rdxrasel.shimmer

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.macwap.rdxrasel.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan


class ShimmerLayout @SuppressLint("NewApi") constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int
) : FrameLayout(context, attrs, defStyle) {
    private var maskOffsetX = 0
    private var maskRect: Rect? = null
    private var gradientTexturePaint: Paint? = null
    private var maskAnimator: ValueAnimator? = null

    private var localMaskBitmap: Bitmap? = null
    private var maskBitmap: Bitmap? = null
    private var canvasForShimmerMask: Canvas? = null

    private var isAnimationReversed = false
    private var isAnimationStarted = false
    private var autoStart = false
    private var shimmerAnimationDuration = 0
    private var shimmerColor = 0
    private var originalShimmerColor = 0
    private var shimmerAngle = 0
    private var maskWidth = 0f
    private var gradientCenterColorWidth = 0f
    private var shimmerAnimationType = AnimationType.WAVE

    private var startAnimationPreDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    private enum class AnimationType {
        WAVE, PULSE
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0)

    init {
        setWillNotDraw(false)

        val a = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.ShimmerLayout,
            0, 0
        )

        try {
            shimmerAngle = a.getInteger(R.styleable.ShimmerLayout_shimmer_angle, DEFAULT_ANGLE.toInt())
            shimmerAnimationDuration = a.getInteger(
                R.styleable.ShimmerLayout_shimmer_animation_duration,
                DEFAULT_ANIMATION_DURATION
            )
            originalShimmerColor = a.getColor(R.styleable.ShimmerLayout_shimmer_color, getColor(R.color.shimmer_color))
            shimmerColor = originalShimmerColor
            autoStart = a.getBoolean(R.styleable.ShimmerFrameLayout_shimmer_auto_start, false)
            maskWidth = a.getFloat(R.styleable.ShimmerLayout_shimmer_mask_width, 0.5f)
            gradientCenterColorWidth = a.getFloat(R.styleable.ShimmerLayout_shimmer_gradient_center_color_width, 0.1f)
            isAnimationReversed = a.getBoolean(R.styleable.ShimmerLayout_shimmer_reverse_animation, false)
            val animationType = a.getInteger(R.styleable.ShimmerLayout_shimmer_animation_type, 0)
            shimmerAnimationType = if (animationType == 1) AnimationType.PULSE else AnimationType.WAVE
        } finally {
            a.recycle()
        }

        setMaskWidth(maskWidth)
        setGradientCenterColorWidth(gradientCenterColorWidth)
        setShimmerAngle(shimmerAngle)
        if (autoStart && getVisibility() == VISIBLE) {
            startShimmerAnimation()
        }
    }

    @SuppressLint("NewApi")
    override fun onDetachedFromWindow() {
        resetShimmering()
        super.onDetachedFromWindow()
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (!isAnimationStarted || getWidth() <= 0 || getHeight() <= 0) {
            super.dispatchDraw(canvas)
        } else {
            dispatchDrawShimmer(canvas)
        }
    }

    @SuppressLint("NewApi")
    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == VISIBLE) {
            if (autoStart) {
                startShimmerAnimation()
            }
        } else {
            stopShimmerAnimation()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    fun startShimmerAnimation() {
        if (isAnimationStarted) {
            return
        }

        if (getWidth() == 0) {
            startAnimationPreDrawListener = object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    getViewTreeObserver().removeOnPreDrawListener(this)
                    startShimmerAnimation()

                    return true
                }
            }

            getViewTreeObserver().addOnPreDrawListener(startAnimationPreDrawListener)

            return
        }
        if (maskRect == null) {
            maskRect = calculateBitmapMaskRect()
        }

        val animator = this.shimmerAnimation
        animator.start()
        isAnimationStarted = true
    }

    @SuppressLint("NewApi")
    fun stopShimmerAnimation() {
        if (startAnimationPreDrawListener != null) {
            getViewTreeObserver().removeOnPreDrawListener(startAnimationPreDrawListener)
        }

        resetShimmering()
    }

    @SuppressLint("NewApi")
    fun setShimmerColor(shimmerColor: Int) {
        this.originalShimmerColor = shimmerColor
        this.shimmerColor = originalShimmerColor
        resetIfStarted()
    }

    @SuppressLint("NewApi")
    fun setShimmerAnimationDuration(durationMillis: Int) {
        this.shimmerAnimationDuration = durationMillis
        resetIfStarted()
    }

    @SuppressLint("NewApi")
    fun setAnimationReversed(animationReversed: Boolean) {
        this.isAnimationReversed = animationReversed
        resetIfStarted()
    }

    /**
     * Set the angle of the shimmer effect in clockwise direction in degrees.
     * The angle must be between {@value #MIN_ANGLE_VALUE} and {@value #MAX_ANGLE_VALUE}.
     *
     * @param angle The angle to be set
     */
    @SuppressLint("NewApi")
    fun setShimmerAngle(angle: Int) {
        require(!(angle < MIN_ANGLE_VALUE || MAX_ANGLE_VALUE < angle)) {
            String.format(
                "shimmerAngle value must be between %d and %d",
                MIN_ANGLE_VALUE,
                MAX_ANGLE_VALUE
            )
        }
        this.shimmerAngle = angle
        resetIfStarted()
    }

    /**
     * Sets the width of the shimmer line to a value higher than 0 to less or equal to 1.
     * 1 means the width of the shimmer line is equal to half of the width of the ShimmerLayout.
     * The default value is 0.5.
     *
     * @param maskWidth The width of the shimmer line.
     */
    @SuppressLint("NewApi")
    fun setMaskWidth(maskWidth: Float) {
        require(!(maskWidth <= MIN_MASK_WIDTH_VALUE || MAX_MASK_WIDTH_VALUE < maskWidth)) {
            String.format(
                "maskWidth value must be higher than %d and less or equal to %d",
                MIN_MASK_WIDTH_VALUE, MAX_MASK_WIDTH_VALUE
            )
        }

        this.maskWidth = maskWidth
        resetIfStarted()
    }

    /**
     * Sets the width of the center gradient color to a value higher than 0 to less than 1.
     * 0.99 means that the whole shimmer line will have this color with a little transparent edges.
     * The default value is 0.1.
     *
     * @param gradientCenterColorWidth The width of the center gradient color.
     */
    fun setGradientCenterColorWidth(gradientCenterColorWidth: Float) {
        require(
            !(gradientCenterColorWidth <= MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE
                    || MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE <= gradientCenterColorWidth)
        ) {
            String.format(
                "gradientCenterColorWidth value must be higher than %d and less than %d",
                MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE, MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE
            )
        }

        this.gradientCenterColorWidth = gradientCenterColorWidth
        resetIfStarted()
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private fun resetIfStarted() {
        if (isAnimationStarted) {
            resetShimmering()
            startShimmerAnimation()
        }
    }

    private fun dispatchDrawShimmer(canvas: Canvas) {
        super.dispatchDraw(canvas)

        localMaskBitmap = getMaskBitmap()
        if (localMaskBitmap == null) {
            return
        }

        if (canvasForShimmerMask == null) {
            canvasForShimmerMask = Canvas(localMaskBitmap!!)
        }

        canvasForShimmerMask!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        canvasForShimmerMask!!.save()
        val offset = if (shimmerAnimationType == AnimationType.WAVE) maskOffsetX.toFloat() else 0f
        canvasForShimmerMask!!.translate(-offset, 0f)

        super.dispatchDraw(canvasForShimmerMask!!)

        canvasForShimmerMask!!.restore()

        drawShimmer(canvas)

        localMaskBitmap = null
    }

    private fun drawShimmer(destinationCanvas: Canvas) {
        createShimmerPaint()

        destinationCanvas.save()

        val offset = if (shimmerAnimationType == AnimationType.WAVE) maskOffsetX.toFloat() else 0f
        destinationCanvas.translate(offset, 0f)
        destinationCanvas.drawRect(
            maskRect!!.left.toFloat(),
            0f,
            maskRect!!.width().toFloat(),
            maskRect!!.height().toFloat(),
            gradientTexturePaint!!
        )

        destinationCanvas.restore()
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private fun resetShimmering() {
        if (maskAnimator != null) {
            maskAnimator!!.end()
            maskAnimator!!.removeAllUpdateListeners()
        }

        maskAnimator = null
        gradientTexturePaint = null
        isAnimationStarted = false

        releaseBitMaps()
    }

    private fun releaseBitMaps() {
        canvasForShimmerMask = null

        if (maskBitmap != null) {
            maskBitmap!!.recycle()
            maskBitmap = null
        }
    }

    private fun getMaskBitmap(): Bitmap? {
        if (maskBitmap == null) {
            maskBitmap = createBitmap(maskRect!!.width(), getHeight())
        }

        return maskBitmap
    }

    private fun createShimmerPaint() {
        if (gradientTexturePaint != null) {
            return
        }

        val maskBitmapShader = BitmapShader(localMaskBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        val gradient: Shader = if (shimmerAnimationType == AnimationType.PULSE) {
            LinearGradient(0f, 0f, 0f, height.toFloat(), intArrayOf(shimmerColor, shimmerColor), null, Shader.TileMode.CLAMP)
        } else { // WAVE
            val edgeColor = reduceColorAlphaValueToZero(shimmerColor)
            val shimmerLineWidth = getWidth() / 2 * maskWidth
            val yPosition = (if (0 <= shimmerAngle) getHeight() else 0).toFloat()

            LinearGradient(
                0f, yPosition,
                cos(Math.toRadians(shimmerAngle.toDouble())).toFloat() * shimmerLineWidth,
                yPosition + sin(Math.toRadians(shimmerAngle.toDouble())).toFloat() * shimmerLineWidth,
                intArrayOf(edgeColor, shimmerColor, shimmerColor, edgeColor),
                this.gradientColorDistribution,
                Shader.TileMode.CLAMP
            )
        }

        val composeShader = ComposeShader(gradient, maskBitmapShader, PorterDuff.Mode.DST_IN)

        gradientTexturePaint = Paint()
        gradientTexturePaint!!.isAntiAlias = true
        gradientTexturePaint!!.isDither = true
        gradientTexturePaint!!.isFilterBitmap = true
        gradientTexturePaint!!.shader = composeShader
    }


    @get:RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private val shimmerAnimation: Animator
        get() {
            return if (shimmerAnimationType == AnimationType.PULSE) {
                getPulseAnimation()
            } else {
                getWaveAnimation()
            }
        }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun getPulseAnimation(): Animator {
        maskAnimator?.let { return it }

        val animator = ValueAnimator.ofFloat(0.0f, 1.0f, 0.0f)
        animator.duration = shimmerAnimationDuration.toLong()
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            val alphaFactor = animation.animatedValue as Float
            val alpha = Color.alpha(originalShimmerColor)
            val red = Color.red(originalShimmerColor)
            val green = Color.green(originalShimmerColor)
            val blue = Color.blue(originalShimmerColor)
            shimmerColor = Color.argb((alpha * alphaFactor).toInt(), red, green, blue)
            gradientTexturePaint = null
            invalidate()
        }
        maskAnimator = animator
        return maskAnimator!!
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private fun getWaveAnimation(): Animator {
        maskAnimator?.let { return it }

        if (maskRect == null) {
            maskRect = calculateBitmapMaskRect()
        }

        val animationToX = getWidth()
        val animationFromX: Int

        if (getWidth() > maskRect!!.width()) {
            animationFromX = -animationToX
        } else {
            animationFromX = -maskRect!!.width()
        }

        val shimmerBitmapWidth = maskRect!!.width()
        val shimmerAnimationFullLength = animationToX - animationFromX

        maskAnimator = if (isAnimationReversed)
            ValueAnimator.ofInt(shimmerAnimationFullLength, 0)
        else
            ValueAnimator.ofInt(0, shimmerAnimationFullLength)
        maskAnimator!!.setDuration(shimmerAnimationDuration.toLong())
        maskAnimator!!.setRepeatCount(ObjectAnimator.INFINITE)

        maskAnimator!!.addUpdateListener { animation ->
            maskOffsetX = animationFromX + (animation.animatedValue as? Int ?: 0)

            if (maskOffsetX + shimmerBitmapWidth >= 0) {
                invalidate()
            }
        }

        return maskAnimator!!
    }

    private fun createBitmap(width: Int, height: Int): Bitmap? {
        try {
            return Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        } catch (e: OutOfMemoryError) {
            System.gc()
            return null
        }
    }

    private fun getColor(id: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getContext().getColor(id)
        } else {
            @Suppress("DEPRECATION")
            return getResources().getColor(id)
        }
    }

    private fun reduceColorAlphaValueToZero(actualColor: Int): Int {
        return Color.argb(
            0,
            Color.red(actualColor),
            Color.green(actualColor),
            Color.blue(actualColor)
        )
    }

    private fun calculateBitmapMaskRect(): Rect {
        if (shimmerAnimationType == AnimationType.PULSE) {
            return Rect(0, 0, width, height)
        }
        return Rect(0, 0, calculateMaskWidth(), getHeight())
    }

    private fun calculateMaskWidth(): Int {
        val shimmerLineBottomWidth = (getWidth() / 2 * maskWidth) / cos(Math.toRadians(abs(shimmerAngle).toDouble()))
        val shimmerLineRemainingTopWidth = getHeight() * tan(Math.toRadians(abs(shimmerAngle).toDouble()))

        return (shimmerLineBottomWidth + shimmerLineRemainingTopWidth).toInt()
    }

    private val gradientColorDistribution: FloatArray
        get() {
            val colorDistribution = FloatArray(4)

            colorDistribution[0] = 0f
            colorDistribution[3] = 1f

            colorDistribution[1] = 0.5f - gradientCenterColorWidth / 2f
            colorDistribution[2] = 0.5f + gradientCenterColorWidth / 2f

            return colorDistribution
        }

    companion object {
        private const val DEFAULT_ANIMATION_DURATION = 1500

        private const val DEFAULT_ANGLE: Byte = 20

        private val MIN_ANGLE_VALUE: Byte = -45
        private const val MAX_ANGLE_VALUE: Byte = 45
        private const val MIN_MASK_WIDTH_VALUE: Byte = 0
        private const val MAX_MASK_WIDTH_VALUE: Byte = 1

        private const val MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE: Byte = 0
        private const val MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE: Byte = 1
    }
}