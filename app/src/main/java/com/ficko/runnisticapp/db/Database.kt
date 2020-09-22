package com.ficko.runnisticapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(Converters::class) // Instructs Room that it should look into the Converters class for TypeConverters
abstract class Database : RoomDatabase() {
    abstract fun getRunDao(): RunDAO
}