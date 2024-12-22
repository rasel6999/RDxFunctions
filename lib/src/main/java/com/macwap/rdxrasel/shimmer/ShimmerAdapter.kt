package com.macwap.rdxrasel.shimmer

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ShimmerAdapter : RecyclerView.Adapter<ShimmerViewHolder>() {

    private var mItemCount: Int = 0
    private var mLayoutReference: Int = 0
    private var mShimmerAngle: Int = 0
    private var mShimmerColor: Int = 0
    private var mShimmerDuration: Int = 0
    private var mShimmerMaskWidth: Float = 0.toFloat()
    private var isAnimationReversed: Boolean = false
    private var mShimmerItemBackground: Drawable? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val shimmerViewHolder = ShimmerViewHolder(inflater, parent, mLayoutReference)
        shimmerViewHolder.setShimmerColor(mShimmerColor)
        shimmerViewHolder.setShimmerAngle(mShimmerAngle)
        shimmerViewHolder.setShimmerMaskWidth(mShimmerMaskWidth)
        shimmerViewHolder.setShimmerViewHolderBackground(mShimmerItemBackground)
        shimmerViewHolder.setShimmerAnimationDuration(mShimmerDuration)
        shimmerViewHolder.setAnimationReversed(isAnimationReversed)

        return shimmerViewHolder
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = mItemCount


    fun setMinItemCount(itemCount: Int) {
        mItemCount = itemCount
    }

    fun setShimmerAngle(shimmerAngle: Int) {
        this.mShimmerAngle = shimmerAngle
    }

    fun setShimmerColor(shimmerColor: Int) {
        this.mShimmerColor = shimmerColor
    }

    fun setShimmerMaskWidth(maskWidth: Float) {
        this.mShimmerMaskWidth = maskWidth
    }

    fun setShimmerItemBackground(shimmerItemBackground: Drawable) {
        this.mShimmerItemBackground = shimmerItemBackground
    }

    fun setShimmerDuration(mShimmerDuration: Int) {
        this.mShimmerDuration = mShimmerDuration
    }

    fun setLayoutReference(layoutReference: Int) {
        this.mLayoutReference = layoutReference
    }

    fun setAnimationReversed(animationReversed: Boolean) {
        this.isAnimationReversed = animationReversed
    }
}
