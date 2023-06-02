package com.weather.weathertask.network

import com.weather.weathertask.model.WeatherModel
import com.weather.weathertask.network.ApiClient
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("data/2.5/weather")
   suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lng: Double,
        @Query("appid") appId: String
    ): Response<WeatherModel>

    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        //@Query("lon") lng: String,
        @Query("appid") appId: String
    ): Response<WeatherModel>



    companion object {
        fun getApi(): ApiService? {
            return ApiClient.client?.create(ApiService::class.java)
        }
    }

}