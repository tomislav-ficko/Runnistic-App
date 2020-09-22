package com.ficko.runnisticapp.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    private val COMPRESSION = 100 // Best quality

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, COMPRESSION, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(array: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(array, 0, array.size)
    }
}