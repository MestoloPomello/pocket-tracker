package com.pockettracker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import java.io.File

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var stepLength: EditText
    private val createCsv = 21

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNeededPermissions()
        renderHome()
    }

    private fun requestNeededPermissions() {
        val permissions = mutableListOf(Manifest.permission.ACTIVITY_RECOGNITION)
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.POST_NOTIFICATIONS
        val missing = permissions.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 10)
    }

    private fun renderHome() {
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(28, 24, 28, 24) }
        layout.addView(TextView(this).apply { text = "PocketTracker"; textSize = 28f; gravity = Gravity.CENTER })
        status = TextView(this).apply { text = "PDR sperimentale · nessun GPS"; textSize = 16f; gravity = Gravity.CENTER; setPadding(0, 24, 0, 16) }
        layout.addView(status)
        layout.addView(TextView(this).apply { text = "Lunghezza passo (metri)" })
        stepLength = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(getPreferences(MODE_PRIVATE).getFloat("step_length", 0.70f).toString())
        }
        layout.addView(stepLength)
        layout.addView(Button(this).apply { text = "Avvia"; setOnClickListener { startTracking() } })
        layout.addView(Button(this).apply { text = "Interrompi"; setOnClickListener { stopService(Intent(this@MainActivity, TrackingService::class.java)); status.text = "Sessione interrotta e salvata" } })
        layout.addView(Button(this).apply { text = "Visualizza percorso"; setOnClickListener { setContentView(TrackCanvasView(this@MainActivity).apply { setOnClickListener { renderHome() } }) } })
        layout.addView(Button(this).apply { text = "Esporta CSV"; setOnClickListener { exportCsv() } })
        setContentView(layout)
    }

    private fun startTracking() {
        if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestNeededPermissions(); status.text = "Concedi il permesso Attività fisica e poi riprova"; return
        }
        val length = stepLength.text.toString().toFloatOrNull()?.coerceIn(0.3f, 1.2f) ?: 0.70f
        getPreferences(MODE_PRIVATE).edit().putFloat("step_length", length).apply()
        val intent = Intent(this, TrackingService::class.java).setAction(TrackingService.ACTION_START).putExtra(TrackingService.EXTRA_STEP_LENGTH, length)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
        status.text = "Registrazione in corso · passo $length m"
    }

    private fun exportCsv() {
        if (!File(filesDir, "sessions.csv").exists()) {
            Toast.makeText(this, "Nessuna sessione da esportare", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "pockettracker-sessions.csv")
        }
        startActivityForResult(intent, createCsv)
    }

    @Deprecated("Uses the platform document picker for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == createCsv && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return
            try {
                contentResolver.openOutputStream(uri)?.use { output ->
                    File(filesDir, "sessions.csv").inputStream().use { it.copyTo(output) }
                }
                Toast.makeText(this, "CSV esportato", Toast.LENGTH_SHORT).show()
            } catch (error: Exception) {
                Toast.makeText(this, "Esportazione non riuscita: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
