package com.mobileandroid.appsnews.api

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchResponse {

    suspend fun getResponse(urlString: String): String? {
        return withContext(Dispatchers.IO) {
            try {

                Log.d("FetchResponse", "Requesting URL: $urlString") // Log the URL being requested


                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"

                Log.d("FetchResponse", "Response received: $connection") // Log the response

                connection.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {

                Log.e("FetchResponse", "Error fetching response: ${e.message}") // Log the exception

                e.printStackTrace()
                null
            }
        }
    }
}

