package com.rockstar.weathermaster.data.remote

import com.squareup.moshi.Json

data class WeatherDto(
    @field:Json(name = "daily")
    val weatherData: WeatherDataDto
)
