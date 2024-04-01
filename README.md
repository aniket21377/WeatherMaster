
WeatherMaster App README
Overview
This README provides an overview of the WeatherMaster application and its implementation details.

App Setup
Upon installing the WeatherMaster application, it will request permission to track your current location using GPS. After granting permission, the app provides initial API data. For faster and offline access to data, ensure to click on the "Save for offline access" button. You can then access your data with or without an internet connection, resulting in faster access due to the presence of stored data.

Components
DefaultLocationTracker
The DefaultLocationTracker class is responsible for retrieving the user's current location within the WeatherMaster app. It utilizes the Fused Location Provider Client to access location data and requires necessary permissions like ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION. The getCurrentLocation() function attempts to fetch the user's location and returns null if permissions are not granted or if GPS functionality is unavailable.

WeatherMappers.kt
The WeatherMappers.kt file contains utility functions for mapping data objects within the WeatherMaster app. It includes mappers for WeatherDataDto and WeatherDto objects, facilitating seamless data handling and processing.

WeatherApi
The WeatherApi interface, located in the data.remote package, defines methods for fetching weather data from a remote source using Retrofit. It includes a method getWeatherData() for retrieving weather data based on latitude, longitude, start date, and end date parameters.

WeatherDatabase
The WeatherDatabase class serves as a Room database for managing local storage of weather data. It includes entities such as WeatherDataEntity and provides an abstract method to access the Data Access Object (DAO) for performing database operations.

WeatherDataDao
The WeatherDataDao interface, within the data.remote package, serves as the Data Access Object (DAO) for interacting with the Room database containing weather data. It provides methods for inserting, querying, and retrieving weather data entities.

WeatherDataDto
The WeatherDataDto data class represents weather data retrieved from a remote source. It includes properties for timestamps, minimum temperatures, and maximum temperatures, facilitating data transfer and processing from the remote source.

WeatherDataEntity
The WeatherDataEntity data class represents weather data stored in the local Room database. It includes properties for date, temperature, minimum temperature, and maximum temperature, serving as a model for efficient storage and retrieval of weather data.

WeatherDto
The WeatherDto data class represents weather information retrieved from a remote source. It contains a property for daily weather data, facilitating data transfer and processing within the WeatherMaster app.

WeatherRepositoryImpl
The WeatherRepositoryImpl class implements the WeatherRepository interface and manages the retrieval and storage of weather data by interfacing with remote APIs and local databases. It utilizes coroutines for asynchronous operations and implements error handling for graceful exception management.

AppModule
The AppModule object in the di package provides dependencies for the WeatherMaster app using Dagger Hilt. It offers dependencies such as WeatherApi and FusedLocationProviderClient for accessing weather data and obtaining device location.

LocationModule
The LocationModule in the di package binds implementations of the LocationTracker interface using Dagger Hilt. It facilitates seamless dependency injection of location tracking functionality throughout the WeatherMaster app.

RepositoryModule
The RepositoryModule in the di package binds implementations of the WeatherRepository interface using Dagger Hilt. It enables seamless dependency injection of weather repository functionality throughout the WeatherMaster app.

MainActivity
The MainActivity class represents the main entry point of the WeatherMaster app. It initializes view models, sets up the UI using Jetpack Compose, and handles user interactions for weather information retrieval.

WeatherViewModel
The WeatherViewModel class serves as the intermediary between the UI and the data layer within the WeatherMaster app. It handles the retrieval and processing of weather data, location tracking, and interaction with the repository.

WeatherCard
The WeatherCard composable function displays weather information in a card format within the WeatherMaster app. It encapsulates the visual representation of weather data and provides interaction options for saving data offline.

WeatherState
The WeatherState data class represents the current state of weather information in the WeatherMaster app. It manages and updates weather information, loading status, and error messages.

WeatherApp
The WeatherApp class serves as the main application class for the WeatherMaster app. It initializes and configures the application, setting up dependencies and acting as the starting point of the Android application lifecycle.

Conclusion
The WeatherMaster app provides users with access to weather information, both online and offline, with seamless data retrieval and storage functionalities. Through its various components and modules, the app offers a robust and user-friendly experience for staying updated on weather conditions.






