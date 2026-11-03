package com.pockettracker.core

import kotlin.math.cos
import kotlin.math.sin

data class Point2D(val x: Double, val y: Double)
data class TrackPoint(val timestampMillis: Long, val steps: Long, val headingRadians: Double, val position: Point2D, val directionReliable: Boolean, val anomaly: Boolean)

/** Heading is clockwise from device-up, corrected by a pocket mounting offset. */
object Geometry {
    fun wrapRadians(value: Double): Double {
        var angle = value
        while (angle <= -Math.PI) angle += 2 * Math.PI
        while (angle > Math.PI) angle -= 2 * Math.PI
        return angle
    }

    fun advance(point: Point2D, headingRadians: Double, distanceMeters: Double) =
        Point2D(point.x + sin(headingRadians) * distanceMeters, point.y + cos(headingRadians) * distanceMeters)
}

/** Experimental step-and-heading PDR. Coordinates use x east, y north; heading clockwise from north. */
class PdrEngine(var stepLengthMeters: Double = 0.70, private var mountingOffsetRadians: Double = 0.0) {
    var position = Point2D(0.0, 0.0)
        private set
    var steps = 0L
        private set

    fun setMountingOffset(radians: Double) { mountingOffsetRadians = radians }
    fun onStep(timestampMillis: Long, rawHeadingRadians: Double, reliable: Boolean, anomaly: Boolean): TrackPoint {
        steps++
        val heading = Geometry.wrapRadians(rawHeadingRadians + mountingOffsetRadians)
        // Keep logging every step. Do not integrate heading while the phone movement looks like handling/extraction.
        if (!anomaly) position = Geometry.advance(position, heading, stepLengthMeters.coerceIn(0.3, 1.2))
        return TrackPoint(timestampMillis, steps, heading, position, reliable && !anomaly, anomaly)
    }
}
