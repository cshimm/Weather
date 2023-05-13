package com.example.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.model.GetWeatherByCityResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val apiKey = "5a1c0090c4cb1301347b318400ce6315"
    private val city = "Seattle"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
    private val weatherService by lazy {
        retrofit.create(WeatherService::class.java)
    }
    private val imageLoader: ImageLoader by lazy { GlideImageLoader(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setSupportActionBar(binding.myToolbar)
        setContentView(binding.root)

        getWeatherResponse()
    }

    private fun getWeatherResponse() {
        binding.progressBar.isVisible = true

        val call = weatherService.getWeather(city, apiKey)
        call.enqueue(object : Callback<GetWeatherByCityResponse> {

            override fun onResponse(
                call: Call<GetWeatherByCityResponse>,
                response: Response<GetWeatherByCityResponse>
            ) {
                if (response.isSuccessful) {
                    val getWeatherByCityResponse = response.body()!!
                    val weather = getWeatherByCityResponse.weather[0]
                    val iconURL = "https://openweathermap.org/img/w/${weather.icon}.png"

                    binding.cityName.text = getWeatherByCityResponse.name
                    binding.cityWeatherCondition.text = weather.description
                    binding.cityTemp.text = kelvinToFahrenheit(getWeatherByCityResponse.main.temp)

                    try {
                        imageLoader.loadImage(iconURL, binding.weatherIcon)
                    } catch (e: Exception) {
                        Log.d("MainActivity", "Missing image URL")
                        binding.errorText.text = "Missing image URL"
                        binding.errorText.isVisible = true
                    }
                } else {
                    Log.d("MainActivity", "Missing weather data")
                    binding.errorText.text = "Missing weather data"
                    binding.errorText.isVisible = true
                }
                    binding.progressBar.isVisible = false
            }

            override fun onFailure(call: Call<GetWeatherByCityResponse>, t: Throwable) {
                binding.progressBar.isVisible = false
                binding.errorText.text = "Failed to get search results"
                binding.errorText.isVisible = true
            }
        })
    }
    private fun kelvinToFahrenheit(kelvin: Double) : String {
        return DecimalFormat("#").format(((kelvin - 273.15) * 1.8) + 32).toString() + "°"
    }
}