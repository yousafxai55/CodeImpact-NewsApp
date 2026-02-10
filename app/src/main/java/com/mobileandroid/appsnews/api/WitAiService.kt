//package com.mobileandroid.appsnews.api
//
//import com.mobileandroid.appsnews.models.WitResponse
//import okhttp3.RequestBody
//import retrofit2.http.Body
//import retrofit2.http.Header
//import retrofit2.http.POST
//
//interface WitAiService {
//    @POST("speech")
//    suspend fun speechToText(
//        @Header("Authorization") authToken: String, // ✅ "Bearer XXX" format mein
//        @Header("Content-Type") contentType: String, // ✅ Dynamically set karo
//        @Body audioFile: RequestBody
//    ): WitResponse
//}


package com.mobileandroid.appsnews.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface WitAiService {
    @POST("speech")
    suspend fun speechToText(
        @Header("Authorization") authToken: String,
        @Header("Content-Type") contentType: String,
        @Body audioFile: RequestBody
    ): ResponseBody  // ✅ Raw response
}