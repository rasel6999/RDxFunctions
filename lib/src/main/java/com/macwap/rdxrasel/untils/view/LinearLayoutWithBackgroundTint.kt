package com.macwap.rdxrasel.untils.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class LinearLayoutWithBackgroundTint : LinearLayout {
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        TintHelper(context, this, attrs)
    }
}