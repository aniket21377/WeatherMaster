package com.rockstar.weathermaster.data.repository

import android.util.Log
import com.rockstar.weathermaster.data.mappers.toWeatherInfo
import com.rockstar.weathermaster.data.remote.WeatherApi
import com.rockstar.weathermaster.data.remote.WeatherDataEntity
import com.rockstar.weathermaster.domain.repository.WeatherRepository
import com.rockstar.weathermaster.domain.util.Resource
import com.rockstar.weathermaster.domain.weather.WeatherInfo
import javax.inject.Inject
import com.rockstar.weathermaster.data.remote.WeatherDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi
): WeatherRepository {

    override suspend fun getWeatherData(lat: Double, long: Double, date: String): Resource<WeatherInfo> {
        return try {
            val dateString = date // Example date string
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val localDate = LocalDate.parse(dateString, formatter)
            val dateAsInteger = localDate.year * 10000 + localDate.monthValue * 100 + localDate.dayOfMonth
            val startDate = if (dateAsInteger < 20100101) date else "2010-01-01"
            Log.d(TAG, "INTEGER: $dateAsInteger, $startDate")
            Resource.Success(
                data = api.getWeatherData(
                    lat = lat,
                    long = long,
                    date=startDate,
                    date1=date
                ).toWeatherInfo()

            )

        } catch(e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "An unknown error occurred.")
        }
    }
    private  val TAG = "WeatherRepositoryImpl"



    override suspend fun addWeatherData(weatherInfo: WeatherInfo, date: String, db: WeatherDatabase) {
        try {
            withContext(Dispatchers.IO) {
                // Iterate over each day's weather data and insert it into the database
                weatherInfo.weatherDataPerDay.values.flatten().forEach { weatherData ->
                    // Convert WeatherData to WeatherDataEntity
                    val formattedTimeInt = weatherData.time.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt()

                    val entity = WeatherDataEntity(
                        date = formattedTimeInt,
                        temperature = weatherData.temperatureCelsius,
                        temperature_max = weatherData.maxt,
                        temperature_min = weatherData.mint
                    )
                    // Insert WeatherDataEntity into the database using injected WeatherDataDao instance
                    db.weatherDataDao().insertweather(entity)
                    Log.d(TAG, "Weather data inserted into database: $entity")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert weather data into database: ${e.message}")
            throw e // Rethrow the exception to indicate failure
        }
    }


    override suspend fun getWeatherDataForDate(requestedDate: Int, db: WeatherDatabase): WeatherDataEntity? {
        return withContext(Dispatchers.IO) {
            try {
                db.weatherDataDao().getWeatherDataForDate(requestedDate)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch weather data for date $requestedDate: ${e.message}")
                null
            }
        }
    }
    override suspend fun getWeatherDataForYear(year: Int,requestedDate: Int, db: WeatherDatabase): List<WeatherDataEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val requestedYear = requestedDate / 10000 // Extracting year
                val requestedMonth = requestedDate / 100 % 100 // Extracting month
                val requestedDay = requestedDate % 100 // Extracting day

                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                val startDate = LocalDate.of(year, requestedMonth, requestedDay).format(formatter).toInt()
                Log.d(TAG, "fetch weather data for year $requestedMonth ,$requestedDay")
                db.weatherDataDao().getWeatherDataForYear(startDate)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch weather data for year $year: ${e.message}")
                throw e
            }
        }
    }





}

