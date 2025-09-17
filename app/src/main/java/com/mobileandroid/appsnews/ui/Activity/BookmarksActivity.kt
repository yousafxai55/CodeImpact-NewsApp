package com.mobileandroid.appsnews.ui.Activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobileandroid.appsnews.R

import com.mobileandroid.appsnews.bookmarks.Bookmark
import com.mobileandroid.appsnews.databinding.ActivityBookmarksBinding
import com.mobileandroid.appsnews.ui.adapters.BookmarksAdapter
import com.mobileandroid.appsnews.viewmodels.DatabaseViewModel

class BookmarksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookmarksBinding
    private lateinit var viewModel: DatabaseViewModel
    private lateinit var adapter: BookmarksAdapter
    private var bookmarkList: List<Bookmark> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setSupportActionBar(binding.bookmarksToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]

        binding.bookmarksRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BookmarksAdapter(emptyList(), ::openNewsDetail, ::deleteBookmark)
        binding.bookmarksRecyclerView.adapter = adapter

        //  Fix: Observe data & correctly handle visibility of textNoBookmarks
        viewModel.getAllBookmarks().observe(this) { bookmarks ->
            bookmarkList = bookmarks
            if (bookmarks.isEmpty()) {
                binding.textNoBookmarks.visibility = View.VISIBLE
                binding.bookmarksRecyclerView.visibility = View.GONE
            } else {
                binding.textNoBookmarks.visibility = View.GONE
                binding.bookmarksRecyclerView.visibility = View.VISIBLE
                adapter.updateData(bookmarks)
            }
        }

        // Back Button
//        binding.bookmarksToolbar.setNavigationOnClickListener {
//            finish()
//        }

        //  Custom SearchBar click → Activate SearchView
        binding.bookmarkSearchBar.setOnClickListener {
            val searchView = SearchView(this).apply {
                queryHint = "Search bookmarks..."
                isIconified = false
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    adapter.filter.filter(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return true
                }
            })

            // Replace fake searchbar text with real SearchView
            binding.searchBarContainer.removeAllViews()
            binding.searchBarContainer.addView(searchView)
        }


        binding.ivOverflow.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.menu_bookmarks, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_clear_all_bookmarks -> {
                        if (bookmarkList.isNotEmpty()) {
                            viewModel.deleteAllBookmarks()
                            Toast.makeText(this, "All bookmarks cleared", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "No bookmarks to clear", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }

//        binding.bookmarkSearchBar.setOnClickListener {
//            showSearchDialog()
//        }


    }

    private fun showSearchDialog() {
        val searchView = android.widget.EditText(this).apply {
            hint = "Search bookmarks..."
            setPadding(40, 30, 40, 30)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Search")
            .setView(searchView)
            .setPositiveButton("Search") { _, _ ->
                val query = searchView.text.toString().trim()
                if (query.isNotEmpty()) {
                    adapter.filter.filter(query)
                } else {
                    Toast.makeText(this, "Enter a keyword to search", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openNewsDetail(bookmark: Bookmark) {
        val intent = Intent(this, NewsDetailActivity::class.java).apply {
            putExtra("title", bookmark.postTitle)
            putExtra("imageLink", bookmark.postImage)
            putExtra("description", bookmark.postUrl)
            putExtra("link", bookmark.postUrl)
            putExtra("source", bookmark.postSource)
        }
        startActivity(intent)
    }

    private fun deleteBookmark(bookmark: Bookmark) {
        viewModel.deleteBookmark(bookmark)
        Toast.makeText(this, "Bookmark Removed", Toast.LENGTH_SHORT).show()
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_bookmarks, menu)
//
//        val searchItem = menu?.findItem(R.id.action_search_bookmarks)
//        val searchView = searchItem?.actionView as? SearchView
//        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean = false
//            override fun onQueryTextChange(newText: String?): Boolean {
//                adapter.filter.filter(newText)
//                return true
//            }
//        })
//
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all_bookmarks -> {
                viewModel.deleteAllBookmarks()
                Toast.makeText(this, "All bookmarks cleared", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
