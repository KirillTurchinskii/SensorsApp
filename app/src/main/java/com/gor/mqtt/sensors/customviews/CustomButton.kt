package com.gor.mqtt.sensors.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatButton
import com.gor.mqtt.sensors.R

class CustomButton : AppCompatButton {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    override fun performLongClick(): Boolean {
        return super.performLongClick()
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            performAnimation(this)
        }
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