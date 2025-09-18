package com.mobileandroid.appsnews.ui.Activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.mobileandroid.appsnews.R
import com.mobileandroid.appsnews.api.FetchResponse
import com.mobileandroid.appsnews.databinding.ActivityMainBinding
import com.mobileandroid.appsnews.repository.NewsRepository
import com.mobileandroid.appsnews.savedsearches.SavedSearches
import com.mobileandroid.appsnews.ui.adapters.PostAdapter
import com.mobileandroid.appsnews.utils.Utils
import com.mobileandroid.appsnews.viewmodels.DatabaseViewModel
import com.mobileandroid.appsnews.viewmodels.NewsItemViewModel
import com.mobileandroid.appsnews.viewmodels.NewsVMFactory



class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

        lateinit var binding: ActivityMainBinding
        lateinit var repository: NewsRepository
//        lateinit var progressDialog: ProgressDialog

        lateinit var viewModel: NewsItemViewModel

        private var isLoading = false
        private var itemList: ArrayList<Map<String, String>> = ArrayList()
        private var filteredList: ArrayList<Map<String, String>> = ArrayList()
        lateinit var factory: NewsVMFactory
        lateinit var adapter: PostAdapter
        private var currentPage = 0
        private val pageSize = 10
        private val utils = Utils() // Initialize Utils

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // ⏳ Show Loading initially
            binding.tvLoading.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.searchBarContainer.visibility = View.GONE


            //        progressDialog = ProgressDialog(this)
    //        progressDialog.setTitle("Loading...")
    //        progressDialog.create()
    //        progressDialog.show()

            val fetchResponse = FetchResponse()
            repository = NewsRepository(fetchResponse)
            factory = NewsVMFactory(repository)
            viewModel = ViewModelProvider(this, factory)[NewsItemViewModel::class.java]
            viewModel.loadNextBatch()


            if (utils.isNetworkAvailable(this)){
                viewModel.loadNextBatch()
            }else{
                showNoInternetDialog()
            }


            setupRecyclerNews()
            setupSwipeRefresh()
           // setupNavigationDrawer()


            //  Setup search bar click
    //        binding.searchEditText.setOnEditorActionListener { _, _, _ ->
    //            val query = binding.searchEditText.text?.toString()?.trim()
    //            if (!query.isNullOrEmpty()) {
    //                saveSearchQuery(query)
    //
    //                val intent = Intent(this, SearchActivity::class.java)
    //                intent.putExtra("search_query", query)
    //                startActivity(intent)
    //            }
    //            true
    //        }

            binding.ivOverflow.setOnClickListener { view ->
                val popup = PopupMenu(this, view)
                popup.menuInflater.inflate(R.menu.menu_overflow, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.main_bookmarks -> {
                            startActivity(Intent(this, BookmarksActivity::class.java))
                            true
                        }
                        R.id.SaveSearchNews -> {
                            startActivity(Intent(this, SavedSearchActivity::class.java))
                            true
                        }

                        R.id.privacy_policy -> {
                            showPrivacyDialog()
                            true
                        }

                        R.id.exit -> {
                            finishAffinity()
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }


            binding.searchBar.setOnClickListener {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }





            // Then check if we need to perform a search
            val searchQuery = intent.getStringExtra("search_query")
            searchQuery?.let {
                // Wait for items to load before searching
                viewModel.newsLiveData.observe(this) { items ->
                    if (items.isNotEmpty()) {
                        performSearch(searchQuery)
                    }
                }
            }

            onBackPressedDispatcher.addCallback {
                handleBackPress()
            }

    //        binding.navigationView.setNavigationItemSelectedListener(this)
    //
    //        binding.toolbarMain.setNavigationOnClickListener {
    //            binding.drawerLayout.openDrawer(GravityCompat.START)
    //        }

            //optionsMenuSelected()

        }

    private fun showPrivacyDialog() {
        val webView = WebView(this).apply {
            settings.javaScriptEnabled = false

            // Force WebView to respect system dark mode if available
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_AUTO)
            }

            // (Optional) make the WebView itself match dialog surface
            // This is safe because HTML already handles dark via CSS above
            setBackgroundColor(
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface)
            )

            loadUrl("file:///android_asset/privacy_policy.html")
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_privacy_policy)
            .setView(webView)
            .setPositiveButton("Close", null)
            .create()

        dialog.setOnShowListener {
            val closeBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            // light gray text for the Close button
            closeBtn.setTextColor(ContextCompat.getColor(this, R.color.text_title_color))
        }

        dialog.show()
    }


    private fun showNoInternetDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setCancelable(false)
            .setPositiveButton("Retry") { _, _ ->
                if (utils.isNetworkAvailable(this)) {
//                    progressDialog.show()
                    viewModel.loadNextBatch()
                } else {
                    showNoInternetDialog()
                }
            }
            .setNegativeButton("Exit") { _, _ -> finish() }
            .create()
            .show()
    }
    private fun setupSwipeRefresh() {
        // Set up SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            itemList.clear()
//            adapter.notifyDataSetChanged()
            // ️ OLD → adapter.notifyDataSetChanged()
            //  NEW → updateList with empty
            adapter.updateList(itemList) //  CHANGED

            currentPage = 0
            isLoading = false
            viewModel.loadNextBatch()
        }
    }


//    private fun optionsMenuSelected() {
//        binding.toolbarMain.setOnMenuItemClickListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.exit -> {
//                    finishAffinity()
//                }
////                R.id.settings -> {
////                }
//
//                R.id.main_bookmarks ->{
//                    val intent = Intent(this, BookmarksActivity::class.java)
//                    startActivity(intent)
//                }
//                R.id.main_menu_action_search ->{
//                    val intent = Intent(this, SearchActivity::class.java)
//                    startActivity(intent)
//                }
//                R.id.SaveSearchNews ->{
//                    val intent = Intent(this, SavedSearchActivity::class.java)
//                    startActivity(intent)
//                }
//            }
//            return@setOnMenuItemClickListener true
//        }
//    }


    private fun setupRecyclerNews() {
        binding.recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = linearLayoutManager

//        adapter = PostAdapter(this, itemList)
        adapter = PostAdapter(this, itemList.toMutableList()) //  CHANGED (MutableList)
        binding.recyclerView.adapter = adapter

//        viewModel.newsLiveData.observe(this) { items ->
//            itemList.addAll(items)
//            progressDialog.cancel()
//            adapter.notifyDataSetChanged()
//        }

        viewModel.newsLiveData.observe(this) { items ->
//            itemList.addAll(items)
//            adapter.notifyDataSetChanged()

            // ⛔️ OLD → itemList.addAll(items) + notifyDataSetChanged()
            // ✅ NEW → adapter.updateList()
            adapter.updateList(items) //  CHANGED

            //  Hide loading text, show RecyclerView
            binding.tvLoading.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            binding.searchBarContainer.visibility = View.VISIBLE


            //  Ab search bar container bhi tabhi dikhana hai jab posts load ho jaye
            binding.searchBarContainer.visibility = View.VISIBLE

//            progressDialog.dismiss() // Dismiss initial ProgressDialog
            binding.swipeRefreshLayout.isRefreshing = false // Stop SwipeRefreshLayout loading indicator
            isLoading = false // Reset loading state
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItem >= totalItemCount - 5) {
                    loadMoreItems()
                    isLoading = true
                }
            }
        })
    }

    private fun loadMoreItems() {
        currentPage++
        isLoading = true // Set loading to true
        viewModel.loadNextBatch()
    }



    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finishAffinity()
                }
            }
        })
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_search, menu)
//
//        val searchItem = menu?.findItem(R.id.action_search)
//        val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView
//
//        searchView?.queryHint = "Search news..."
//        searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                query?.let {
//                    saveSearchQuery(it.trim())
//                    performSearch(it)
//                }
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                return false
//            }
//        })
//
//        return true
//    }



    //  Save Search to Database
    private fun saveSearchQuery(query: String) {
        val savedSearch = SavedSearches(query)
        val viewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
        viewModel.addRecentSearch(savedSearch)
        Toast.makeText(this, "Search saved!", Toast.LENGTH_SHORT).show()
    }


//    // Update the performSearch function to correctly filter the news items
//    private fun performSearch(query: String) {
//        if (query.isEmpty()) {
//            return
//        }
//        val allItems = ArrayList(itemList)
//        itemList.clear()
//
//        val filteredItems = allItems.filter { item ->
//            item["title"]?.contains(query, ignoreCase = true) == true ||
//                    item["description"]?.contains(query, ignoreCase = true) == true
//            item["image"]?.contains(query, ignoreCase = true) == true
//        }
//
//        itemList.addAll(filteredItems)
//        adapter.notifyDataSetChanged()
//
//        if (itemList.isEmpty()) {
//            Toast.makeText(this, "No results found for: $query", Toast.LENGTH_SHORT).show()
//            itemList.addAll(allItems)
//            adapter.notifyDataSetChanged()
//        } else {
//            Toast.makeText(this, "Found ${itemList.size} results for: $query", Toast.LENGTH_SHORT).show()
//        }
//    }



    private fun performSearch(query: String) {
        if (query.isEmpty()) return

        val allItems = ArrayList(itemList)
        itemList.clear()

        val filteredItems = allItems.filter { item ->
            (item["title"]?.contains(query, ignoreCase = true) == true) ||
                    (item["description"]?.contains(query, ignoreCase = true) == true) ||
                    (item["image"]?.contains(query, ignoreCase = true) == true)
        }

//        itemList.addAll(filteredItems)
//        adapter.notifyDataSetChanged()

        // ⛔ OLD → itemList.clear() + addAll + notify
        // ✅ NEW → adapter.updateList(filteredItems)
        adapter.updateList(filteredItems) //  CHANGED

        if (itemList.isEmpty()) {
            Toast.makeText(this, "No results found for: $query", Toast.LENGTH_SHORT).show()
            // restore original list
            itemList.addAll(allItems)
            adapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "Found ${itemList.size} results for: $query", Toast.LENGTH_SHORT).show()
        }
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.exit -> {
                finishAffinity()
            }
//            R.id.settings -> {
//            }
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.home -> {
//                binding.drawerLayout.closeDrawer(GravityCompat.START)
//               // showToast("Home", this)
//            }
//            R.id.settings -> {
//            }

//            R.id.history -> {
//            }
        }
        return true
    }
}

