//package com.mobileandroid.appsnews.repository
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import com.mobileandroid.appsnews.ResponseParser
//import com.mobileandroid.appsnews.api.FetchResponse
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.withContext
//import kotlinx.coroutines.withTimeout
//
//
//class NewsRepository(private val fetchResponse: FetchResponse) {
//
//    private val urlList = listOf(
//        "https://www.theage.com.au/rss/feed.xml",
//        "https://www.canberratimes.com.au/rss.xml",
//        "https://www.cbc.ca/cmlink/rss-topstories",
//        "https://feeds.thelocal.com/rss/es",
//        "https://www.france24.com/en/rss",
//        "https://www.hongkongfp.com/feed/",
//        "https://www.republika.co.id/rss/",
//        "https://feeds.breakingnews.ie/bntopstories",
//        "https://www.news18.com/rss/world.xml",
//        "https://www.indiatoday.in/rss/home",
//        "https://www.newsweek.pl/rss.xml",
//        "https://www.rt.com/rss/",
//        "https://rss.unian.net/site/news_eng.rss",
//        "https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml",
//        "https://www.sowetanlive.co.za/rss/?publication=sowetan-live",
//        "https://www.space.com/feeds/all"
//    )
//
//    private val parser = ResponseParser(this)
//
//    private val _mutableLiveData = MutableLiveData<ArrayList<Map<String, String>>>()
//    val responseLiveData: LiveData<ArrayList<Map<String, String>>> = _mutableLiveData
//
//    /** Fetch RSS feeds concurrently without blocking main thread */
//
//
//    suspend fun getResponsesConcurrently(limit: Int = 5) = withContext(Dispatchers.IO) { // 🔹 limit added
//        try {
//            val allItems = ArrayList<Map<String, String>>()
//
//            // 🔹 Load only "limit" feeds first (default 5)
//            val initialUrls = urlList.take(limit)
//            val remainingUrls = urlList.drop(limit)
//
//            // 🔹 Fetch first batch fast
//            val firstBatch = initialUrls.map { url ->
//                async { fetchAndParse(url) }
//            }.awaitAll().filterNotNull()
//
//            firstBatch.forEach { allItems.addAll(it) }
//
//            // 🔹 Update UI immediately with first batch
//            _mutableLiveData.postValue(ArrayList(allItems))
//
//            // 🔹 Load remaining feeds in background
//            remainingUrls.map { url ->
//                async {
//                    fetchAndParse(url)
//                }
//            }.awaitAll().filterNotNull().forEach {
//                allItems.addAll(it)
//                _mutableLiveData.postValue(ArrayList(allItems)) // 🔹 keep updating
//            }
//
//        } catch (e: Exception) {
//            Log.e("NewsRepository", "Error fetching feeds: ${e.message}", e)
//        }
//    }
//
//
////    suspend fun getResponsesConcurrently() = withContext(Dispatchers.IO) {
////        try {
////            val allItems = ArrayList<Map<String, String>>()
////
////            // Limit concurrency with async + awaitAll
////            val deferredList = urlList.map { url ->
////                async {
////                    fetchAndParse(url)
////                }
////            }
////
////            val results = deferredList.awaitAll()
////            results.filterNotNull().forEach { allItems.addAll(it) }
////
////            // Update LiveData (thread-safe)
////            _mutableLiveData.postValue(allItems)
////
////        } catch (e: Exception) {
////            Log.e("NewsRepository", "Error fetching feeds: ${e.message}", e)
////        }
////    }
//
//    private suspend fun fetchAndParse(url: String): ArrayList<Map<String, String>>? {
//        return try {
//            val response = withTimeout(2000) { fetchResponse.getResponse(url) }
//            if (response != null) {
//                parser.parse(response)
//            } else null
//        } catch (e: Exception) {
//            Log.e("NewsRepository", "Failed for $url -> ${e.message}")
//            null
//        }
//    }
//}
//



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
import java.util.Collections
import java.util.LinkedHashSet

class NewsRepository(private val fetchResponse: FetchResponse) {

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

    // --- de-dup memory (URL based) ---
    private val seenLinks = Collections.synchronizedSet(LinkedHashSet<String>())

    fun resetFeedCache() {
        seenLinks.clear()
    }

    private fun dedupeByUrl(items: List<Map<String, String>>): ArrayList<Map<String, String>> {
        val out = ArrayList<Map<String, String>>(items.size)
        for (m in items) {
            val link = m["link"]?.trim().orEmpty()
            if (link.isNotBlank() && seenLinks.add(link)) {
                out.add(m)
            }
        }
        return out
    }

    /** Fetch RSS feeds concurrently; push updates incrementally, deduped */
    suspend fun getResponsesConcurrently(limit: Int = 5) = withContext(Dispatchers.IO) {
        try {
            val all = ArrayList<Map<String, String>>()

            val initialUrls = urlList.take(limit)
            val remainingUrls = urlList.drop(limit)

            // First batch
            val firstBatch = initialUrls.map { url -> async { fetchAndParse(url) } }
                .awaitAll()
                .filterNotNull()
                .flatten()

            val firstDedup = dedupeByUrl(firstBatch)
            if (firstDedup.isNotEmpty()) {
                all.addAll(firstDedup)
                _mutableLiveData.postValue(ArrayList(all))
            }

            // Remaining batches (update incrementally)
            remainingUrls.map { url -> async { fetchAndParse(url) } }
                .awaitAll()
                .forEach { list ->
                    if (list != null) {
                        val dedup = dedupeByUrl(list)
                        if (dedup.isNotEmpty()) {
                            all.addAll(dedup)
                            _mutableLiveData.postValue(ArrayList(all))
                        }
                    }
                }

        } catch (e: Exception) {
            Log.e("NewsRepository", "Error fetching feeds: ${e.message}", e)
        }
    }

    private suspend fun fetchAndParse(url: String): ArrayList<Map<String, String>>? {
        return try {
            // 2s bahut tight hai mobile/DNS ke liye; 8s zyada stable rahega
            val response = withTimeout(8_000) { fetchResponse.getResponse(url) }
            if (response.isNullOrBlank()) return null
            val parsed = parser.parse(response) ?: return null

            // filter out blanks (just in case)
            ArrayList(parsed.filter { it["link"]?.isNotBlank() == true })
        } catch (e: Exception) {
            Log.w("NewsRepository", "Failed for $url -> ${e.message}")
            null
        }
    }
}
