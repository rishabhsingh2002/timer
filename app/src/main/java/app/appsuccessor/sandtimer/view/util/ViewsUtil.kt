package app.appsuccessor.sandtimer.view.util

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import app.appsuccessor.sandtimer.R
import java.time.LocalTime

fun View.clickTo(
    pressEffect: Boolean = true,
    debounceIntervalMs: Int = 700,
    listener: (view: View?) -> Unit
) {
    var lastTapTimestamp: Long = 0
    this.setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTimestamp > debounceIntervalMs) {
            lastTapTimestamp = currentTime
            listener(it)
        }
    }
    if (pressEffect) {
        addPressEffect()
    }
}

fun View.addPressEffect() {
    val alphaAnimator: ValueAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0.7f).apply {
        duration = 200
    }

    fun isMotionEventInsideView(view: View, event: MotionEvent): Boolean {
        val viewRect = Rect(
            view.left,
            view.top,
            view.right,
            view.bottom
        )
        return viewRect.contains(
            view.left + event.x.toInt(),
            view.top + event.y.toInt()
        )
    }
    setOnTouchListener(View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                alphaAnimator.start()
                v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_press))
                return@OnTouchListener true
            }

            MotionEvent.ACTION_UP -> {
                alphaAnimator.reverse()
                v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_release))
                if (isMotionEventInsideView(v, event)) {
                    performClick()
                }
                return@OnTouchListener true
            }
        }
        false
    })
}

fun Activity.setStatusBarColor(color: Int) {
    var flags = window?.decorView?.systemUiVisibility // get current flag
    if (flags != null) {
        if (isColorDark(color)) {
            flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            window?.decorView?.systemUiVisibility = flags
        } else {
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window?.decorView?.systemUiVisibility = flags
        }
    }
    window?.statusBarColor = color
}

fun Activity.isColorDark(color: Int): Boolean {
    val darkness =
        1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
    return darkness >= 0.5
}

fun View.clickToShow(action: () -> Unit) {
    this.setOnClickListener {
        action()
    }
    this.isClickable = true
}

fun isNightTime(): Boolean {
    val currentTime = LocalTime.now()
    return currentTime.isAfter(LocalTime.of(18, 0)) || currentTime.isBefore(
        LocalTime.of(
            6,
            0
        )
    )
}
