package com.rockstar.weathermaster.data.remote

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface WeatherDataDao {
    @Insert
    fun insertweather(weatherDataEntity: WeatherDataEntity)

    @Query("SELECT * FROM weather_data")
    fun getAllWeatherData(): Flow<List<WeatherDataEntity>>

    @Query("SELECT * FROM weather_data WHERE date >= :startDate ORDER BY date ASC")
    suspend fun getWeatherDataForLastNDays(startDate: String): List<WeatherDataEntity>

    @Query("SELECT * FROM weather_data WHERE date= :requestedDate")
    suspend fun getWeatherDataForDate(requestedDate: Int): WeatherDataEntity?

    @Query("SELECT * FROM weather_data WHERE date == :startDate ")
    suspend fun getWeatherDataForYear(startDate: Int): List<WeatherDataEntity>




}