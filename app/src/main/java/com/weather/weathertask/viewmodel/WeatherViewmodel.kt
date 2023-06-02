package com.weather.weathertask.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.weather.weathertask.model.WeatherInModel

class WeatherViewmodel : ViewModel() {

    private var newList = MutableLiveData<WeatherInModel>()

    fun observeWeatherLiveData(): LiveData<WeatherInModel> {
        return newList
    }

    var placeName = MutableLiveData<String>()
    var temp = MutableLiveData<String>()
    var pressure = MutableLiveData<String>()
    var humidity = MutableLiveData<String>()


}