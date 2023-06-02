package com.weather.weathertask

import android.app.Application
import androidx.multidex.MultiDex

class BaseApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
    }
}