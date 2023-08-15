package com.weather.weathertask.utils

import java.math.RoundingMode
import java.text.DecimalFormat

class StringUtils {


    companion object {
        fun convertTemp(temp: String): String {
            val intTemp: Float = temp.toFloat()
            var tempConv = ((intTemp - 273.1) * (9 / 5)) + 32
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING
            return "${df.format(tempConv).toFloat()} F"
        }
    }
}