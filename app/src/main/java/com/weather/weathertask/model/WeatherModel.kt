package com.weather.weathertask.model

import com.google.gson.annotations.SerializedName

data class WeatherModel(
    @SerializedName("coord") var cord: CoordModel? = null,
    @SerializedName("main") var temp: WeatherTempModel? = null,
    @SerializedName("weather") var weatherInfo: List<WeatherInModel>? = null,
    @SerializedName("name") var name: String? = null,
) {
    override fun toString(): String {
        return "WeatherModel(cord=$cord)"
    }
}
