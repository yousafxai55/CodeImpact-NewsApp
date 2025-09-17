package com.mobileandroid.appsnews.ui.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobileandroid.appsnews.R
import com.mobileandroid.appsnews.databinding.ActivitySavedSearchBinding
import com.mobileandroid.appsnews.savedsearches.SavedSearches
import com.mobileandroid.appsnews.ui.adapters.SavedSearchAdapter
import com.mobileandroid.appsnews.viewmodels.DatabaseViewModel
class SavedSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedSearchBinding
    private lateinit var viewModel: DatabaseViewModel
    private lateinit var adapter: SavedSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.savedSearchesToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        viewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[DatabaseViewModel::class.java]

        binding.savedSearchesRecyclerview.layoutManager = LinearLayoutManager(this)
        adapter = SavedSearchAdapter(
            emptyList(),
            ::onSearchClick,
            ::onSearchLongClick
        )
        binding.savedSearchesRecyclerview.adapter = adapter

        viewModel.getAllRecentSearches().observe(this) { searches ->
            adapter.updateData(searches)
        }

        binding.savedSearchesToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun onSearchClick(search: String) {
        val intent = Intent(this, SearchActivity::class.java)
        intent.putExtra("search_query", search)
        startActivity(intent)
    }

//    private fun onSearchLongClick(savedSearch: SavedSearches) {
//        AlertDialog.Builder(this).apply {
//            setTitle("Delete Search")
//            setMessage("Are you sure you want to delete '${savedSearch.searchTopic}'?")
//            setPositiveButton("Yes") { _, _ ->
//                viewModel.deleteRecentSearch(savedSearch)
//                Toast.makeText(context, "Search deleted!", Toast.LENGTH_SHORT).show()
//            }
//            setNegativeButton("No", null)
//        }.show()
//    }

    private fun onSearchLongClick(savedSearch: SavedSearches) {
        val dialogView = layoutInflater.inflate(R.layout.savedsearch_delete_dialog, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val message = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        // Customize text dynamically
        message.text = "Are you sure you want to delete '${savedSearch.searchTopic}'?"

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            viewModel.deleteRecentSearch(savedSearch)
            Toast.makeText(this, "Search deleted!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

}

