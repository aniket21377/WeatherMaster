package com.rockstar.weathermaster.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("v1/archive?&daily=temperature_2m_max,temperature_2m_min")
    suspend fun getWeatherData(
        @Query("latitude") lat: Double,
        @Query("longitude") long: Double,
        @Query("start_date") date:String,
        @Query("end_date") date1:String
    ): WeatherDto
}