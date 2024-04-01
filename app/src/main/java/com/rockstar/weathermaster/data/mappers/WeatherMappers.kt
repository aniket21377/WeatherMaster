package com.rockstar.weathermaster.data.mappers

import android.util.Log
import com.rockstar.weathermaster.data.remote.WeatherDataDto
import com.rockstar.weathermaster.data.remote.WeatherDto
import com.rockstar.weathermaster.domain.weather.WeatherData
import com.rockstar.weathermaster.domain.weather.WeatherInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
private data class IndexedWeatherData(
    val index: Int,
    val data: WeatherData
)
fun WeatherDataDto.toWeatherDataMap(): Map<Int, List<WeatherData>> {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val weatherDataMap = mutableMapOf<Int, MutableList<WeatherData>>()

    for ((index, time) in time.withIndex()) {
        val date = LocalDate.parse(time.substring(0, 10), formatter) // Ensure correct date format
        val maxTemperature = temperatures_max[index]
        val minTemperature = temperatures_min[index]

        val weatherData = WeatherData(
            time = date.atStartOfDay(),
            temperatureCelsius = ((maxTemperature + minTemperature) / 2.0).let{String.format("%.2f", it).toDouble()},
            maxt = maxTemperature,
            mint = minTemperature
        )

        val dayOfYear = date.dayOfYear
        if (weatherDataMap.containsKey(dayOfYear)) {
            weatherDataMap[dayOfYear]?.add(weatherData)
        } else {
            weatherDataMap[dayOfYear] = mutableListOf(weatherData)
        }

        Log.d("WeatherDataMap", "Added WeatherData for date: $date, max temp: $maxTemperature, min temp: $minTemperature")
    }

    return weatherDataMap
}

fun WeatherDto.toWeatherInfo(): WeatherInfo {
    val weatherDataMap = weatherData.toWeatherDataMap()

    // Extract the date from the weather data map
    val currentDate = weatherDataMap.values.flatten().lastOrNull()?.time?.toLocalDate()

    // Log the current date
    Log.d("WeatherInfo", "Current Date: $currentDate")

    // Find the weather data for the current date, if available
    val currentWeatherData = weatherDataMap.flatMap { it.value }
        .firstOrNull { it.time.toLocalDate() == currentDate }

    Log.d("WeatherInfo", "Mapping WeatherDto to WeatherInfo: $weatherDataMap, Current WeatherData: $currentWeatherData")

    return WeatherInfo(
        weatherDataPerDay = weatherDataMap,
        currentWeatherData = currentWeatherData
    )
}
