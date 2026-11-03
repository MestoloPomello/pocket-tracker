package com.pockettracker

import android.app.*
import android.content.Intent
import android.hardware.*
import android.os.Build
import android.os.IBinder
import com.pockettracker.core.PdrEngine
import kotlin.math.PI

class TrackingService : Service(), SensorEventListener {
    private lateinit var manager: SensorManager
    private lateinit var engine: PdrEngine
    private var heading = 0.0
    private var reliable = false
    private var anomalyUntil = 0L
    private var rotationSensor: Sensor? = null
    private var accel: Sensor? = null
    private var lastMagnitude = 0f

    override fun onCreate() {
        super.onCreate()
        manager = getSystemService(SENSOR_SERVICE) as SensorManager
        createChannel()
        startForeground(1, notification("PocketTracker sta registrando"))
        FileHeader.ensure(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        if (intent?.action == ACTION_START) {
            engine = PdrEngine(intent.getFloatExtra(EXTRA_STEP_LENGTH, 0.70f).toDouble())
            val stepDetector = manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            rotationSensor = manager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
                ?: manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            accel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            stepDetector?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
            rotationSensor?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            accel?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            TrackStore.append(this, "# session_start,${System.currentTimeMillis()},step_length_m,${engine.stepLengthMeters}")
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GAME_ROTATION_VECTOR, Sensor.TYPE_ROTATION_VECTOR -> {
                val matrix = FloatArray(9); SensorManager.getRotationMatrixFromVector(matrix, event.values)
                val orientation = FloatArray(3); SensorManager.getOrientation(matrix, orientation)
                // With the phone upside down, its top edge points opposite the walking direction.
                heading = wrap(orientation[0].toDouble() + PI)
                reliable = event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val magnitude = kotlin.math.sqrt(event.values.sumOf { it.toDouble() * it })
                if (lastMagnitude != 0f && kotlin.math.abs(magnitude - lastMagnitude) > 7.5) anomalyUntil = System.currentTimeMillis() + 1800
                lastMagnitude = magnitude.toFloat()
            }
            Sensor.TYPE_STEP_DETECTOR -> recordStep()
        }
    }

    private fun recordStep() {
        val now = System.currentTimeMillis()
        val anomaly = now < anomalyUntil
        val point = engine.onStep(now, heading, reliable, anomaly)
        TrackStore.append(this, "${now},${point.steps},${point.headingRadians},${if (reliable) "rotation-vector" else "unreliable"},${anomaly},${point.position.x},${point.position.y}")
    }

    private fun wrap(angle: Double): Double {
        var result = angle
        while (result > PI) result -= 2 * PI
        while (result <= -PI) result += 2 * PI
        return result
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { reliable = accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE }
    override fun onDestroy() { manager.unregisterListener(this); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel("tracking", "Tracking", NotificationManager.IMPORTANCE_LOW))
    }
    private fun notification(message: String) = if (Build.VERSION.SDK_INT >= 26)
        Notification.Builder(this, "tracking").setContentTitle("PocketTracker").setContentText(message).setSmallIcon(android.R.drawable.ic_menu_mylocation).build()
    else @Suppress("DEPRECATION") Notification.Builder(this).setContentTitle("PocketTracker").setContentText(message).setSmallIcon(android.R.drawable.ic_menu_mylocation).build()

    companion object {
        const val ACTION_START = "com.pockettracker.START"
        const val ACTION_STOP = "com.pockettracker.STOP"
        const val EXTRA_STEP_LENGTH = "step_length_m"
    }
}
