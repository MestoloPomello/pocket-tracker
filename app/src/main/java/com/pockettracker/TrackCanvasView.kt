package com.pockettracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.pockettracker.core.Point2D
import kotlin.math.max

class TrackCanvasView(context: Context) : View(context) {
    private val points = TrackStore.load(context)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(248, 249, 251))
        val cx = width / 2f; val cy = height / 2f
        paint.color = Color.DKGRAY; paint.strokeWidth = 2f; paint.style = Paint.Style.STROKE
        canvas.drawLine(0f, cy, width.toFloat(), cy, paint); canvas.drawLine(cx, 0f, cx, height.toFloat(), paint)
        paint.textSize = 28f; canvas.drawText("N ↑   (tocca per tornare)", 24f, 44f, paint)
        if (points.isEmpty()) { paint.style = Paint.Style.FILL; canvas.drawText("Nessun dato di percorso", 24f, 90f, paint); return }
        val maxCoord = max(5.0, points.maxOf { max(kotlin.math.abs(it.x), kotlin.math.abs(it.y)) })
        val scale = (minOf(width, height) * 0.42 / maxCoord).toFloat()
        paint.color = Color.rgb(30, 115, 190); paint.strokeWidth = 5f; paint.style = Paint.Style.STROKE
        for (i in 1 until points.size) canvas.drawLine(cx + points[i-1].x.toFloat()*scale, cy - points[i-1].y.toFloat()*scale, cx + points[i].x.toFloat()*scale, cy - points[i].y.toFloat()*scale, paint)
        paint.style = Paint.Style.FILL; paint.color = Color.rgb(20, 140, 80); canvas.drawCircle(cx, cy, 9f, paint)
    }
}
