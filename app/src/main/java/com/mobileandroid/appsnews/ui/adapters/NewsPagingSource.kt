//package com.example.myapplication.adapters
//
//import android.util.Log
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import com.example.myapplication.repository.NewsRepository
//
//class NewsPagingSource(
//    private val repository: NewsRepository
//) : PagingSource<Int, Map<String, String>>() {
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Map<String, String>> {
//        return try {
//            Log.d("NewsPagingSource", "Loading data with params: ${params.key}, loadSize: ${params.loadSize}")
//
//            // Fetch the data and return as a list
//            val response = repository.getResponsesConcurrently()
//
//            Log.d("NewsPagingSource", "Data loaded successfully, size: ${response.size}")
//
//
//            LoadResult.Page(
//                data = response, // Correctly using the list returned from the repository
//                prevKey = null, // RSS feed paging is not handled by key
//                nextKey = null  // Assume no further paging
//            )
//        } catch (e: Exception) {
//
//            Log.e("NewsPagingSource", "Error loading data", e)
//
//
//            LoadResult.Error(e)
//        }
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, Map<String, String>>): Int? {
//
//        Log.d("NewsPagingSource", "getRefreshKey called with anchorPosition: ${state.anchorPosition}")
//
//        return null // No key-based paging for RSS feeds
//    }
//}
//


package com.mobileandroid.appsnews.ui.adapters

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mobileandroid.appsnews.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewsPagingSource(
    private val repository: NewsRepository
) : PagingSource<Int, Map<String, String>>() {

    private var cachedData: List<Map<String, String>>? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Map<String, String>> =
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()

            try {
                val currentPage = params.key ?: 0
                val pageSize = params.loadSize

                // Fetch data only if cache is empty
                if (cachedData == null) {
                   // cachedData = repository.getResponsesConcurrently()
                }

                val data = cachedData ?: emptyList()
                val startPosition = currentPage * pageSize
                val endPosition = minOf((currentPage + 1) * pageSize, data.size)

                if (startPosition >= data.size) {
                    return@withContext LoadResult.Page(
                        data = emptyList(),
                        prevKey = null,
                        nextKey = null
                    )
                }

                val pageData = data.subList(startPosition, endPosition)
                val endTime = System.currentTimeMillis()

                Log.d("NewsPagingSource", "Page $currentPage loaded with ${pageData.size} items in ${endTime - startTime}ms")

                LoadResult.Page(
                    data = pageData,
                    prevKey = if (currentPage > 0) currentPage - 1 else null,
                    nextKey = if (endPosition < data.size) currentPage + 1 else null
                )
            } catch (e: Exception) {
                Log.e("NewsPagingSource", "Error loading data", e)
                LoadResult.Error(e)
            }
        }

    override fun getRefreshKey(state: PagingState<Int, Map<String, String>>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
