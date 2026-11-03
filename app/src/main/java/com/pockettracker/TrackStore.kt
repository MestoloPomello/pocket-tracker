package com.pockettracker

import android.content.Context
import com.pockettracker.core.Point2D
import java.io.File

object TrackStore {
    private const val FILE = "sessions.csv"
    fun append(context: Context, row: String) {
        File(context.filesDir, FILE).appendText(row + "\n")
    }
    fun load(context: Context): List<Point2D> = runCatching {
        File(context.filesDir, FILE).readLines().drop(1).mapNotNull { line ->
            val c = line.split(","); if (c.size < 7) null else Point2D(c[5].toDouble(), c[6].toDouble())
        }
    }.getOrDefault(emptyList())
}
