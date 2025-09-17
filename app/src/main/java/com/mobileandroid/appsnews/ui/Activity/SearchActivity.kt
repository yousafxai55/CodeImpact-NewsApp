package com.mobileandroid.appsnews.ui.Activity

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.mobileandroid.appsnews.R
import com.mobileandroid.appsnews.databinding.ActivitySearchBinding
import com.mobileandroid.appsnews.savedsearches.SavedSearches
import com.mobileandroid.appsnews.ui.Fragment.PostsFragment
import com.mobileandroid.appsnews.viewmodels.DatabaseViewModel


class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: DatabaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]



        setupBackPressedHandler()

        val searchQuery = intent.getStringExtra("search_query") ?: ""
        if (searchQuery.isNotEmpty()) {
            binding.searchEditText.setText(searchQuery)
            showSearchResults(searchQuery)
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    saveSearchQuery(query)
                    showSearchResults(query)
                }
                true
            } else false
        }

        // 👉 Search icon click पर भी search
        binding.ivSearch.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                saveSearchQuery(query)
                showSearchResults(query)
            }
        }

        loadRecentSearches()
    }

//    private fun showSearchResults(query: String) {
//        binding.recentSearchesChipGroup.visibility = View.GONE
//        binding.tvRecentSearches.visibility = View.GONE
//
//        val fragment = PostsFragment.newInstance(query)
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.search_fragment_container, fragment)
//            .addToBackStack(null)
//            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//            .commit()
//    }

    private fun showSearchResults(query: String) {
        // Search bar update
        binding.searchEditText.setText(query)
        binding.searchEditText.setSelection(query.length)

        // Hide recent searches
        binding.recentSearchesChipGroup.visibility = View.GONE
        binding.tvRecentSearches.visibility = View.GONE

        // Show loading
        binding.searchLoading.visibility = View.VISIBLE
        binding.searchFragmentContainer.visibility = View.GONE

        val fragmentTag = "PostsFragment"
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment != null && existingFragment is PostsFragment) {
            existingFragment.updateSearchQuery(query)
            existingFragment.setOnDataLoadedListener {
                binding.searchLoading.visibility = View.GONE
                binding.searchFragmentContainer.visibility = View.VISIBLE
            }
        } else {
            val newFragment = PostsFragment.newInstance(query)
            newFragment.setOnDataLoadedListener {
                binding.searchLoading.visibility = View.GONE
                binding.searchFragmentContainer.visibility = View.VISIBLE
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.search_fragment_container, newFragment, fragmentTag)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
    }


//    private fun showSearchResults(query: String) {
//
//        //  Search bar me bhi query set karo
//        binding.searchEditText.setText(query)
//        binding.searchEditText.setSelection(query.length) // cursor end pe
//
//        binding.recentSearchesChipGroup.visibility = View.GONE
//        binding.tvRecentSearches.visibility = View.GONE
//
//
//        val fragmentTag = "PostsFragment"
//        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
//
//        if (existingFragment != null && existingFragment is PostsFragment) {
//            //  Agar fragment pehle se loaded hai → update query
//            existingFragment.updateSearchQuery(query)
//        } else {
//            //  Agar pehli baar search ho rahi hai → naya fragment load karo
//            val newFragment = PostsFragment.newInstance(query)
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.search_fragment_container, newFragment, fragmentTag)
//                .addToBackStack(null)
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                .commit()
//        }
//    }


    private fun loadRecentSearches() {
        viewModel.getAllRecentSearches().observe(this) { searches ->
            binding.recentSearchesChipGroup.removeAllViews()
            if (searches.isNotEmpty()) {
                binding.tvRecentSearches.visibility = View.VISIBLE
                searches.forEach { savedSearch ->
                    val chip = Chip(this).apply {
                        text = savedSearch.searchTopic
                        setOnClickListener { showSearchResults(savedSearch.searchTopic) }
                    }
                    binding.recentSearchesChipGroup.addView(chip)
                }
            } else {
                binding.tvRecentSearches.visibility = View.GONE
            }
        }
    }

    private fun saveSearchQuery(query: String) {
        val savedSearch = SavedSearches(query)
        viewModel.addRecentSearch(savedSearch)
    }

//    override fun onBackPressed() {
//        if (supportFragmentManager.backStackEntryCount > 0) {
//            supportFragmentManager.popBackStack()
//            binding.recentSearchesChipGroup.visibility = View.VISIBLE
//            binding.tvRecentSearches.visibility = View.VISIBLE
//        } else {
//            super.onBackPressed()
//        }
//    }

    //  Back Press ko handle karne ka function
    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                    binding.recentSearchesChipGroup.visibility = View.VISIBLE
                    binding.tvRecentSearches.visibility = View.VISIBLE
                } else {
                    finish() // default behavior → Activity close
                }
            }
        })
    }
}

