package com.rockstar.weathermaster.data.remote

// Step 3: Room Database class
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WeatherDataEntity::class], version = 5)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDataDao(): WeatherDataDao


}