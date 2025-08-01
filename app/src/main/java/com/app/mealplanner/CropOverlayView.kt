package com.app.mealplanner

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CropOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val cropRect = RectF()
    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val overlayPaint = Paint().apply {
        color = Color.BLACK
        alpha = 128
    }
    private val gridPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 2f
        alpha = 200
    }

    private var bitmap: Bitmap? = null
    private var isDragging = false
    private var dragHandle = DragHandle.NONE
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val handleSize = 40f

    enum class DragHandle {
        NONE, MOVE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        initializeCropRect()
        invalidate()
    }

    private fun initializeCropRect() {
        val size = min(width, height) * 0.8f
        val centerX = width / 2f
        val centerY = height / 2f

        cropRect.set(
            centerX - size / 2f,
            centerY - size / 2f,
            centerX + size / 2f,
            centerY + size / 2f
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (bitmap != null) {
            initializeCropRect()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw semi-transparent overlay
        canvas.drawRect(0f, 0f, width.toFloat(), cropRect.top, overlayPaint)
        canvas.drawRect(0f, cropRect.bottom, width.toFloat(), height.toFloat(), overlayPaint)
        canvas.drawRect(0f, cropRect.top, cropRect.left, cropRect.bottom, overlayPaint)
        canvas.drawRect(cropRect.right, cropRect.top, width.toFloat(), cropRect.bottom, overlayPaint)

        // Draw crop rectangle
        canvas.drawRect(cropRect, paint)

        // Draw grid lines
        val thirdWidth = cropRect.width() / 3f
        val thirdHeight = cropRect.height() / 3f

        // Vertical lines
        canvas.drawLine(cropRect.left + thirdWidth, cropRect.top, cropRect.left + thirdWidth, cropRect.bottom, gridPaint)
        canvas.drawLine(cropRect.left + 2 * thirdWidth, cropRect.top, cropRect.left + 2 * thirdWidth, cropRect.bottom, gridPaint)

        // Horizontal lines
        canvas.drawLine(cropRect.left, cropRect.top + thirdHeight, cropRect.right, cropRect.top + thirdHeight, gridPaint)
        canvas.drawLine(cropRect.left, cropRect.top + 2 * thirdHeight, cropRect.right, cropRect.top + 2 * thirdHeight, gridPaint)

        // Draw corner handles
        drawHandle(canvas, cropRect.left, cropRect.top)
        drawHandle(canvas, cropRect.right, cropRect.top)
        drawHandle(canvas, cropRect.left, cropRect.bottom)
        drawHandle(canvas, cropRect.right, cropRect.bottom)
    }

    private fun drawHandle(canvas: Canvas, x: Float, y: Float) {
        canvas.drawCircle(x, y, handleSize / 2f, paint.apply { style = Paint.Style.FILL })
        canvas.drawCircle(x, y, handleSize / 2f, paint.apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
        })
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                dragHandle = getDragHandle(event.x, event.y)
                isDragging = dragHandle != DragHandle.NONE
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val deltaX = event.x - lastTouchX
                    val deltaY = event.y - lastTouchY

                    when (dragHandle) {
                        DragHandle.MOVE -> {
                            moveCropRect(deltaX, deltaY)
                        }
                        DragHandle.TOP_LEFT -> {
                            resizeCropRect(deltaX, deltaY, 0f, 0f)
                        }
                        DragHandle.TOP_RIGHT -> {
                            resizeCropRect(0f, deltaY, deltaX, 0f)
                        }
                        DragHandle.BOTTOM_LEFT -> {
                            resizeCropRect(deltaX, 0f, 0f, deltaY)
                        }
                        DragHandle.BOTTOM_RIGHT -> {
                            resizeCropRect(0f, 0f, deltaX, deltaY)
                        }
                        else -> {}
                    }

                    lastTouchX = event.x
                    lastTouchY = event.y
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                isDragging = false
                dragHandle = DragHandle.NONE
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getDragHandle(x: Float, y: Float): DragHandle {
        // Check corner handles first
        if (isNearPoint(x, y, cropRect.left, cropRect.top)) return DragHandle.TOP_LEFT
        if (isNearPoint(x, y, cropRect.right, cropRect.top)) return DragHandle.TOP_RIGHT
        if (isNearPoint(x, y, cropRect.left, cropRect.bottom)) return DragHandle.BOTTOM_LEFT
        if (isNearPoint(x, y, cropRect.right, cropRect.bottom)) return DragHandle.BOTTOM_RIGHT

        // Check if inside crop rectangle for move
        if (cropRect.contains(x, y)) return DragHandle.MOVE

        return DragHandle.NONE
    }

    private fun isNearPoint(x: Float, y: Float, pointX: Float, pointY: Float): Boolean {
        return abs(x - pointX) < handleSize && abs(y - pointY) < handleSize
    }

    private fun moveCropRect(deltaX: Float, deltaY: Float) {
        val newLeft = cropRect.left + deltaX
        val newTop = cropRect.top + deltaY
        val newRight = cropRect.right + deltaX
        val newBottom = cropRect.bottom + deltaY

        // Keep within bounds
        if (newLeft >= 0 && newRight <= width && newTop >= 0 && newBottom <= height) {
            cropRect.offset(deltaX, deltaY)
        }
    }

    private fun resizeCropRect(deltaLeft: Float, deltaTop: Float, deltaRight: Float, deltaBottom: Float) {
        val newLeft = max(0f, min(cropRect.left + deltaLeft, cropRect.right - 50f))
        val newTop = max(0f, min(cropRect.top + deltaTop, cropRect.bottom - 50f))
        val newRight = min(width.toFloat(), max(cropRect.right + deltaRight, cropRect.left + 50f))
        val newBottom = min(height.toFloat(), max(cropRect.bottom + deltaBottom, cropRect.top + 50f))

        cropRect.set(newLeft, newTop, newRight, newBottom)
    }

    fun getCropRect(): RectF {
        return RectF(cropRect)
    }
}
