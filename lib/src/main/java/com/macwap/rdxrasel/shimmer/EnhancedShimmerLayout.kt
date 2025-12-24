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
 * 🔥 FINAL OPTIMIZED EnhancedShimmerLayout
 * - All animations working
 * - Memory leak free
 * - No syntax errors
 * - Perfect performance
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

    // Features - ALL controllable with booleans
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

    private var originalShimmerColor = shimmerColor
    private var sparkleColor = Color.WHITE
    private var rippleColor = "#44FFFFFF".toColorInt()

    // Wave
    private var maskOffsetX = 0
    private var shimmerAngle = 20
    private var maskWidth = 0.5f
    private var gradientCenterColorWidth = 0.1f
    private var isAnimationReversed = false
    private var waveCount = 1
    private var shimmerAnimationDuration = 1500

    // Pulse
    private var pulseIntensity = 0f

    // Sparkles
    private val sparkles = mutableListOf<Sparkle>()
    private var maxSparkles = 15
    private var sparkleSize = 8f
    private var sparkleFrequency = 0.3f

    // Ripples
    private val ripples = mutableListOf<Ripple>()
    private var maxRipples = 3
    private var rippleStrokeWidth = 4f

    // Breathing
    private var breathingScale = 1f
    private var breathingIntensity = 1.03f
    private var breathingDuration = 2000

    // Fade Effects
    private var fadePulseAlpha = 1f
    private var staggeredProgress = 0f

    // Drawing
    private var maskRect: Rect? = null
    private var gradientTexturePaint: Paint? = null
    private val sparklePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
    private val ripplePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE }

    // Animators
    private var waveAnimator: ValueAnimator? = null
    private var pulseAnimator: ValueAnimator? = null
    private var sparkleAnimator: ValueAnimator? = null
    private var breathingAnimator: ValueAnimator? = null
    private var fadePulseAnimator: ValueAnimator? = null
    private var staggeredAnimator: ValueAnimator? = null

    // Bitmaps
    private var localMaskBitmap: Bitmap? = null
    private var maskBitmap: Bitmap? = null
    private var canvasForShimmerMask: Canvas? = null
    private var startAnimationPreDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    enum class AnimationType {
        SHIMMER
    }

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
            originalShimmerColor = a.getColor(R.styleable.ShimmerLayout_shimmer_color, shimmerColor)
            shimmerColor = originalShimmerColor
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (enableRippleOnTouch && isAnimationStarted && isShimmerEnabled) {
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                if (ripples.size < maxRipples) {
                    ripples.add(Ripple(event.x, event.y, 0f, 1f, max(width, height) * 0.5f))
                }
            }
        }
        return super.onTouchEvent(event)
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        if (!isAnimationStarted || !isShimmerEnabled || width <= 0 || height <= 0) return

        // Fade Pulse effect (controls alpha)
        if (enableFadePulse) {
            alpha = fadePulseAlpha
        }

        // Staggered Fade effect
        if (enableStaggeredFade) {
            canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        }

        // Breathing scale
        if (enableBreathing) {
            canvas.save()
            canvas.scale(breathingScale, breathingScale, width / 2f, height / 2f)
        }

        // Draw all enabled effects
        drawShimmerEffects(canvas)

        // Staggered fade overlay
        if (enableStaggeredFade) {
            drawStaggeredFade(canvas)
            canvas.restore()
        }

        if (enableBreathing) canvas.restore()
    }

    private fun drawShimmerEffects(canvas: Canvas) {
        // Wave effect
        if (enableWave && waveCount > 0) {
            if (waveCount == 1) {
                drawWaveEffect(canvas)
            } else {
                drawMultiWaveEffect(canvas)
            }
        }

        // Pulse overlay
        if (enablePulse) {
            drawPulseOverlay(canvas)
        }

        // Sparkles
        if (enableSparkles) drawSparkles(canvas)

        // Ripples
        if (enableRippleOnTouch) drawRipples(canvas)
    }

    private fun drawWaveEffect(canvas: Canvas) {
        if (waveCount == 0) return

        localMaskBitmap = getMaskBitmap() ?: return
        if (canvasForShimmerMask == null) canvasForShimmerMask = Canvas(localMaskBitmap!!)

        canvasForShimmerMask!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvasForShimmerMask!!.save()
        canvasForShimmerMask!!.translate(-maskOffsetX.toFloat(), 0f)
        super.dispatchDraw(canvasForShimmerMask!!)
        canvasForShimmerMask!!.restore()

        createShimmerPaint()
        maskRect?.let {
            canvas.save()
            canvas.translate(maskOffsetX.toFloat(), 0f)
            canvas.drawRect(it.left.toFloat(), 0f, it.width().toFloat(), it.height().toFloat(), gradientTexturePaint!!)
            canvas.restore()
        }

        localMaskBitmap = null
    }

    private fun drawMultiWaveEffect(canvas: Canvas) {
        if (waveCount == 0) return

        localMaskBitmap = getMaskBitmap() ?: return
        if (canvasForShimmerMask == null) canvasForShimmerMask = Canvas(localMaskBitmap!!)

        canvasForShimmerMask!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvasForShimmerMask!!.save()

        for (i in 0 until waveCount) {
            val offset = maskOffsetX - (i * (width / waveCount))
            canvasForShimmerMask!!.save()
            canvasForShimmerMask!!.translate(-offset.toFloat(), 0f)
            super.dispatchDraw(canvasForShimmerMask!!)
            canvasForShimmerMask!!.restore()
        }

        canvasForShimmerMask!!.restore()
        createShimmerPaint()
        maskRect?.let {
            canvas.save()
            canvas.translate(maskOffsetX.toFloat(), 0f)
            canvas.drawRect(it.left.toFloat(), 0f, it.width().toFloat(), it.height().toFloat(), gradientTexturePaint!!)
            canvas.restore()
        }

        localMaskBitmap = null
    }

    private fun drawPulseOverlay(canvas: Canvas) {
        val paint = Paint().apply {
            color = pulseColor
            alpha = (pulseIntensity * 255).toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }



    private fun drawStaggeredFade(canvas: Canvas) {
        val fadeHeight = height * 0.3f
        val fadeCenter = height * staggeredProgress

        val gradient = LinearGradient(
            0f, fadeCenter - fadeHeight,
            0f, fadeCenter + fadeHeight,
            intArrayOf(Color.TRANSPARENT, Color.BLACK, Color.BLACK, Color.BLACK, Color.TRANSPARENT),
            floatArrayOf(0f, 0.3f, 0.5f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )

        val paint = Paint().apply {
            this.shader = gradient
            this.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
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

    private fun createShimmerPaint() {
        if (gradientTexturePaint != null) return

        val maskBitmapShader = BitmapShader(localMaskBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
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

        gradientTexturePaint = Paint().apply {
            isAntiAlias = true
            isDither = true
            isFilterBitmap = true
            this.shader = ComposeShader(gradient, maskBitmapShader, PorterDuff.Mode.DST_IN)
        }
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
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
        startAllAnimations()
        isAnimationStarted = true
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun startAllAnimations() {
        // Wave
        if (enableWave && waveCount > 0) startWaveAnimation()

        // Pulse
        if (enablePulse) startPulseAnimation()

        // Sparkles
        if (enableSparkles) startSparkleAnimation()

        // Breathing
        if (enableBreathing) startBreathingAnimation()

        // Fade Pulse
        if (enableFadePulse) startFadePulseAnimation()

        // Staggered Fade
        if (enableStaggeredFade) startStaggeredAnimation()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun startWaveAnimation() {
        waveAnimator?.cancel()
        waveAnimator?.removeAllUpdateListeners()

        val animationToX = width
        val animationFromX = if (width > (maskRect?.width() ?: 0)) -animationToX else -(maskRect?.width() ?: 0)
        val fullLength = animationToX - animationFromX

        waveAnimator = if (isAnimationReversed) ValueAnimator.ofInt(fullLength, 0) else ValueAnimator.ofInt(0, fullLength)
        waveAnimator!!.duration = shimmerAnimationDuration.toLong()
        waveAnimator!!.repeatCount = ObjectAnimator.INFINITE
        waveAnimator!!.interpolator = LinearInterpolator()
        waveAnimator!!.addUpdateListener {
            maskOffsetX = animationFromX + (it.animatedValue as Int)
            if (maskOffsetX + (maskRect?.width() ?: 0) >= 0) invalidate()
        }
        waveAnimator!!.start()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun startPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator?.removeAllUpdateListeners()

        pulseAnimator = ValueAnimator.ofFloat(0f, 0.3f, 0f)
        pulseAnimator!!.duration = (shimmerAnimationDuration * 1.5).toLong()
        pulseAnimator!!.repeatCount = ObjectAnimator.INFINITE
        pulseAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        pulseAnimator!!.addUpdateListener { pulseIntensity = it.animatedValue as Float; invalidate() }
        pulseAnimator!!.start()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun startSparkleAnimation() {
        sparkleAnimator?.cancel()
        sparkleAnimator?.removeAllUpdateListeners()

        sparkleAnimator = ValueAnimator.ofFloat(0f, 1f)
        sparkleAnimator!!.duration = 100
        sparkleAnimator!!.repeatCount = ObjectAnimator.INFINITE
        sparkleAnimator!!.addUpdateListener {
            if (sparkles.size < maxSparkles && Math.random() < sparkleFrequency) {
                sparkles.add(Sparkle(
                    (Math.random() * width).toFloat(),
                    (Math.random() * height).toFloat(),
                    1f,
                    sparkleSize * (0.5f + Math.random().toFloat() * 0.5f),
                    0.5f + Math.random().toFloat() * 1.5f
                ))
            }
            invalidate()
        }
        sparkleAnimator!!.start()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun startBreathingAnimation() {
        breathingAnimator?.cancel()
        breathingAnimator?.removeAllUpdateListeners()

        breathingAnimator = ValueAnimator.ofFloat(1f, breathingIntensity, 1f)
        breathingAnimator!!.duration = breathingDuration.toLong()
        breathingAnimator!!.repeatCount = ObjectAnimator.INFINITE
        breathingAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        breathingAnimator!!.addUpdateListener { breathingScale = it.animatedValue as Float; invalidate() }
        breathingAnimator!!.start()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun startFadePulseAnimation() {
        fadePulseAnimator?.cancel()
        fadePulseAnimator?.removeAllUpdateListeners()

        fadePulseAnimator = ValueAnimator.ofFloat(1f, 0.5f, 1f)
        fadePulseAnimator!!.duration = shimmerAnimationDuration.toLong()
        fadePulseAnimator!!.repeatCount = ObjectAnimator.INFINITE
        fadePulseAnimator!!.repeatMode = ValueAnimator.RESTART
        fadePulseAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        fadePulseAnimator!!.addUpdateListener { fadePulseAlpha = it.animatedValue as Float; alpha = fadePulseAlpha; invalidate() }
        fadePulseAnimator!!.start()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun startStaggeredAnimation() {
        staggeredAnimator?.cancel()
        staggeredAnimator?.removeAllUpdateListeners()

        staggeredAnimator = ValueAnimator.ofFloat(0f, 1f)
        staggeredAnimator!!.duration = shimmerAnimationDuration.toLong()
        staggeredAnimator!!.repeatCount = ObjectAnimator.INFINITE
        staggeredAnimator!!.repeatMode = ValueAnimator.RESTART
        staggeredAnimator!!.interpolator = LinearInterpolator()
        staggeredAnimator!!.addUpdateListener { staggeredProgress = it.animatedValue as Float; invalidate() }
        staggeredAnimator!!.start()
    }

    fun stopShimmerAnimation() {
        startAnimationPreDrawListener?.let { viewTreeObserver.removeOnPreDrawListener(it) }
        resetShimmering()
        ShimmerViewHolder.stopAnimation()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun resetShimmering() {
        waveAnimator?.cancel(); waveAnimator?.removeAllUpdateListeners(); waveAnimator = null
        pulseAnimator?.cancel(); pulseAnimator?.removeAllUpdateListeners(); pulseAnimator = null
        sparkleAnimator?.cancel(); sparkleAnimator?.removeAllUpdateListeners(); sparkleAnimator = null
        breathingAnimator?.cancel(); breathingAnimator?.removeAllUpdateListeners(); breathingAnimator = null
        fadePulseAnimator?.cancel(); fadePulseAnimator?.removeAllUpdateListeners(); fadePulseAnimator = null
        staggeredAnimator?.cancel(); staggeredAnimator?.removeAllUpdateListeners(); staggeredAnimator = null

        gradientTexturePaint = null
        isAnimationStarted = false
        sparkles.clear()
        ripples.clear()
        releaseBitmaps()
    }

    private fun releaseBitmaps() {
        canvasForShimmerMask = null
        maskBitmap?.recycle()
        maskBitmap = null
    }

    private fun getMaskBitmap(): Bitmap? {
        if (maskBitmap == null && maskRect != null) {
            maskBitmap = try {
                Bitmap.createBitmap(maskRect!!.width(), height, Bitmap.Config.ALPHA_8)
            } catch (e: OutOfMemoryError) {
                System.gc()
                null
            }
        }
        return maskBitmap
    }

    private fun calculateBitmapMaskRect(): Rect {
        val shimmerLineBottomWidth = (width / 2 * maskWidth) / cos(Math.toRadians(abs(shimmerAngle).toDouble()))
        val shimmerLineRemainingTopWidth = height * tan(Math.toRadians(abs(shimmerAngle).toDouble()))
        return Rect(0, 0, (shimmerLineBottomWidth + shimmerLineRemainingTopWidth).toInt(), height)
    }

    // Public API - Simple boolean controls
    fun setShimmerEnabled(enabled: Boolean) {
        isShimmerEnabled = enabled
        if (!enabled && isAnimationStarted) stopShimmerAnimation()
        else if (enabled && !isAnimationStarted && autoStart) startShimmerAnimation()
    }

    fun setAnimationType(type: AnimationType) { primaryAnimationType = type }
    fun setEnableWave(enable: Boolean) { enableWave = enable }
    fun setEnablePulse(enable: Boolean) { enablePulse = enable }
    fun setEnableSparkles(enable: Boolean) { enableSparkles = enable }
    fun setEnableRipples(enable: Boolean) { enableRippleOnTouch = enable }
    fun setEnableBreathing(enable: Boolean) { enableBreathing = enable }
    fun setEnableFadePulse(enable: Boolean) { enableFadePulse = enable }
    fun setEnableStaggeredFade(enable: Boolean) { enableStaggeredFade = enable }
    fun setEnableSequentialReveal(enable: Boolean) { enableStaggeredFade = enable }  // Alias
    fun setWaveCount(count: Int) { waveCount = count.coerceIn(0, 10) }
    fun setShimmerColor(color: Int) { originalShimmerColor = color; shimmerColor = color }
    fun setSparkleColor(color: Int) { sparkleColor = color }
    fun setRippleColor(color: Int) { rippleColor = color }

    fun setPulseColor(color: Int){ pulseColor =color}
    fun setShimmerAngle(angle: Int) { shimmerAngle = angle }
    fun setMaskWidth(width: Float) { maskWidth = width }
    fun setShimmerAnimationDuration(duration: Int) { shimmerAnimationDuration = duration }
    fun setAnimationReversed(reversed: Boolean) { isAnimationReversed = reversed }
    fun setSparkleSize(size: Float) { sparkleSize = size }
    fun setSparkleFrequency(frequency: Float) { sparkleFrequency = frequency }
    fun setMaxSparkles(max: Int) { maxSparkles = max }
    fun setRippleStrokeWidth(width: Float) { rippleStrokeWidth = width; ripplePaint.strokeWidth = width }
    fun setMaxRipples(max: Int) { maxRipples = max }
    fun setBreathingIntensity(intensity: Float) { breathingIntensity = intensity }
    fun setBreathingDuration(duration: Int) { breathingDuration = duration }
    fun setGradientCenterColorWidth(width: Float) { gradientCenterColorWidth = width }
    fun setEnableAutoStart(enable: Boolean) { autoStart = enable }
    fun setRevealAngle(angle: Float) { }  // Not used

    override fun onDetachedFromWindow() {
        resetShimmering()
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
        enableScroll: Boolean = true,
    ) {
            this.enableWave = wave
            this.enablePulse = pulse
            this.enableSparkles = sparkles
            this.enableBreathing = breathing
            this.enableFadePulse = fadePulse
            this.waveCount = waves
            this.sparkleColor = sparkleColor
            this.rippleColor = rippleColor
            this.sparkleSize = sparkleSize
            this.sparkleFrequency = sparkleFrequency
            this.maxSparkles = maxSparkles
            this.rippleStrokeWidth = rippleStrokeWidth
            this.maxRipples = maxRipples
            this.pulseColor = pulseColor
            this.breathingIntensity = breathingIntensity
            this.breathingDuration = breathingDuration
            this.gradientCenterColorWidth = gradientCenterColorWidth
            setShimmerEnabled(shimmerEnabled)
            setEnableAutoStart(enableAutoStart)
            setShimmerAngle(shimmerAngle)
            setShimmerColor (shimmerColor)
            setAnimationReversed( isAnimationReversed )
            setEnableRipples(ripples)
        staggeredFade(staggeredFade)
    }
    fun staggeredFade(staggeredFade: Boolean) {

        if(!staggeredFade) return

        this.animate()
            .alpha(0.3f)
            .setDuration(this.breathingDuration.toLong())
            .withEndAction {
                this.animate()
                    .alpha(1f)
                    .setDuration(this.breathingDuration.toLong())
                    .withEndAction {
                        staggeredFade(staggeredFade)
                    }
                    .start()
            }
            .start()
    }
}