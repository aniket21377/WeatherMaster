package com.rockstar.weathermaster.presentation

import android.Manifest
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.compose.material.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rockstar.weathermaster.presentation.ui.theme.DarkBlue
import com.rockstar.weathermaster.presentation.ui.theme.DeepBlue
import com.rockstar.weathermaster.presentation.ui.theme.WeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.rockstar.weathermaster.data.remote.WeatherDatabase

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var database: WeatherDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Starting database initialization...")
        database= Room.databaseBuilder(
            applicationContext,
            WeatherDatabase::class.java,
            "weathers.db"
        ).fallbackToDestructiveMigration().build()
        Log.d(TAG, "Database initialization completed.")
        super.onCreate(savedInstanceState)
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            viewModel.loadWeatherInfo(database)
        }
        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ))
        viewModel.insertWeatherData(database)

        setContent {
            WeatherAppTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBlue),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "WeatherMaster",
                            fontSize = 40.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Welcome! How may I assist you today?",
                            fontSize = 20.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        DateInputField(viewModel,database)
                        WeatherCard(
                            state = viewModel.state,
                            backgroundColor = DeepBlue,
                            viewModel = viewModel,
                            db = database
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if(viewModel.state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    viewModel.state.error?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun DateInputField(viewModel: WeatherViewModel,database: WeatherDatabase) {
    var dateInput by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ){

        TextField(
            value = dateInput,
            onValueChange = { date ->
                dateInput = date
                viewModel.updateDateInput(date)
            },
            label = { Text("Enter Date (yyyy-MM-dd)", color = Color.White) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            keyboardActions = KeyboardActions(onDone = { viewModel.loadWeatherInfo(database) }),
            modifier = Modifier.weight(1f) ,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                textColor = Color.White
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { viewModel.loadWeatherInfo(database) },
            modifier = Modifier.wrapContentWidth()
        ) {
            Text("Load Weather")
        }
    }
}


