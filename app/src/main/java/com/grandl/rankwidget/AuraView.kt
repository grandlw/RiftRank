package com.grandl.rankwidget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class AuraView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var phase = 0f
    private var colors = RankTheme.colors("Platinum")
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 9000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { phase = it.animatedValue as Float; invalidate() }
    }
    fun setTier(tier: String) { colors = RankTheme.colors(tier); invalidate() }
    override fun onAttachedToWindow() { super.onAttachedToWindow(); if (!animator.isStarted) animator.start() }
    override fun onDetachedFromWindow() { animator.cancel(); super.onDetachedFromWindow() }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(7, 11, 18))
        val w = width.toFloat(); val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        val x = w * (0.15f + 0.7f * phase)
        val y = h * (0.30f + 0.10f * kotlin.math.sin(phase * Math.PI * 2).toFloat())
        paint.shader = RadialGradient(x, y, maxOf(w,h)*0.70f,
            intArrayOf(Color.argb(105, Color.red(colors[0]), Color.green(colors[0]), Color.blue(colors[0])), Color.TRANSPARENT),
            floatArrayOf(0f,1f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f,0f,w,h,paint)
        val x2 = w - x
        paint.shader = RadialGradient(x2, h*0.82f, maxOf(w,h)*0.55f,
            intArrayOf(Color.argb(65, Color.red(colors[1]), Color.green(colors[1]), Color.blue(colors[1])), Color.TRANSPARENT),
            floatArrayOf(0f,1f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f,0f,w,h,paint)
        paint.shader = null
    }
}
