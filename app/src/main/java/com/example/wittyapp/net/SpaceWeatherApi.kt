package com.example.wittyapp.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class SpaceWeatherApi {
    private val client = OkHttpClient()

    suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${'$'}{resp.code} for ${'$'}url")
            resp.body?.string() ?: ""
        }
    }

    suspend fun kp1mJson(): String =
        get("https://services.swpc.noaa.gov/json/planetary_k_index_1m.json")

    suspend fun wind1mJson(): String =
        get("https://services.swpc.noaa.gov/json/rtsw/rtsw_wind_1m.json")

    suspend fun mag1mJson(): String =
        get("https://services.swpc.noaa.gov/json/rtsw/rtsw_mag_1m.json")
}
