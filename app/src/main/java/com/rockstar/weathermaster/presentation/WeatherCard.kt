package com.rockstar.weathermaster.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import com.rockstar.weathermaster.data.remote.WeatherDatabase

@Composable
fun WeatherCard(
    state: WeatherState,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel,
    db:WeatherDatabase
)
{
    state.weatherInfo?.currentWeatherData?.let { data ->
        Card(
            backgroundColor = backgroundColor,
            shape = RoundedCornerShape(10.dp),
            modifier = modifier.padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Date : ${
                        data.time.format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        )
                    }",
                    modifier = Modifier.align(Alignment.End),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Average Temprature",
                    fontSize = 35.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${data.temperatureCelsius}°C",
                    fontSize = 34.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = "Max",
                        fontSize = 30.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "Min",
                        fontSize = 30.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = "${data.maxt}°C",
                        fontSize = 30.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "${data.mint}°C",
                        fontSize = 30.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
                Button(onClick = {
                    state.weatherInfo?.let { viewModel.addWeatherDataToDatabase( it,db) }
                }) {
                    Text(text = "Save for Offline Access")
                }
            }
        }
    }
}