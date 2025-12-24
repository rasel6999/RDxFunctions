@file:RequiresApi(Build.VERSION_CODES.HONEYCOMB)

package com.macwap.rdxrasel.shimmer

import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

class ShimmerViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    innerViewResId: Int
) : RecyclerView.ViewHolder(inflater.inflate(innerViewResId, parent, false)) {

    private val mShimmerLayout: EnhancedShimmerLayout? = itemView as? EnhancedShimmerLayout
    private var isEnableStaggeredFade: Boolean = false

    init {
        // Initialize the animator for the fade-in/fade-out effect
/*        animator = ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0.3f, 1f).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            duration = 2500
        }*/
    }
    // Public setters for all features
    fun setShimmerEnabled(enabled: Boolean) { mShimmerLayout?.setShimmerEnabled(enabled) }
    fun setAnimationType(type: EnhancedShimmerLayout.AnimationType) { mShimmerLayout?.setAnimationType(type) }
    fun setEnableWave(enable: Boolean) { mShimmerLayout?.setEnableWave(enable) }
    fun setEnablePulse(enable: Boolean) { mShimmerLayout?.setEnablePulse(enable) }
    fun setEnableSparkles(enable: Boolean) { mShimmerLayout?.setEnableSparkles(enable) }
    fun setEnableRipples(enable: Boolean) { mShimmerLayout?.setEnableRipples(enable) }
    fun setEnableBreathing(enable: Boolean) { mShimmerLayout?.setEnableBreathing(enable) }
    fun setEnableFadePulse(enable: Boolean) { mShimmerLayout?.setEnableFadePulse(enable) }
    fun setEnableStaggeredFade(enable: Boolean) {
        isEnableStaggeredFade = enable
        mShimmerLayout?.setEnableStaggeredFade(enable)
    }
    fun setWaveCount(count: Int) { mShimmerLayout?.setWaveCount(count) }
    fun setSparkleColor(color: Int) { mShimmerLayout?.setSparkleColor(color) }
    fun setRippleColor(color: Int) { mShimmerLayout?.setRippleColor(color) }
    fun setPulseColor(color: Int) { mShimmerLayout?.setPulseColor(color) }
    fun setSparkleSize(size: Float) { mShimmerLayout?.setSparkleSize(size) }
    fun setSparkleFrequency(frequency: Float) { mShimmerLayout?.setSparkleFrequency(frequency) }
    fun setMaxSparkles(max: Int) { mShimmerLayout?.setMaxSparkles(max) }
    fun setRippleStrokeWidth(width: Float) { mShimmerLayout?.setRippleStrokeWidth(width) }
    fun setMaxRipples(max: Int) { mShimmerLayout?.setMaxRipples(max) }
    fun setBreathingIntensity(intensity: Float) { mShimmerLayout?.setBreathingIntensity(intensity) }
    fun setBreathingDuration(duration: Int) { mShimmerLayout?.setBreathingDuration(duration) }
    fun setGradientCenterColorWidth(width: Float) { mShimmerLayout?.setGradientCenterColorWidth(width) }
    fun setEnableAutoStart(enable: Boolean) { mShimmerLayout?.setEnableAutoStart(enable) }

    // Legacy API
    fun setShimmerAngle(angle: Int) { mShimmerLayout?.setShimmerAngle(angle) }
    fun setShimmerColor(color: Int) { mShimmerLayout?.setShimmerColor(color) }
    fun setShimmerMaskWidth(maskWidth: Float) { mShimmerLayout?.setMaskWidth(maskWidth) }
    fun setShimmerAnimationDuration(duration: Int) { mShimmerLayout?.setShimmerAnimationDuration(duration) }
    fun setAnimationReversed(animationReversed: Boolean) { mShimmerLayout?.setAnimationReversed(animationReversed) }

    fun setShimmerViewHolderBackground(viewHolderBackground: Drawable?) {
        if (viewHolderBackground != null) {
            setBackground(viewHolderBackground)
        }
    }

    fun bind() {
        mShimmerLayout?.startShimmerAnimation()
        if(isEnableStaggeredFade) {
            animator?.start()
        }

    }


    private fun setBackground(background: Drawable) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            mShimmerLayout?.background = background
        } else {
            @Suppress("DEPRECATION")
            mShimmerLayout?.setBackgroundDrawable(background)
        }
    }

    fun applyPreset(preset: ShimmerPreset) {
        when (preset) {
            ShimmerPreset.ULTRA_SMOOTH -> {
                setEnableWave(true)
                setEnablePulse(true)
                setEnableSparkles(true)
                setEnableRipples(true)
                setEnableBreathing(true)
                setWaveCount(3)
            }
            ShimmerPreset.MINIMAL -> {
                setEnableWave(true)
                setEnablePulse(false)
                setEnableSparkles(false)
                setEnableRipples(false)
                setEnableBreathing(false)
                setWaveCount(1)
            }
            ShimmerPreset.SPARKLY -> {
                setEnableWave(false)
                setEnablePulse(true)
                setEnableSparkles(true)
                setEnableRipples(false)
                setEnableBreathing(true)
                setWaveCount(1)
            }
            ShimmerPreset.INTERACTIVE -> {
                setEnableWave(true)
                setEnablePulse(true)
                setEnableSparkles(true)
                setEnableRipples(true)
                setEnableBreathing(false)
                setWaveCount(2)
            }
            ShimmerPreset.WAVES -> {
                setEnableWave(true)
                setEnablePulse(false)
                setEnableSparkles(false)
                setEnableRipples(false)
                setEnableBreathing(true)
                setWaveCount(5)
            }
            ShimmerPreset.STAGGERED -> {
                setEnableWave(false)
                setEnablePulse(false)
                setEnableStaggeredFade(true)
                setEnableSparkles(false)
                setEnableRipples(false)
                setEnableBreathing(false)
            }
        }
    }

    enum class ShimmerPreset {
        ULTRA_SMOOTH,
        MINIMAL,
        SPARKLY,
        INTERACTIVE,
        WAVES,
        STAGGERED
    }
    companion object{
        public var animator: ObjectAnimator? = null

        fun stopAnimation() {
            animator?.cancel()
        }

    }
}