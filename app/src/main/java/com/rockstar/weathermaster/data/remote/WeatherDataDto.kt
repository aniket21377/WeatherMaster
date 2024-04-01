package com.rockstar.weathermaster.data.remote

import com.squareup.moshi.Json

data class WeatherDataDto(
    val time: List<String>,
    @field:Json(name = "temperature_2m_min")
    val temperatures_min: List<Double>,
    @field:Json(name = "temperature_2m_max")
    val temperatures_max: List<Double>
)





