package com.ficko.runnisticapp.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run(
    var img: Bitmap? = null,        // preview image of the run
    var timestamp: Long = 0L,       // beginning of the run in Unix time
    var avgSpeed: Float = 0f,       // in km/h
    var distance: Int = 0,          // in meters
    var duration: Long = 0L,        // duration in milliseconds
    var caloriesBurned: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}