package com.weather.weathertask.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.weathertask.model.WeatherInModel
import com.weather.weathertask.utils.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.Util

class WeatherViewmodel : ViewModel() {

    private var newList = MutableLiveData<WeatherInModel>()

    fun observeWeatherLiveData(): LiveData<WeatherInModel> {
        return newList
    }

    var placeName = MutableLiveData<String>()
    var temp = MutableLiveData<String>()
    var pressure = MutableLiveData<String>()
    var humidity = MutableLiveData<String>()
    var clouds = MutableLiveData<String>()



    fun longTask() {
        viewModelScope.launch(Dispatchers.Default) {
                withContext(Dispatchers.Main) {
                    temp.value = StringUtils.convertTemp("${temp.value}")
                    Log.d("TAG", "longTask: "+ temp.value)
                }
        }
    }
}