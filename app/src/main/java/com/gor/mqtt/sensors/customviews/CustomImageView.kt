package com.gor.mqtt.sensors.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatImageView
import com.gor.mqtt.sensors.R


class CustomImageView : AppCompatImageView {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            performAnimation(this)
        }
        if (event.action == MotionEvent.ACTION_UP) {
            // performClick();
        }
        //  return true;
        return super.onTouchEvent(event)
    }

    private fun performAnimation(view: View) {
        val animation = AnimationUtils.loadAnimation(this.context, R.anim.btnscale)
        animation.duration = 50
        view.startAnimation(animation)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
    }
}