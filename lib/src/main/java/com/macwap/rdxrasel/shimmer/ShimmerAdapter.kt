@file:SuppressLint("NewApi")
package com.macwap.rdxrasel.shimmer

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView

class ShimmerAdapter : RecyclerView.Adapter<ShimmerViewHolder>() {

    private var mItemCount: Int = 10
    private var mLayoutReference: Int = 0

    // Legacy properties
    var mShimmerAngle: Int = 20
    var mShimmerColor: Int = "#66FFFFFF".toColorInt()
    var mShimmerDuration: Int = 1500
    var mShimmerMaskWidth: Float = 0.5f
    var mIsAnimationReversed: Boolean = false  // Changed name to avoid clash
    private var mShimmerItemBackground: Drawable? = null

    // Enhanced properties
    var shimmerEnabled = true
    var animationType = EnhancedShimmerLayout.AnimationType.SHIMMER
    var enableWave = true
    var enablePulse = false
    var enableSparkles = false
    var enableRipples = false
    var enableBreathing = false
    var enableFadePulse = false
    var enableStaggeredFade = false
    var waveCount = 1
    var sparkleColor = Color.WHITE
    var rippleColor = "#44FFFFFF".toColorInt()

    var pulseColor = "#44FFFFFF".toColorInt()

    var shimmerPreset: ShimmerViewHolder.ShimmerPreset? = null

    // Extended enhanced properties
    var sparkleSize = 8f
    var sparkleFrequency = 0.3f
    var maxSparkles = 15
    var rippleStrokeWidth = 4f
    var maxRipples = 3
    var breathingIntensity = 1.03f
    var breathingDuration = 2000
    var gradientCenterColorWidth = 0.1f
    var enableAutoStart = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val shimmerViewHolder = ShimmerViewHolder(inflater, parent, mLayoutReference)

        // Apply preset if set
        shimmerPreset?.let {
            shimmerViewHolder.applyPreset(it)
            return shimmerViewHolder
        }

        // Apply configuration
        shimmerViewHolder.setShimmerEnabled(shimmerEnabled)
        shimmerViewHolder.setAnimationType(animationType)
        shimmerViewHolder.setEnableWave(enableWave)
        shimmerViewHolder.setEnablePulse(enablePulse)
        shimmerViewHolder.setEnableSparkles(enableSparkles)
        shimmerViewHolder.setEnableRipples(enableRipples)
        shimmerViewHolder.setEnableBreathing(enableBreathing)
        shimmerViewHolder.setEnableFadePulse(enableFadePulse)
        shimmerViewHolder.setEnableStaggeredFade(enableStaggeredFade)
        shimmerViewHolder.setWaveCount(waveCount)
        shimmerViewHolder.setSparkleColor(sparkleColor)
        shimmerViewHolder.setRippleColor(rippleColor)
        shimmerViewHolder.setSparkleSize(sparkleSize)
        shimmerViewHolder.setSparkleFrequency(sparkleFrequency)
        shimmerViewHolder.setMaxSparkles(maxSparkles)
        shimmerViewHolder.setRippleStrokeWidth(rippleStrokeWidth)
        shimmerViewHolder.setMaxRipples(maxRipples)
        shimmerViewHolder.setPulseColor(pulseColor)
        shimmerViewHolder.setBreathingIntensity(breathingIntensity)
        shimmerViewHolder.setBreathingDuration(breathingDuration)
        shimmerViewHolder.setGradientCenterColorWidth(gradientCenterColorWidth)
        shimmerViewHolder.setEnableAutoStart(enableAutoStart)

        // Legacy settings
        shimmerViewHolder.setShimmerColor(mShimmerColor)
        shimmerViewHolder.setShimmerAngle(mShimmerAngle)
        shimmerViewHolder.setShimmerMaskWidth(mShimmerMaskWidth)
        shimmerViewHolder.setShimmerViewHolderBackground(mShimmerItemBackground)
        shimmerViewHolder.setShimmerAnimationDuration(mShimmerDuration)
        shimmerViewHolder.setAnimationReversed(mIsAnimationReversed)  // Use new name

        return shimmerViewHolder
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = mItemCount

    // Legacy API
    fun setMinItemCount(itemCount: Int) { mItemCount = itemCount }
    fun setShimmerAngle(shimmerAngle: Int) { this.mShimmerAngle = shimmerAngle }
    fun setShimmerColor(shimmerColor: Int) { this.mShimmerColor = shimmerColor }
    fun setShimmerMaskWidth(maskWidth: Float) { this.mShimmerMaskWidth = maskWidth }
    fun setShimmerItemBackground(shimmerItemBackground: Drawable) { this.mShimmerItemBackground = shimmerItemBackground }
    fun setShimmerDuration(shimmerDuration: Int) { this.mShimmerDuration = shimmerDuration }
    fun setLayoutReference(layoutReference: Int) { this.mLayoutReference = layoutReference }
    fun setAnimationReversed(reversed: Boolean) { this.mIsAnimationReversed = reversed }  // Use new name
    fun applyPreset(preset: ShimmerViewHolder.ShimmerPreset) { this.shimmerPreset = preset }
}
