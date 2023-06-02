package com.weather.weathertask.utils

class StringUtils {


    companion object {
        fun convertTemp(temp: String): String {
            val intTemp: Int = temp.toInt()
            var tempConv = ((intTemp - 273.1) * (9 / 5)) + 32
            return "$tempConv F"
        }
    }
}