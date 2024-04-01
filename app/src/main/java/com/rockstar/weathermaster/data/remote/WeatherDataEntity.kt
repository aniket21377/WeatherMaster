package com.rockstar.weathermaster.data.remote

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_data")
data class WeatherDataEntity(
    @PrimaryKey
    val date: Int,
    @ColumnInfo(name = "temperature_2m")
    val temperature: Double,
    @ColumnInfo(name = "temperature_min")
    val temperature_min: Double,
    @ColumnInfo(name = "temperature_max")
    val temperature_max: Double
)

