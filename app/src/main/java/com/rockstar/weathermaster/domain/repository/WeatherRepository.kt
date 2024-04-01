package com.rockstar.weathermaster.domain.repository

import com.rockstar.weathermaster.data.remote.WeatherDataEntity
import com.rockstar.weathermaster.data.remote.WeatherDatabase
import com.rockstar.weathermaster.domain.util.Resource
import com.rockstar.weathermaster.domain.weather.WeatherInfo

interface WeatherRepository {
    suspend fun getWeatherData(lat: Double, long: Double, date: String): Resource<WeatherInfo>
    suspend fun addWeatherData(weatherInfo: WeatherInfo, date: String,db:WeatherDatabase)
    suspend fun getWeatherDataForDate(requestedDate: Int, db: WeatherDatabase): WeatherDataEntity?
    suspend fun getWeatherDataForYear(year: Int,requestedDate: Int, db: WeatherDatabase): List<WeatherDataEntity>
}