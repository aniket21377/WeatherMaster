package com.rockstar.weathermaster.presentation

import android.content.ContentValues
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rockstar.weathermaster.data.remote.WeatherDataEntity
import com.rockstar.weathermaster.data.remote.WeatherDatabase
import com.rockstar.weathermaster.domain.location.LocationTracker
import com.rockstar.weathermaster.domain.repository.WeatherRepository
import com.rockstar.weathermaster.domain.util.Resource
import com.rockstar.weathermaster.domain.weather.WeatherData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import com.rockstar.weathermaster.domain.weather.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker
): ViewModel() {
    var state by mutableStateOf(WeatherState())
        private set
    // Date input state
    var dateInput by mutableStateOf("")
        private set
    // Function to update date input
    fun updateDateInput(date: String) {
        dateInput = date
    }
    private val _weatherInfo = MutableStateFlow<WeatherInfo?>(null)
    fun loadWeatherInfo(database: WeatherDatabase) {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            val defaultLatitude = 28.637845 // Default latitude value
            val defaultLongitude = 77.277697 // Default longitude value
            val location = locationTracker.getCurrentLocation()
            if (location != null){
                val TAG = "WeatherViewModel"
                val currentDate = dateInput.takeIf { it.isNotBlank() } ?: LocalDate.now().minusDays(3).toString()
                val requestedDate = dateInput.takeIf { it.isNotBlank() } ?: currentDate
                val currentDate2 = LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt()
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                val formattedDate = LocalDate.parse(requestedDate, DateTimeFormatter.ISO_DATE).format(formatter)
                val dateAsInt = formattedDate.toInt()
                Log.d(TAG, "Requested Date: $requestedDate")
                Log.d(TAG, "Requested Date: $dateAsInt")
                Log.d(TAG, "Requested Date: $currentDate2")

                if (dateAsInt > currentDate2) {
                    // Calculate the average of the last 10 years' temperatures

                    val currentYear = LocalDate.now().year
                    val last10YearsData = mutableListOf<WeatherDataEntity>()
                    for (i in currentYear - 10..currentYear) {
                        val yearData = repository.getWeatherDataForYear(i,dateAsInt, database)
                        last10YearsData.addAll(yearData)
                        Log.d(TAG, "yeardata:$yearData")
                    }

                    if (last10YearsData.isNotEmpty()) {
                        val minTempAverage = last10YearsData.map { it.temperature_min }.average()
                        val maxTempAverage = last10YearsData.map { it.temperature_max }.average()
                        Log.d(TAG, "yeardata:$last10YearsData")
                        Log.d(TAG, "Average of last 10 years' minimum temperature: $minTempAverage")
                        Log.d(TAG, "Average of last 10 years' maximum temperature: $maxTempAverage")
                        val dateString = requestedDate.toString()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val localDateTime = LocalDate.parse(dateString, formatter).atStartOfDay()
                        val temperatureCelsius = ((minTempAverage + maxTempAverage) / 2).let { String.format("%.2f", it).toDouble() }
                        val maxTemp = maxTempAverage.let { String.format("%.2f", it).toDouble() }
                        val minTemp = minTempAverage.let { String.format("%.2f", it).toDouble() }
                        val weatherData = WeatherData(
                            time =  localDateTime,
                            temperatureCelsius = temperatureCelsius,
                            maxt =maxTemp,
                            mint =minTemp
                        )
                        val dayOfYear = localDateTime.dayOfYear // Get the day of the year
                        val weatherDataMap = mapOf(dayOfYear to listOf(weatherData))

                        state = state.copy(
                            weatherInfo = WeatherInfo(
                                weatherDataPerDay = weatherDataMap,
                                currentWeatherData = weatherData
                            ),
                            isLoading = false,
                            error = null
                        )
                    } else {
                        Log.d(TAG, "No data available for the last 10 years.")
                    }
                    Log.d(TAG, "Retrieved data from the database.")
                }  else {

                    Log.d(TAG, "Requested Date: $requestedDate")
                    // Check if the data for the requested date exists in the local database
                    val weatherDataForRequestedDate = repository.getWeatherDataForDate( dateAsInt , database) // Query by primary key

                    Log.d(TAG, "Time: ${weatherDataForRequestedDate}")
                    if (weatherDataForRequestedDate != null) {
                        Log.d(TAG, "Data for requested date retrieved from the database.")
                        Log.d(TAG, "Data for requested date retrieved from the database:")
                        Log.d(TAG, "Time: ${weatherDataForRequestedDate.date}")
                        Log.d(TAG, "Temperature: ${weatherDataForRequestedDate.temperature}")
                        Log.d(TAG, "Max Temperature: ${weatherDataForRequestedDate.temperature_max}")
                        Log.d(TAG, "Min Temperature: ${weatherDataForRequestedDate.temperature_min}")
                        val dateString = weatherDataForRequestedDate.date.toString()
                        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                        val localDateTime = LocalDate.parse(dateString, formatter).atStartOfDay()

                        val weatherData = WeatherData(
                            time =  localDateTime,
                            temperatureCelsius = weatherDataForRequestedDate.temperature,
                            maxt = weatherDataForRequestedDate.temperature_max,
                            mint = weatherDataForRequestedDate.temperature_min
                        )
                        val date = LocalDate.parse(requestedDate) // Parse the requested date
                        val dayOfYear = date.dayOfYear // Get the day of the year
                        val weatherDataMap = mapOf(dayOfYear to listOf(weatherData))

                        state = state.copy(
                            weatherInfo = WeatherInfo(
                                weatherDataPerDay = weatherDataMap,
                                currentWeatherData = weatherData
                            ),
                            isLoading = false,
                            error = null
                        )
                    }
                    else {
                        // Fetch weather data for the requested date from the API
                        Log.d(TAG, "Data for requested date not found in the database. Fetching from API.")
                        when (val result = repository.getWeatherData(location.latitude, location.longitude, requestedDate)) {


                            is Resource.Success -> {
                                state = state.copy(
                                    weatherInfo = result.data,
                                    isLoading = false,
                                    error = null
                                )
                            }
                            is Resource.Error -> {
                                state = state.copy(
                                    weatherInfo = null,
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                        }
                    }
                }
            } else {
                // Location is null, use default latitude and longitude
                val TAG = "WeatherViewModel"
                Log.d(TAG, "Location is null. Using default latitude and longitude.")
                val currentDate = dateInput.takeIf { it.isNotBlank() } ?: LocalDate.now().minusDays(3).toString()
                val requestedDate = dateInput.takeIf { it.isNotBlank() } ?: currentDate
                val currentDate2 = LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt()
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                val formattedDate = LocalDate.parse(requestedDate, DateTimeFormatter.ISO_DATE).format(formatter)
                val dateAsInt = formattedDate.toInt()
                Log.d(TAG, "Requested Date: $requestedDate")
                Log.d(TAG, "Requested Date: $dateAsInt")
                Log.d(TAG, "Requested Date: $currentDate2")

                if (dateAsInt > currentDate2) {
                    // Calculate the average of the last 10 years' temperatures

                    val currentYear = LocalDate.now().year
                    val last10YearsData = mutableListOf<WeatherDataEntity>()
                    for (i in currentYear - 10..currentYear) {
                        val yearData = repository.getWeatherDataForYear(i,dateAsInt, database)
                        last10YearsData.addAll(yearData)
                        Log.d(TAG, "yeardata:$yearData")
                    }

                    if (last10YearsData.isNotEmpty()) {
                        val minTempAverage = last10YearsData.map { it.temperature_min }.average()
                        val maxTempAverage = last10YearsData.map { it.temperature_max }.average()
                        Log.d(TAG, "yeardata:$last10YearsData")
                        Log.d(TAG, "Average of last 10 years' minimum temperature: $minTempAverage")
                        Log.d(TAG, "Average of last 10 years' maximum temperature: $maxTempAverage")
                        val dateString = requestedDate.toString()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val localDateTime = LocalDate.parse(dateString, formatter).atStartOfDay()
                        val temperatureCelsius = ((minTempAverage + maxTempAverage) / 2).let { String.format("%.2f", it).toDouble() }
                        val maxTemp = maxTempAverage.let { String.format("%.2f", it).toDouble() }
                        val minTemp = minTempAverage.let { String.format("%.2f", it).toDouble() }
                        val weatherData = WeatherData(
                            time =  localDateTime,
                            temperatureCelsius = temperatureCelsius,
                            maxt =maxTemp,
                            mint =minTemp
                        )
                        val dayOfYear = localDateTime.dayOfYear // Get the day of the year
                        val weatherDataMap = mapOf(dayOfYear to listOf(weatherData))

                        state = state.copy(
                            weatherInfo = WeatherInfo(
                                weatherDataPerDay = weatherDataMap,
                                currentWeatherData = weatherData
                            ),
                            isLoading = false,
                            error = null
                        )
                    } else {
                        Log.d(TAG, "No data available for the last 10 years.")
                    }
                    Log.d(TAG, "Retrieved data from the database.")
                }  else {

                    Log.d(TAG, "Requested Date: $requestedDate")
                    // Check if the data for the requested date exists in the local database
                    val weatherDataForRequestedDate = repository.getWeatherDataForDate( dateAsInt , database) // Query by primary key

                    Log.d(TAG, "Time: ${weatherDataForRequestedDate}")
                    if (weatherDataForRequestedDate != null) {
                        Log.d(TAG, "Data for requested date retrieved from the database.")
                        Log.d(TAG, "Data for requested date retrieved from the database:")
                        Log.d(TAG, "Time: ${weatherDataForRequestedDate.date}")
                        Log.d(TAG, "Temperature: ${weatherDataForRequestedDate.temperature}")
                        Log.d(TAG, "Max Temperature: ${weatherDataForRequestedDate.temperature_max}")
                        Log.d(TAG, "Min Temperature: ${weatherDataForRequestedDate.temperature_min}")
                        val dateString = weatherDataForRequestedDate.date.toString()
                        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                        val localDateTime = LocalDate.parse(dateString, formatter).atStartOfDay()

                        val weatherData = WeatherData(
                            time =  localDateTime,
                            temperatureCelsius = weatherDataForRequestedDate.temperature,
                            maxt = weatherDataForRequestedDate.temperature_max,
                            mint = weatherDataForRequestedDate.temperature_min
                        )
                        val date = LocalDate.parse(requestedDate) // Parse the requested date
                        val dayOfYear = date.dayOfYear // Get the day of the year
                        val weatherDataMap = mapOf(dayOfYear to listOf(weatherData))

                        state = state.copy(
                            weatherInfo = WeatherInfo(
                                weatherDataPerDay = weatherDataMap,
                                currentWeatherData = weatherData
                            ),
                            isLoading = false,
                            error = null
                        )
                    }
                    else {
                        // Fetch weather data for the requested date from the API
                        Log.d(TAG, "Data for requested date not found in the database. Fetching from API.")
                        when (val result = repository.getWeatherData(defaultLatitude, defaultLongitude, requestedDate)) {


                            is Resource.Success -> {
                                state = state.copy(
                                    weatherInfo = result.data,
                                    isLoading = false,
                                    error = null
                                )
                            }
                            is Resource.Error -> {
                                state = state.copy(
                                    weatherInfo = null,
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                        }
                    }
                }

            }
        }
    }




    // Function to add weather data to the database
    fun addWeatherDataToDatabase(weatherInfo: WeatherInfo,db: WeatherDatabase) {
        viewModelScope.launch {
            // Call repository method to add weather data to the database

            repository.addWeatherData(weatherInfo,dateInput,db)
        }
    }

    fun insertWeatherData(db: WeatherDatabase) {

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.weatherDataDao().insertweather(
                        WeatherDataEntity(19451203, 12.0, 12.0, 12.0 )

                    )
                }
                Log.d(ContentValues.TAG, "Inserted Successfully...")
            } catch (e: Exception) {
                // Handle any potential exceptions
                e.printStackTrace()
            }
        }
    }

    fun addWeatherDataToDatabase1(database: WeatherDatabase) {
        viewModelScope.launch {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                val formattedDate = LocalDate.parse(dateInput, DateTimeFormatter.ISO_DATE).format(formatter)
                val dateAsInt = formattedDate.toInt()
                state.weatherInfo?.currentWeatherData?.let { currentWeatherData ->
                    withContext(Dispatchers.IO) {
                        database.weatherDataDao().insertweather(
                            WeatherDataEntity(
                                date = dateAsInt,
                                temperature = currentWeatherData.temperatureCelsius ,
                                temperature_max = currentWeatherData.maxt,
                                temperature_min = currentWeatherData.mint
                            )
                        )
                    }
                    Log.d(ContentValues.TAG, "Weather data inserted successfully.")
                }
            } catch (e: Exception) {
                // Handle any potential exceptions
                Log.e(ContentValues.TAG, "Error inserting weather data: ${e.message}", e)
            }
        }
    }
}