package com.macwap.rdxrasel.shimmer

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.view.animation.*
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import com.macwap.rdxrasel.R
import kotlin.math.*
import androidx.core.view.isVisible

/**
 * 🔥 PROFESSIONAL OPTIMIZED EnhancedShimmerLayout
 * - Fixed Breathing and StaggeredFade
 * - Memory optimized (reused objects)
 * - Smooth performance
 */
@SuppressLint("NewApi")
class EnhancedShimmerLayout @SuppressLint("NewApi") constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int
) : FrameLayout(context, attrs, defStyle) {

    // State
    private var isAnimationStarted = false
    private var isShimmerEnabled = true
    private var autoStart = false

    // Animation Type
    var primaryAnimationType = AnimationType.SHIMMER

    // Features
    private var enableWave = true
    private var enablePulse = false
    private var enableSparkles = false
    private var enableRippleOnTouch = false
    private var enableBreathing = false
    private var enableFadePulse = false
    private var enableStaggeredFade = false

    // Colors
    private var shimmerColor = "#66FFFFFF".toColorInt()
    private var pulseColor = "#66FFFFFF".toColorInt()
    private var sparkleColor = Color.WHITE
    private var rippleColor = "#44FFFFFF".toColorInt()

    // Wave config
    private var maskOffsetX = 0
    private var shimmerAngle = 20
    private var maskWidth = 0.5f
    private var gradientCenterColorWidth = 0.1f
    private var isAnimationReversed = false
    private var waveCount = 1
    private var shimmerAnimationDuration = 1500

    // Animation values
    private var pulseIntensity = 0f
    private var breathingScale = 1f
    private var breathingIntensity = 1.03f
    private var breathingDuration = 2000
    private var fadePulseAlpha = 1f
    private var staggeredProgress = 0f

    // Reusable Paints and Objects
    private val sparklePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
    private val ripplePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE }
    private val pulsePaint = Paint().apply { style = Paint.Style.FILL }
    private val staggeredPaint = Paint().apply { 
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN) 
    }
    private var shimmerPaint: Paint? = null
    private var maskRect: Rect? = null

    // Sparkles & Ripples
    private val sparkles = mutableListOf<Sparkle>()
    private var maxSparkles = 15
    private var sparkleSize = 8f
    private var sparkleFrequency = 0.3f
    private val ripples = mutableListOf<Ripple>()
    private var maxRipples = 3
    private var rippleStrokeWidth = 4f

    // Animators
    private var waveAnimator: ValueAnimator? = null
    private var pulseAnimator: ValueAnimator? = null
    private var sparkleAnimator: ValueAnimator? = null
    private var breathingAnimator: ValueAnimator? = null
    private var fadePulseAnimator: ValueAnimator? = null
    private var staggeredAnimator: ValueAnimator? = null

    private var maskBitmap: Bitmap? = null
    private var canvasForShimmerMask: Canvas? = null
    private var startAnimationPreDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    enum class AnimationType { SHIMMER }
    data class Sparkle(var x: Float, var y: Float, var alpha: Float, var size: Float, var velocity: Float)
    data class Ripple(var x: Float, var y: Float, var radius: Float, var alpha: Float, var maxRadius: Float)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0)

    init {
        setWillNotDraw(false)
        ripplePaint.strokeWidth = rippleStrokeWidth

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ShimmerLayout, 0, 0)
        try {
            shimmerAngle = a.getInteger(R.styleable.ShimmerLayout_shimmer_angle, 20)
            shimmerAnimationDuration = a.getInteger(R.styleable.ShimmerLayout_shimmer_animation_duration, 1500)
            shimmerColor = a.getColor(R.styleable.ShimmerLayout_shimmer_color, shimmerColor)
            autoStart = a.getBoolean(R.styleable.ShimmerFrameLayout_shimmer_auto_start, false)
            maskWidth = a.getFloat(R.styleable.ShimmerLayout_shimmer_mask_width, 0.5f)
            gradientCenterColorWidth = a.getFloat(R.styleable.ShimmerLayout_shimmer_gradient_center_color_width, 0.1f)
            isAnimationReversed = a.getBoolean(R.styleable.ShimmerLayout_shimmer_reverse_animation, false)
        } finally {
            a.recycle()
        }

        if (autoStart && isVisible) {
            startShimmerAnimation()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (!isAnimationStarted || !isShimmerEnabled || width <= 0 || height <= 0) {
            super.dispatchDraw(canvas)
            return
        }

        // Apply global effects like Breathing and Fade Pulse BEFORE children draw
        canvas.save()

        if (enableBreathing) {
            canvas.scale(breathingScale, breathingScale, width / 2f, height / 2f)
        }

        if (enableFadePulse) {
            val originalAlpha = alpha
            alpha = fadePulseAlpha * originalAlpha
            super.dispatchDraw(canvas)
            alpha = originalAlpha
        } else if (enableStaggeredFade) {
            // Staggered Fade requires a layer to mask the children
            canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
            super.dispatchDraw(canvas)
            drawStaggeredFadeMask(canvas)
            canvas.restore()
        } else {
            super.dispatchDraw(canvas)
        }

        // Draw overlays (Shimmer Wave, Pulse, Sparkles)
        drawOverlays(canvas)

        canvas.restore()
    }

    private fun drawOverlays(canvas: Canvas) {
        if (enableWave && waveCount > 0) {
            drawWaveEffect(canvas)
        }
        if (enablePulse) {
            pulsePaint.color = pulseColor
            pulsePaint.alpha = (pulseIntensity * 255).toInt()
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), pulsePaint)
        }
        if (enableSparkles) drawSparkles(canvas)
        if (enableRippleOnTouch) drawRipples(canvas)
    }

    private fun drawWaveEffect(canvas: Canvas) {
        val mask = getOrCreateMaskBitmap() ?: return
        if (canvasForShimmerMask == null) canvasForShimmerMask = Canvas(mask)

        canvasForShimmerMask!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvasForShimmerMask!!.save()
        canvasForShimmerMask!!.translate(-maskOffsetX.toFloat(), 0f)
        super.dispatchDraw(canvasForShimmerMask!!)
        canvasForShimmerMask!!.restore()

        if (shimmerPaint == null) createShimmerPaint(mask)
        
        maskRect?.let {
            canvas.save()
            canvas.translate(maskOffsetX.toFloat(), 0f)
            canvas.drawRect(it.left.toFloat(), 0f, it.width().toFloat(), it.height().toFloat(), shimmerPaint!!)
            canvas.restore()
        }
    }

    private fun drawStaggeredFadeMask(canvas: Canvas) {
        val fadeHeight = height * 0.4f
        val fadeCenter = height * staggeredProgress
        
        val gradient = LinearGradient(
            0f, fadeCenter - fadeHeight,
            0f, fadeCenter + fadeHeight,
            intArrayOf(Color.TRANSPARENT, Color.BLACK, Color.BLACK, Color.TRANSPARENT),
            floatArrayOf(0f, 0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        staggeredPaint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), staggeredPaint)
    }

    private fun drawSparkles(canvas: Canvas) {
        val iterator = sparkles.iterator()
        while (iterator.hasNext()) {
            val s = iterator.next()
            sparklePaint.color = sparkleColor
            sparklePaint.alpha = (s.alpha * 255).toInt()
            canvas.drawCircle(s.x, s.y, s.size, sparklePaint)
            s.alpha -= 0.02f * s.velocity
            s.size += 0.3f * s.velocity
            if (s.alpha <= 0f) iterator.remove()
        }
    }

    private fun drawRipples(canvas: Canvas) {
        val iterator = ripples.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next()
            ripplePaint.color = rippleColor
            ripplePaint.alpha = (r.alpha * 255).toInt()
            canvas.drawCircle(r.x, r.y, r.radius, ripplePaint)
            r.radius += r.maxRadius * 0.05f
            r.alpha -= 0.03f
            if (r.alpha <= 0f || r.radius >= r.maxRadius) iterator.remove()
        }
    }

    private fun createShimmerPaint(mask: Bitmap) {
        val maskBitmapShader = BitmapShader(mask, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val edgeColor = Color.argb(0, Color.red(shimmerColor), Color.green(shimmerColor), Color.blue(shimmerColor))
        val shimmerLineWidth = width / 2 * maskWidth
        val yPosition = (if (shimmerAngle >= 0) height else 0).toFloat()

        val gradient = LinearGradient(
            0f, yPosition,
            cos(Math.toRadians(shimmerAngle.toDouble())).toFloat() * shimmerLineWidth,
            yPosition + sin(Math.toRadians(shimmerAngle.toDouble())).toFloat() * shimmerLineWidth,
            intArrayOf(edgeColor, shimmerColor, shimmerColor, edgeColor),
            floatArrayOf(0f, max(0.5f - gradientCenterColorWidth / 2f, 0f), min(0.5f + gradientCenterColorWidth / 2f, 1f), 1f),
            Shader.TileMode.CLAMP
        )

        shimmerPaint = Paint().apply {
            isAntiAlias = true
            isDither = true
            isFilterBitmap = true
            this.shader = ComposeShader(gradient, maskBitmapShader, PorterDuff.Mode.DST_IN)
        }
    }

    fun startShimmerAnimation() {
        if (isAnimationStarted || !isShimmerEnabled) return

        if (width == 0) {
            startAnimationPreDrawListener = object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    viewTreeObserver.removeOnPreDrawListener(this)
                    startShimmerAnimation()
                    return true
                }
            }
            viewTreeObserver.addOnPreDrawListener(startAnimationPreDrawListener)
            return
        }

        if (maskRect == null) maskRect = calculateBitmapMaskRect()
        startAllAnimators()
        isAnimationStarted = true
    }

    private fun startAllAnimators() {
        if (enableWave) {
            val animationToX = width
            val animationFromX = if (width > (maskRect?.width() ?: 0)) -animationToX else -(maskRect?.width() ?: 0)
            val fullLength = animationToX - animationFromX
            waveAnimator = setupAnimator(if (isAnimationReversed) fullLength else 0, if (isAnimationReversed) 0 else fullLength, shimmerAnimationDuration.toLong()) {
                maskOffsetX = animationFromX + (it.animatedValue as Int)
                invalidate()
            }
        }

        if (enablePulse) {
            pulseAnimator = setupAnimator(0f, 0.3f, (shimmerAnimationDuration * 1.5).toLong(), true) {
                pulseIntensity = it.animatedValue as Float
                invalidate()
            }
        }

        if (enableBreathing) {
            breathingAnimator = setupAnimator(1f, breathingIntensity, breathingDuration.toLong(), true) {
                breathingScale = it.animatedValue as Float
                invalidate()
            }
        }

        if (enableSparkles) {
            sparkleAnimator = setupAnimator(0f, 1f, 100, false) {
                if (sparkles.size < maxSparkles && Math.random() < sparkleFrequency) {
                    sparkles.add(Sparkle((Math.random() * width).toFloat(), (Math.random() * height).toFloat(), 1f, sparkleSize, 1f))
                }
                invalidate()
            }
        }

        if (enableFadePulse) {
            fadePulseAnimator = setupAnimator(1f, 0.5f, shimmerAnimationDuration.toLong(), true) {
                fadePulseAlpha = it.animatedValue as Float
                invalidate()
            }
        }

        if (enableStaggeredFade) {
            staggeredAnimator = setupAnimator(0f, 1f, (shimmerAnimationDuration * 2).toLong(), false) {
                staggeredProgress = it.animatedValue as Float
                invalidate()
            }
        }
    }

    private fun setupAnimator(from: Any, to: Any, duration: Long, reverse: Boolean = false, update: (ValueAnimator) -> Unit): ValueAnimator {
        val animator = if (from is Int) ValueAnimator.ofInt(from, to as Int) else ValueAnimator.ofFloat(from as Float, to as Float)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = if (reverse) ValueAnimator.REVERSE else ValueAnimator.RESTART
        animator.interpolator = if (reverse) AccelerateDecelerateInterpolator() else LinearInterpolator()
        animator.addUpdateListener(update)
        animator.start()
        return animator
    }

    fun stopShimmerAnimation() {
        startAnimationPreDrawListener?.let { viewTreeObserver.removeOnPreDrawListener(it) }
        resetAnimators()
        isAnimationStarted = false
    }

    private fun resetAnimators() {
        waveAnimator?.cancel(); pulseAnimator?.cancel(); sparkleAnimator?.cancel()
        breathingAnimator?.cancel(); fadePulseAnimator?.cancel(); staggeredAnimator?.cancel()
        sparkles.clear(); ripples.clear()
        shimmerPaint = null
        releaseBitmaps()
    }

    private fun releaseBitmaps() {
        canvasForShimmerMask = null
        maskBitmap?.recycle()
        maskBitmap = null
    }

    private fun getOrCreateMaskBitmap(): Bitmap? {
        if (maskBitmap == null && maskRect != null) {
            maskBitmap = try {
                Bitmap.createBitmap(maskRect!!.width(), height, Bitmap.Config.ALPHA_8)
            } catch (e: Exception) { null }
        }
        return maskBitmap
    }

    private fun calculateBitmapMaskRect(): Rect {
        val angleRad = Math.toRadians(abs(shimmerAngle).toDouble())
        val shimmerLineBottomWidth = (width / 2 * maskWidth) / cos(angleRad)
        val shimmerLineRemainingTopWidth = height * tan(angleRad)
        return Rect(0, 0, (shimmerLineBottomWidth + shimmerLineRemainingTopWidth).toInt(), height)
    }

    // Public setters
    fun setShimmerEnabled(enabled: Boolean) {
        isShimmerEnabled = enabled
        if (!enabled) stopShimmerAnimation() else if (autoStart) startShimmerAnimation()
    }
    fun setEnableWave(e: Boolean) { enableWave = e }
    fun setEnablePulse(e: Boolean) { enablePulse = e }
    fun setEnableSparkles(e: Boolean) { enableSparkles = e }
    fun setEnableRipples(e: Boolean) { enableRippleOnTouch = e }
    fun setEnableBreathing(e: Boolean) { enableBreathing = e }
    fun setEnableFadePulse(e: Boolean) { enableFadePulse = e }
    fun setEnableStaggeredFade(e: Boolean) { enableStaggeredFade = e }
    fun setShimmerColor(c: Int) { shimmerColor = c; shimmerPaint = null }
    fun setPulseColor(c: Int) { pulseColor = c }
    fun setBreathingIntensity(i: Float) { breathingIntensity = i }
    fun setBreathingDuration(d: Int) { breathingDuration = d }
    fun setEnableAutoStart(e: Boolean) { autoStart = e }
    fun setAnimationReversed(r: Boolean) { isAnimationReversed = r }
    fun setShimmerAngle(a: Int) { shimmerAngle = a; maskRect = null; shimmerPaint = null }
    fun setShimmerAnimationDuration(d: Int) { shimmerAnimationDuration = d }
    fun setSparkleColor(c: Int) { sparkleColor = c }
    fun setRippleColor(c: Int) { rippleColor = c }
    fun setSparkleSize(s: Float) { sparkleSize = s }
    fun setSparkleFrequency(f: Float) { sparkleFrequency = f }
    fun setMaxSparkles(m: Int) { maxSparkles = m }
    fun setRippleStrokeWidth(w: Float) { rippleStrokeWidth = w; ripplePaint.strokeWidth = w }
    fun setMaxRipples(m: Int) { maxRipples = m }
    fun setMaskWidth(w: Float) { maskWidth = w; maskRect = null; shimmerPaint = null }
    fun setGradientCenterColorWidth(w: Float) { gradientCenterColorWidth = w; shimmerPaint = null }
    fun setWaveCount(c: Int) { waveCount = c }
    fun setAnimationType(type: AnimationType) { primaryAnimationType = type }

    override fun onDetachedFromWindow() {
        resetAnimators()
        super.onDetachedFromWindow()
    }

    fun configure(
        shimmerEnabled: Boolean = true,
        wave: Boolean = true,
        pulse: Boolean = false,
        sparkles: Boolean = false,
        ripples: Boolean = false,
        breathing: Boolean = false,
        fadePulse: Boolean = false,
        staggeredFade: Boolean = false,
        waves: Int = 1,
        shimmerColor: Int = "#66FFFFFF".toColorInt(),
        shimmerAngle: Int = 20,
        shimmerDuration: Int = 1500,
        shimmerMaskWidth: Float = 0.5f,
        isAnimationReversed: Boolean = false,
        gradientCenterColorWidth: Float = 0.1f,
        sparkleColor: Int = Color.WHITE,
        sparkleSize: Float = 8f,
        sparkleFrequency: Float = 0.3f,
        maxSparkles: Int = 15,
        rippleColor: Int = "#44FFFFFF".toColorInt(),
        rippleStrokeWidth: Float = 4f,
        maxRipples: Int = 13,
        pulseColor: Int = "#66FFFFFF".toColorInt(),
        breathingIntensity: Float = 1.03f,
        breathingDuration: Int = 2000,
        enableAutoStart: Boolean = true,
        enableScroll: Boolean = true
    ) {
        this.enableWave = wave
        this.enablePulse = pulse
        this.enableSparkles = sparkles
        this.enableBreathing = breathing
        this.enableFadePulse = fadePulse
        this.enableStaggeredFade = staggeredFade
        this.waveCount = waves
        this.shimmerColor = shimmerColor
        this.pulseColor = pulseColor
        this.breathingIntensity = breathingIntensity
        this.breathingDuration = breathingDuration
        this.shimmerAnimationDuration = shimmerDuration
        this.shimmerAngle = shimmerAngle
        this.maskWidth = shimmerMaskWidth
        this.isAnimationReversed = isAnimationReversed
        this.sparkleColor = sparkleColor
        this.sparkleSize = sparkleSize
        this.sparkleFrequency = sparkleFrequency
        this.maxSparkles = maxSparkles
        this.rippleColor = rippleColor
        this.rippleStrokeWidth = rippleStrokeWidth
        this.maxRipples = maxRipples
        this.gradientCenterColorWidth = gradientCenterColorWidth
        this.autoStart = enableAutoStart
        this.isShimmerEnabled = shimmerEnabled
        
        if (isAnimationStarted) {
            stopShimmerAnimation()
            startShimmerAnimation()
        }
    }
}
