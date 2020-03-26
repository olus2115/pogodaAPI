package com.example.pogodynkaapi

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var disposable: Disposable? = null

    private val wikiApiServe by lazy {
        WeatherApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search.setOnClickListener {
            if (searchText.text.toString().isNotEmpty()) {
                search(searchText.text.toString())
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(search.windowToken, 0)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun search(searchString: String) {
        disposable = wikiApiServe.hitCountCheck(
                searchString,
                "b9a31dcb2d2b84843ea2eba6b48ff5f9",
                "metric",
                "pl"
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            city.text = "${result.name}"
                            temp.text = "${result.main.temp} °C"
                            pressure.text = "${result.main.pressure} hPa"
                            description.text = "${result.weather[0].description}"
                            date.text = "${getDate(result.dt.toLong() * 1000, "dd-MM-yyyy")}"
                            if (result.weather[0].main == "Clear") {
                                icon.setBackgroundResource(R.drawable.ic_sun)
                            } else if (result.weather[0].main == "Rainy") {
                                icon.setBackgroundResource(R.drawable.ic_cloud_drizzle)
                            } else if (result.weather[0].main == "Windy") {
                                icon.setBackgroundResource(R.drawable.ic_cloud_wind)
                            } else if (result.weather[0].main == "Clouds") {
                                icon.setBackgroundResource(R.drawable.ic_cloud)
                            }else if (result.weather[0].main == "Drizzle") {
                                icon.setBackgroundResource(R.drawable.ic_cloud_drizzle)
                            }
                            tempIco.setBackgroundResource(R.drawable.ic_temperature)
                            sunriseIco.setBackgroundResource(R.drawable.ic_sunrise)
                            sunsetIco.setBackgroundResource(R.drawable.ic_sunset)

                            sunrise.text = "${getTime(result.sys.sunrise.toLong() * 1000)}"
                            sunset.text = "${getTime(result.sys.sunset.toLong() * 1000)}"
                        }
                        ,
                        { error -> Toast.makeText(this, "Błąd, proszę wpisać poprawne dane!", Toast.LENGTH_LONG).show() } //error.message
                )
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    fun getTime(milliSeconds: Long): String? {
        return String.format(
            " %02d:%02d",
            ((milliSeconds / (1000 * 60 * 60)) % 24),
            ((milliSeconds / (1000 * 60)) % 60)
        )
    }

    fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(milliSeconds)
        return formatter.format(calendar.getTime())
    }


}
