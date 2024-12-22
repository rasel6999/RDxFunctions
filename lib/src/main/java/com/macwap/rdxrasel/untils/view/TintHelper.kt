@file:Suppress("DEPRECATION", "unused", "SpellCheckingInspection",
    "SENSELESS_COMPARISON", "MemberVisibilityCanBePrivate","UnusedConstructorParameter")

package com.macwap.rdxrasel.untils.view

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.macwap.rdxrasel.R

//this will change the background tint color for the layouts
class TintHelper(var context: Context, var viewGroup: ViewGroup, var attrs: AttributeSet?) {
    var color = -1

    //set background color
    private fun setBackgroundColor(viewGroup: ViewGroup, color: Int) {
        val thumbDrawable = viewGroup.background.mutate()
        thumbDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    init {
        //get the color from corresponding attribute file
        if (viewGroup is LinearLayout) {
            if (attrs != null) {
                val array = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.LinearLayoutWithBackgroundTint,
                    0,
                    0
                )
                if (array != null) {
                    color = array.getColor(
                        R.styleable.LinearLayoutWithBackgroundTint_linearBgTintColor,
                        -1
                    )
                    array.recycle()
                }
            }
        } else if (viewGroup is FrameLayout) {
            if (attrs != null) {
                val array = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.FrameLayoutWithBackgroundTint,
                    0,
                    0
                )
                if (array != null) {
                    color = array.getColor(
                        R.styleable.FrameLayoutWithBackgroundTint_frameBgTintColor,
                        -1
                    )
                    array.recycle()
                }
            }
        } else if (viewGroup is RelativeLayout) {
            if (attrs != null) {
                val array = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.RelativeLayoutWithBackgroundTint,
                    0,
                    0
                )
                if (array != null) {
                    color = array.getColor(
                        R.styleable.RelativeLayoutWithBackgroundTint_relativeBgTintColor,
                        -1
                    )
                    array.recycle()
                }
            }
        }
        setBackgroundColor(viewGroup, color)
    }
}