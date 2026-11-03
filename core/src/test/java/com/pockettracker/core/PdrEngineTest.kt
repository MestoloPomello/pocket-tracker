package com.pockettracker.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PdrEngineTest {
    @Test fun northStepAdvancesYAxis() {
        val p = Geometry.advance(Point2D(0.0, 0.0), 0.0, 1.0)
        assertEquals(0.0, p.x, 1e-9); assertEquals(1.0, p.y, 1e-9)
    }
    @Test fun eastStepAdvancesXAxis() {
        val p = Geometry.advance(Point2D(0.0, 0.0), Math.PI / 2, 1.0)
        assertEquals(1.0, p.x, 1e-9); assertEquals(0.0, p.y, 1e-9)
    }
    @Test fun anomalyDoesNotIntegrateStep() {
        val engine = PdrEngine(0.7)
        val p = engine.onStep(1, 0.0, true, true)
        assertEquals(0.0, p.position.y, 1e-9); assertEquals(1L, p.steps)
    }
}
