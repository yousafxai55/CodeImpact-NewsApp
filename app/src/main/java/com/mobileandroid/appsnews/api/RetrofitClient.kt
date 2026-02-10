package com.mobileandroid.appsnews.api

import com.google.gson.GsonBuilder

object RetrofitClient {
    private const val BASE_URL = "https://api.wit.ai/"

    // ✅ GSON ko lenient banao - malformed JSON accept karega
    private val gson = GsonBuilder()
        .setLenient()  // ✅ Ye add karo
        .create()

    val witService: WitAiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson)) // ✅ Custom GSON use karo
            .build()
            .create(WitAiService::class.java)
    }
}