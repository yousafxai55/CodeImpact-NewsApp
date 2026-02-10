package com.mobileandroid.appsnews.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mobileandroid.appsnews.ResponseParser
import com.mobileandroid.appsnews.api.FetchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull


class NewsRepository(private val fetchResponse: FetchResponse) {

    private val WIT_SERVER_TOKEN = "HYCDVURT7U27P4TUHSINCSBPCZ4JLOKJ"

    private val urlList = listOf(
        "https://www.theage.com.au/rss/feed.xml",
        "https://www.canberratimes.com.au/rss.xml",
        "https://www.cbc.ca/cmlink/rss-topstories",
        "https://feeds.thelocal.com/rss/es",
        "https://www.france24.com/en/rss",
        "https://www.hongkongfp.com/feed/",
        "https://www.republika.co.id/rss/",
        "https://feeds.breakingnews.ie/bntopstories",
        "https://www.news18.com/rss/world.xml",
        "https://www.indiatoday.in/rss/home",
        "https://www.newsweek.pl/rss.xml",
        "https://www.rt.com/rss/",
        "https://rss.unian.net/site/news_eng.rss",
        "https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml",
        "https://www.sowetanlive.co.za/rss/?publication=sowetan-live",
        "https://www.space.com/feeds/all"
    )

    private val parser = ResponseParser(this)

    private val _mutableLiveData = MutableLiveData<ArrayList<Map<String, String>>>()
    val responseLiveData: LiveData<ArrayList<Map<String, String>>> = _mutableLiveData

    /** Fetch RSS feeds concurrently without blocking main thread */


    suspend fun getSpeechToText(file: java.io.File): String? {
        return try {
            val contentType = "audio/wav"
            val requestFile = okhttp3.RequestBody.create(contentType.toMediaTypeOrNull(), file)
            val authHeader = "Bearer $WIT_SERVER_TOKEN"

            Log.d("NewsRepository", "📤 Sending audio: ${file.name}, size: ${file.length()} bytes")

            val response = com.mobileandroid.appsnews.api.RetrofitClient.witService.speechToText(
                authHeader,
                contentType,
                requestFile
            )

            val rawResponse = response.string()
            Log.d("NewsRepository", "📥 Raw Response: $rawResponse")

            // ✅ REGEX se multiple JSON objects extract karo
            val jsonPattern = """\{[^{}]*(?:\{[^{}]*\}[^{}]*)*\}""".toRegex()
            val jsonMatches = jsonPattern.findAll(rawResponse)

            var finalUnderstanding: String? = null
            var finalTranscription: String? = null
            var lastPartialUnderstanding: String? = null

            for (match in jsonMatches) {
                try {
                    val jsonObject = org.json.JSONObject(match.value)
                    val type = jsonObject.optString("type", "")
                    val text = jsonObject.optString("text", "").trim()
                    val isFinal = jsonObject.optBoolean("is_final", false)

                    if (text.isEmpty()) continue

                    when {
                        type == "FINAL_UNDERSTANDING" && isFinal -> {
                            finalUnderstanding = text
                            Log.d("NewsRepository", "✅ FINAL_UNDERSTANDING: '$text'")
                        }
                        type == "FINAL_TRANSCRIPTION" -> {
                            finalTranscription = text
                            Log.d("NewsRepository", "📝 FINAL_TRANSCRIPTION: '$text'")
                        }
                        type == "PARTIAL_UNDERSTANDING" -> {
                            lastPartialUnderstanding = text
                        }
                    }
                } catch (e: Exception) {
                    Log.w("NewsRepository", "JSON parse error: ${e.message}")
                }
            }

            // ✅ Priority: FINAL_UNDERSTANDING > FINAL_TRANSCRIPTION > PARTIAL
            val extractedText = finalUnderstanding
                ?: finalTranscription
                ?: lastPartialUnderstanding

            Log.d("NewsRepository", "🎯 Final Text: '$extractedText'")

            if (extractedText.isNullOrEmpty()) {
                Log.e("NewsRepository", "❌ No speech detected!")
                null
            } else {
                extractedText
            }
        } catch (e: Exception) {
            Log.e("NewsRepository", "❌ Error: ${e.message}")
            e.printStackTrace()
            null
        }
    }


//    suspend fun getSpeechToText(file: java.io.File): String? {
//        return try {
//            val contentType = "audio/wav"
//            val requestFile = okhttp3.RequestBody.create(contentType.toMediaTypeOrNull(), file)
//            val authHeader = "Bearer $WIT_SERVER_TOKEN"
//
//            Log.d("NewsRepository", "📤 Sending audio: ${file.name}, size: ${file.length()} bytes")
//
//            val response = com.mobileandroid.appsnews.api.RetrofitClient.witService.speechToText(
//                authHeader,
//                contentType,
//                requestFile
//            )
//
//            val rawResponse = response.string()
//            Log.d("NewsRepository", "📥 Raw Response: $rawResponse")
//
//            // 🛠️ FIX START: JSON Formatting ko clean karna
//            // 1. Saare newlines aur carriage returns hata kar ek single line banao
//            val cleanResponse = rawResponse.replace("\n", " ").replace("\r", " ")
//
//            // 2. Concatenated JSON objects "}{" ko "}\n{" se replace karo taaki split sahi ho
//            val formattedResponse = cleanResponse.replace("}{", "}\n{")
//
//            // 3. Ab split kaam karega kyunki har line ek complete JSON object hai
//            val lines = formattedResponse.split("\n")
//            // 🛠️ FIX END
//
//            var extractedText: String? = null
//
//            // ✅ Reverse order check (Last response usually contains final result)
//            for (line in lines.reversed()) {
//                if (line.isBlank()) continue // Empty lines skip karein
//
//                try {
//                    val jsonObject = org.json.JSONObject(line)
//                    val type = jsonObject.optString("type", "")
//                    val text = jsonObject.optString("text", "")
//
//                    // ✅ FINAL_UNDERSTANDING is best, but FINAL_TRANSCRIPTION is also good
//                    if (text.isNotEmpty() && (type == "FINAL_UNDERSTANDING" || type == "FINAL_TRANSCRIPTION")) {
//                        extractedText = text.trim()
//                        Log.d("NewsRepository", "✅ Found Final Text: '$extractedText' (type: $type)")
//                        break
//                    }
//
//                    // Backup: Agar final nahi mila toh last valid text store kar lo
//                    if (text.isNotEmpty() && extractedText == null) {
//                        extractedText = text.trim()
//                    }
//
//                } catch (e: Exception) {
//                    Log.w("NewsRepository", "JSON Parse Skipped for line: $line")
//                }
//            }
//
//            Log.d("NewsRepository", "🎯 Final Extracted Text: '$extractedText'")
//
//            if (extractedText.isNullOrEmpty()) {
//                Log.e("NewsRepository", "❌ No speech detected!")
//                null
//            } else {
//                extractedText
//            }
//        } catch (e: Exception) {
//            Log.e("NewsRepository", "❌ Voice API Error: ${e.message}")
//            e.printStackTrace()
//            null
//        }
//    }



    suspend fun getResponsesConcurrently(limit: Int = 5) = withContext(Dispatchers.IO) { // 🔹 limit added
        try {
            val allItems = ArrayList<Map<String, String>>()

            // 🔹 Load only "limit" feeds first (default 5)
            val initialUrls = urlList.take(limit)
            val remainingUrls = urlList.drop(limit)

            // 🔹 Fetch first batch fast
            val firstBatch = initialUrls.map { url ->
                async { fetchAndParse(url) }
            }.awaitAll().filterNotNull()

            firstBatch.forEach { allItems.addAll(it) }

            // 🔹 Update UI immediately with first batch
            _mutableLiveData.postValue(ArrayList(allItems))

            // 🔹 Load remaining feeds in background
            remainingUrls.map { url ->
                async {
                    fetchAndParse(url)
                }
            }.awaitAll().filterNotNull().forEach {
                allItems.addAll(it)
                _mutableLiveData.postValue(ArrayList(allItems)) // 🔹 keep updating
            }

        } catch (e: Exception) {
            Log.e("NewsRepository", "Error fetching feeds: ${e.message}", e)
        }
    }


//    suspend fun getResponsesConcurrently() = withContext(Dispatchers.IO) {
//        try {
//            val allItems = ArrayList<Map<String, String>>()
//
//            // Limit concurrency with async + awaitAll
//            val deferredList = urlList.map { url ->
//                async {
//                    fetchAndParse(url)
//                }
//            }
//
//            val results = deferredList.awaitAll()
//            results.filterNotNull().forEach { allItems.addAll(it) }
//
//            // Update LiveData (thread-safe)
//            _mutableLiveData.postValue(allItems)
//
//        } catch (e: Exception) {
//            Log.e("NewsRepository", "Error fetching feeds: ${e.message}", e)
//        }
//    }

    private suspend fun fetchAndParse(url: String): ArrayList<Map<String, String>>? {
        return try {
            val response = withTimeout(2000) { fetchResponse.getResponse(url) }
            if (response != null) {
                parser.parse(response)
            } else null
        } catch (e: Exception) {
            Log.e("NewsRepository", "Failed for $url -> ${e.message}")
            null
        }
    }
}

