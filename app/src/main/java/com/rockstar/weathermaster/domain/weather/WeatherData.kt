package com.rockstar.weathermaster.domain.weather

import java.time.LocalDateTime

data class WeatherData(
    val time: LocalDateTime,
    val temperatureCelsius: Double,
    val maxt :Double,
    val mint :Double
)
