package com.roadrater.utils

import com.roadrater.database.entities.Car
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

object GetCarInfo {

    private val client = OkHttpClient()

    fun getCarInfo(plateNumber: String): Car {
        val url = "https://thatcar.nz/c/$plateNumber"

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        val document = Jsoup.parse(response.body.string())

        val year = document.select("td:contains(Year)").next().text()
        val make = document.select("td:contains(Make)").next().text()
        val model = document.select("td:contains(Model)").next().text()
        val returnedPlateNumber = document.select("td:contains(Plate number)").next().text()

        return Car(
            number_plate = returnedPlateNumber,
            make = make,
            model = model,
            year = year,
        )
    }
}
