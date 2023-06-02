package com.weather.weathertask.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import com.weather.weathertask.R
import com.weather.weathertask.databinding.ActivityMainBinding
import com.weather.weathertask.model.WeatherModel
import com.weather.weathertask.network.ApiService
import com.weather.weathertask.utils.StringUtils
import com.weather.weathertask.viewmodel.WeatherViewmodel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.*


class MainActivity : AppCompatActivity(), LocationListener {
    private var isLocationFetched: Boolean = false
    private lateinit var locationmanager: LocationManager
    private var progressDialog: ProgressDialog? = null
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var weatherViewmodel: WeatherViewmodel
    private lateinit var locationByGps: Location
    private lateinit var locationByNetwork: Location
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    // private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager
    private lateinit var sharedPreference: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)
        sharedPreference = getSharedPreferences("WEATHER_APP", Context.MODE_PRIVATE)
        var editor: SharedPreferences.Editor = sharedPreference.edit()


        var city = sharedPreference.getString("City", "")
        mainBinding.citySearch.setText(city)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1432
            );
        } else {
            progressDialog =
                ProgressDialog.show(this@MainActivity, null, resources.getString(R.string.loading))
            locationmanager = getSystemService(LOCATION_SERVICE) as LocationManager
            locationmanager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 100f, this)

        }



        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//------------------------------------------------------//
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByGps = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
//------------------------------------------------------//
        val networkLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByNetwork = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (hasGps) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                gpsLocationListener
            )
        }
//------------------------------------------------------//
        if (hasNetwork) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                0F,
                networkLocationListener
            )
        }


        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let {
            locationByGps = lastKnownLocationByGps
        }
//------------------------------------------------------//
        val lastKnownLocationByNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        lastKnownLocationByNetwork?.let {
            locationByNetwork = lastKnownLocationByNetwork
        }
//------------------------------------------------------//
        if (locationByGps != null && locationByNetwork != null) {
            if (locationByGps.accuracy > locationByNetwork!!.accuracy) {
                currentLocation = locationByGps
                latitude = currentLocation!!.latitude
                longitude = currentLocation!!.longitude

                editor.putString("latitude", latitude.toString())
                editor.putString("longitude", longitude.toString())
                editor.apply()
                // use latitude and longitude as per your need
            } else {
                currentLocation = locationByNetwork
                latitude = currentLocation!!.latitude
                longitude = currentLocation!!.longitude
                // use latitude and longitude as per your need
            }
        }

        weatherViewmodel = ViewModelProvider(this)[WeatherViewmodel::class.java]
        var list = weatherViewmodel.observeWeatherLiveData()
        mainBinding.weatherViewModel = weatherViewmodel
        mainBinding.lifecycleOwner = this@MainActivity

        mainBinding.goWeatherBtn.setOnClickListener {
            if (city.equals("")) {
                city = mainBinding.citySearch.text.toString().trim()
            }

            city = mainBinding.citySearch.text.toString().trim()
            editor.putString("City", city)
            editor.apply()
            CoroutineScope(Dispatchers.IO).launch {
                val response = ApiService.getApi()
                    ?.getWeatherByCity(city!!, WEATHER_KEY)
                /*?.getWeather(
                    latitude.toString(),
                    longitude.toString(),
                    "16a65673c31ba783edb476c6f1b1dc9b"
                )*/

                Log.d("TAG", "onCreateWeather: " + response!!.body().toString())

                if (response.isSuccessful) {
                    updateData(response)
                }
            }
        }

        val permission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {

        } else {
            Toast.makeText(this, "No permission given", Toast.LENGTH_LONG).show()
        }


    }

    private fun getLocation() {

        val geocoder: Geocoder
        val addresses: List<Address>?
        geocoder = Geocoder(applicationContext, Locale.getDefault())

        try {
            val lat: String = sharedPreference.getString("latitude", "0.0")!!
            val langi: String = sharedPreference.getString("longitude", "0.0")!!
            addresses = geocoder.getFromLocation(lat.toDouble(), langi.toDouble(), 1)
            var address = addresses!![0].getAddressLine(0)
            var city = addresses!![0].locality

            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            progressDialog =
                ProgressDialog.show(this@MainActivity, null, resources.getString(R.string.loading))
            locationmanager = getSystemService(LOCATION_SERVICE) as LocationManager
            locationmanager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 100f, this)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
        }
    }

    companion object {
        var PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        private const val WEATHER_KEY = "16a65673c31ba783edb476c6f1b1dc9b"

    }


    override fun onLocationChanged(location: Location) {

        if (!isLocationFetched) {
            latitude = location.getLatitude()
            longitude = location.getLongitude()
            getCurrentWeatherData(latitude, longitude)
            isLocationFetched = true
            val geocoder: Geocoder
            val addresses: List<Address>?
            try {
                geocoder = Geocoder(this, Locale.getDefault())
                addresses =
                    geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1)
                //address = addresses.get(0).getAdminArea();
                var address =
                    addresses!![0].getAddressLine(0)
                var countryCode = addresses[0].countryCode
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getCurrentWeatherData(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiService.getApi()
                ?.getWeather(latitude, longitude, WEATHER_KEY)
            /*?.getWeather(
                latitude.toString(),
                longitude.toString(),
                "16a65673c31ba783edb476c6f1b1dc9b"
            )*/

            Log.d("TAG", "onCreateWeather: " + response!!.body().toString())

            if (response!!.isSuccessful) {
                progressDialog!!.dismiss()
                updateData(response);
            } else {
                progressDialog!!.dismiss()
                Toast.makeText(this@MainActivity, response.message(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateData(response: Response<WeatherModel>) {

        CoroutineScope(Dispatchers.Main).launch {

            weatherViewmodel.temp.value =
                /*StringUtils.convertTemp*/(response.body()!!.temp!!.temp.toString())
            // mainBinding.temp.text = response.body()!!.temp!!.temp.toString()
            weatherViewmodel.pressure.value = response.body()!!.temp!!.pressure.toString()
            weatherViewmodel.humidity.value = response.body()!!.temp!!.humidity.toString()
            weatherViewmodel.placeName.value = response.body()!!.name.toString()

            var iconsList = response.body()!!.weatherInfo
            var iconUrl =
                "https://openweathermap.org/img/wn/" + iconsList!!.get(0).icon + "@" + "2x" + ".png"
            Picasso.get().load(iconUrl).fit().into(mainBinding.weatherIcon)
        }
    }

}