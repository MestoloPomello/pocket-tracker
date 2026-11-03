package com.pockettracker

import android.content.Context
import java.io.File

object FileHeader {
    fun ensure(context: Context) {
        val file = File(context.filesDir, "sessions.csv")
        if (!file.exists()) file.writeText("timestamp_ms,step_count,heading_rad,orientation_source,anomaly,x_m,y_m\n")
    }
}
