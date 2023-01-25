package com.example.weather

data class WeatherModel(
    val city: String,
    val time: String,
    val condition: String,
    val imageUrl: String,
    val currentTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val hoursTemp: String,
    val currentTime: String,
    val feelsLike: String

)
