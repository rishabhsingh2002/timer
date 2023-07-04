package app.appsuccessor.sandtimer.view.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import app.appsuccessor.sandtimer.R
import java.util.*

class CustomAnalogClock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val hourHandColor =
        resources.getColor(R.color.clock_hour_bg) // Replace with your desired hour hand color
    private val minuteHandColor =
        resources.getColor(R.color.clock_hour_bg) // Replace with your desired minute hand color
    private val secondsHandColor = Color.RED // Replace with your desired seconds hand color
    private val backgroundColor =
        resources.getColor(R.color.clock_bg) // Replace with your desired background color
    private val dotCircleColor = resources.getColor(R.color.clock_dot_circle)// Replace with your desired dot circle color

    private val hourHandWidth = resources.getDimensionPixelSize(R.dimen.dp8).toFloat()
    private val minuteHandWidth = resources.getDimensionPixelSize(R.dimen.dp4).toFloat()
    private val secondsHandWidth = resources.getDimensionPixelSize(R.dimen.dp2).toFloat()
    private val dotCircleRadius = resources.getDimensionPixelSize(R.dimen.dp8).toFloat()

    private val hourPaint = Paint()
    private val minutePaint = Paint()
    private val secondsPaint = Paint()
    private val backgroundPaint = Paint()
    private val dotCirclePaint = Paint()

    private var handler: Handler? = null
    private val clockRunnable = object : Runnable {
        override fun run() {
            invalidate()
            handler?.postDelayed(this, 1000)
        }
    }

    init {
        hourPaint.color = hourHandColor
        hourPaint.strokeWidth = hourHandWidth
        hourPaint.strokeCap = Paint.Cap.ROUND

        minutePaint.color = minuteHandColor
        minutePaint.strokeWidth = minuteHandWidth
        minutePaint.strokeCap = Paint.Cap.ROUND

        secondsPaint.color = secondsHandColor
        secondsPaint.strokeWidth = secondsHandWidth
        secondsPaint.strokeCap = Paint.Cap.ROUND

        backgroundPaint.color = backgroundColor

        dotCirclePaint.color = dotCircleColor
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler = Handler()
        handler?.post(clockRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler?.removeCallbacks(clockRunnable)
        handler = null
    }

    override fun onDraw(canvas: Canvas) {
        val hourHandLength = (width / 3).toFloat()
        val minuteHandLength = (width / 2.5).toFloat()
        val secondsHandLength = (width / 2.2).toFloat()

        val centerX = width / 2
        val centerY = height / 2

        // Draw background circle
        val backgroundRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawOval(backgroundRect, backgroundPaint)

        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)

        val hourRotation = (hours % 12 + minutes / 60.0) * 30
        val minuteRotation = (minutes + seconds / 60.0) * 6
        val secondsRotation = seconds * 6

        canvas.save()
        canvas.rotate(hourRotation.toFloat(), centerX.toFloat(), centerY.toFloat())
        canvas.drawLine(
            centerX.toFloat(),
            centerY.toFloat(),
            centerX.toFloat(),
            centerY - hourHandLength,
            hourPaint
        )
        canvas.restore()

        canvas.save()
        canvas.rotate(minuteRotation.toFloat(), centerX.toFloat(), centerY.toFloat())
        canvas.drawLine(
            centerX.toFloat(),
            centerY.toFloat(),
            centerX.toFloat(),
            centerY - minuteHandLength,
            minutePaint
        )
        canvas.restore()

        canvas.save()
        canvas.rotate(secondsRotation.toFloat(), centerX.toFloat(), centerY.toFloat())
        canvas.drawLine(
            centerX.toFloat(),
            centerY.toFloat(),
            centerX.toFloat(),
            centerY - secondsHandLength,
            secondsPaint
        )
        canvas.restore()

        // Draw dot circle at the center
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), dotCircleRadius, dotCirclePaint)
    }
}



