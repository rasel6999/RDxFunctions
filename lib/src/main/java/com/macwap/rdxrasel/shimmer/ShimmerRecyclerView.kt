package com.macwap.rdxrasel.shimmer

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macwap.rdxrasel.R
import com.macwap.rdxrasel.untils.FunctionManager.alert


class ShimmerRecyclerView : RecyclerView {

    var actualAdapter: Adapter<*>? = null
        private set
    private var mShimmerAdapter: ShimmerAdapter? = null

    private var mShimmerLayoutManager: LayoutManager? = null
    private var mActualLayoutManager: LayoutManager? = null
    private var mLayoutMangerType: LayoutMangerType? = null

    private var mCanScroll: Boolean = false
    private var mReverseLayout: Boolean = false
    var layoutReference: Int = 0
        private set
    private var mGridCount: Int = 0

    @Suppress("unused")
    val shimmerAdapter: Adapter<*>?
        get() = mShimmerAdapter

    enum class LayoutMangerType {
        LINEAR_VERTICAL, LINEAR_HORIZONTAL, GRID
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mShimmerAdapter = ShimmerAdapter()
        val a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerRecyclerView, 0, 0)

        val mShimmerAngle: Int
        val mShimmerColor: Int
        val mShimmerDuration: Int
        val mShimmerMaskWidth: Float
        val isAnimationReversed: Boolean
        val mShimmerItemBackground: Drawable?

        try {
            setDemoLayoutReference(
                a.getResourceId(
                    R.styleable.ShimmerRecyclerView_shimmer_demo_layout,
                    R.layout.layout_sample_view
                )
            )
            setDemoChildCount(
                a.getInteger(
                    R.styleable.ShimmerRecyclerView_shimmer_demo_child_count,
                    10
                )
            )
            setGridChildCount(
                a.getInteger(
                    R.styleable.ShimmerRecyclerView_shimmer_demo_grid_child_count,
                    2
                )
            )

            val value = a.getInteger(
                R.styleable.ShimmerRecyclerView_shimmer_demo_layout_manager_type,
                0
            )
            when (value) {
                0 -> setDemoLayoutManager(LayoutMangerType.LINEAR_VERTICAL)
                1 -> setDemoLayoutManager(LayoutMangerType.LINEAR_HORIZONTAL)
                2 -> setDemoLayoutManager(LayoutMangerType.GRID)
                else -> throw IllegalArgumentException("This value for layout manager is not valid!")
            }

            mShimmerAngle = a.getInteger(R.styleable.ShimmerRecyclerView_shimmer_demo_angle, 0)
            mShimmerColor = a.getColor(
                R.styleable.ShimmerRecyclerView_shimmer_demo_shimmer_color,
                getColor(R.color.default_shimmer_color)
            )
            mShimmerItemBackground = a.getDrawable(
                R.styleable.ShimmerRecyclerView_shimmer_demo_view_holder_item_background
            )
            mShimmerDuration = a.getInteger(
                R.styleable.ShimmerRecyclerView_shimmer_demo_duration,
                1500
            )
            mShimmerMaskWidth = a.getFloat(
                R.styleable.ShimmerRecyclerView_shimmer_demo_mask_width,
                0.5f
            )
            isAnimationReversed = a.getBoolean(
                R.styleable.ShimmerRecyclerView_shimmer_demo_reverse_animation,
                false
            )
            mReverseLayout = a.getBoolean(R.styleable.ShimmerRecyclerView_shimmer_demo_reverse_layout, false)
        } finally {
            a.recycle()
        }

        // Apply legacy settings
        mShimmerAdapter!!.setShimmerAngle(mShimmerAngle)
        mShimmerAdapter!!.setShimmerColor(mShimmerColor)
        mShimmerAdapter!!.setShimmerMaskWidth(mShimmerMaskWidth)
        if (mShimmerItemBackground != null)
            mShimmerAdapter!!.setShimmerItemBackground(mShimmerItemBackground)
        mShimmerAdapter!!.setShimmerDuration(mShimmerDuration)
        mShimmerAdapter!!.setAnimationReversed(isAnimationReversed)

        showShimmerAdapter()
    }

    // ========== LEGACY API (Backward Compatible) ==========

    fun setGridChildCount(count: Int) {
        mGridCount = count
    }

    fun setDemoLayoutManager(type: LayoutMangerType) {
        mLayoutMangerType = type
    }

    fun setDemoChildCount(count: Int) {
        mShimmerAdapter!!.setMinItemCount(count)
    }

    fun setDemoShimmerDuration(duration: Int) {
        mShimmerAdapter!!.setShimmerDuration(duration)
    }

    fun setDemoShimmerMaskWidth(maskWidth: Float) {
        mShimmerAdapter!!.setShimmerMaskWidth(maskWidth)
    }

    fun setDemoReverseLayout(reverse: Boolean) {
        mReverseLayout = reverse
        mShimmerLayoutManager = null // Force re-init
    }

    fun showShimmerAdapter() {
        if (mShimmerLayoutManager == null) {
            initShimmerManager()
        }

        layoutManager = mShimmerLayoutManager
        adapter = mShimmerAdapter
    }

    fun hideShimmerAdapter() {
        mCanScroll = true
        layoutManager = mActualLayoutManager
        adapter = actualAdapter
        mShimmerAdapter?.shimmerEnabled = false
    }

    fun enableScroll(enable: Boolean) {
        mCanScroll = enable
    }

    @Suppress("DEPRECATION")
    override fun setLayoutManager(manager: LayoutManager?) {
        if (manager == null) {
            mActualLayoutManager = null
        } else if (manager !== mShimmerLayoutManager) {
            mActualLayoutManager = manager
        }

        super.setLayoutManager(manager)
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (adapter == null) {
            actualAdapter = null
        } else if (adapter !== mShimmerAdapter) {
            actualAdapter = adapter
        }

        super.setAdapter(adapter)
    }

    fun setDemoLayoutReference(mLayoutReference: Int) {
        this.layoutReference = mLayoutReference
        mShimmerAdapter!!.setLayoutReference(layoutReference)
    }

    private fun initShimmerManager() {
        when (mLayoutMangerType) {
            LayoutMangerType.LINEAR_VERTICAL -> mShimmerLayoutManager =
                object : LinearLayoutManager(context, VERTICAL, mReverseLayout) {
                    override fun canScrollVertically(): Boolean {
                        return mCanScroll
                    }
                }

            LayoutMangerType.LINEAR_HORIZONTAL -> mShimmerLayoutManager =
                object : LinearLayoutManager(
                    context,
                    HORIZONTAL,
                    mReverseLayout
                ) {
                    override fun canScrollHorizontally(): Boolean {
                        return mCanScroll
                    }
                }

            LayoutMangerType.GRID -> mShimmerLayoutManager =
                object : GridLayoutManager(context, mGridCount) {
                    override fun canScrollVertically(): Boolean {
                        return mCanScroll
                    }
                }

            else -> {}
        }
    }

    fun setDemoLayout(layoutResId: Int) {
        setDemoLayoutReference(layoutResId)
    }

    fun setDemoAngle(angle: Int) {
        mShimmerAdapter?.setShimmerAngle(angle)
    }

    fun setShimmerColor(color: Int) {
        mShimmerAdapter?.setShimmerColor(color)
    }

    private fun getColor(id: Int) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getColor(id)
        } else {
            @Suppress("DEPRECATION")
            resources.getColor(id)
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
        reverseLayout: Boolean = false
    ) {
        mReverseLayout = reverseLayout
        mShimmerLayoutManager = null // Reset manager to apply reverseLayout
        
        mShimmerAdapter?.apply {
            this.shimmerEnabled = shimmerEnabled
            this.enableWave = wave
            this.enablePulse = pulse
            this.enableSparkles = sparkles
            this.enableRipples = ripples
            this.enableBreathing = breathing
            this.enableFadePulse = fadePulse
            this.enableStaggeredFade = staggeredFade
            this.waveCount = waves
            this.mShimmerColor = shimmerColor
            this.sparkleColor = sparkleColor
            this.rippleColor = rippleColor
            this.mShimmerAngle = shimmerAngle
            this.mShimmerDuration = shimmerDuration
            this.mShimmerMaskWidth = shimmerMaskWidth
            this.mIsAnimationReversed = isAnimationReversed
            this.sparkleSize = sparkleSize
            this.sparkleFrequency = sparkleFrequency
            this.maxSparkles = maxSparkles
            this.rippleStrokeWidth = rippleStrokeWidth
            this.maxRipples = maxRipples
            this.pulseColor = pulseColor
            this.breathingIntensity = breathingIntensity
            this.breathingDuration = breathingDuration
            this.gradientCenterColorWidth = gradientCenterColorWidth
            this.enableAutoStart = enableAutoStart
            this.shimmerPreset = null
        }
        mCanScroll = enableScroll
    }


}

// Helper
fun ShimmerRecyclerView.getAttrColor(attrRes: Int): Int {
    val typedValue = android.util.TypedValue()
    context.theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue.data
}
